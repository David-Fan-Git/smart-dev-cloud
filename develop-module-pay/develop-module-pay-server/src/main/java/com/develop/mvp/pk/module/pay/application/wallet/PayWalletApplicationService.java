package com.develop.mvp.pk.module.pay.application.wallet;
// DDD 角色：钱包应用服务 - AggregateRoot_Pay_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletFactory;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletTransactionRepository;
import com.develop.mvp.pk.module.pay.domain.wallet.service.PayWalletLock;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_BALANCE_NOT_ENOUGH;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_FREEZE_PRICE_NOT_ENOUGH;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_NOT_FOUND;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_REFUND_EXIST;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_TRANSACTION_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PayWalletApplicationService {

	private final PayWalletRepository walletRepo;

	private final PayWalletTransactionRepository transactionRepo;

	private final PayWalletLock walletLock;

	@Transactional
	public PayWallet getOrCreate(Long userId, Integer userType) {

		return walletRepo.findByUserIdAndType(userId, userType).orElseGet(() -> walletLock.lock(userId, () -> walletRepo.findByUserIdAndType(userId, userType).orElseGet(() -> walletRepo.save(PayWalletFactory.create(null, userId, userType)))));
	}

	@Transactional
	public PayWalletTransaction addBalance(Long walletId, String bizId, Integer bizType, Integer price, String title) {

		PayWallet wallet = walletRepo.findById(walletId);
		if (wallet == null)
			throw exception(WALLET_NOT_FOUND);
		return walletLock.lock(walletId, () -> {
			PayWalletBizTypeEnum walletBizType = PayWalletBizTypeEnum.valueOf(bizType);
			switch (walletBizType) {
				case PAYMENT_REFUND -> walletRepo.updateWhenConsumptionRefund(walletId, price);
				case RECHARGE -> walletRepo.updateWhenRecharge(walletId, price);
				case UPDATE_BALANCE, TRANSFER -> walletRepo.updateWhenAdd(walletId, price);
				default -> throw new UnsupportedOperationException("待实现：" + walletBizType);
			}
			PayWalletTransaction tx = new PayWalletTransaction(null).walletId(walletId).bizType(bizType).bizId(bizId).price(price).balance(wallet.balance() + price).title(walletBizType.getDescription());
			return transactionRepo.save(tx);
		});
	}

	@Transactional
	public PayWalletTransaction deductBalance(Long walletId, Long bizId, Integer bizType, Integer price) {

		PayWallet wallet = walletRepo.findById(walletId);
		if (wallet == null)
			throw exception(WALLET_NOT_FOUND);
		return walletLock.lock(walletId, () -> {
			PayWalletBizTypeEnum walletBizType = PayWalletBizTypeEnum.valueOf(bizType);
			int updateCount = switch (walletBizType) {
				case PAYMENT -> walletRepo.updateWhenConsumption(walletId, price);
				case RECHARGE_REFUND -> walletRepo.updateWhenRechargeRefund(walletId, price);
				default -> throw new UnsupportedOperationException("待实现");
			};
			if (updateCount == 0)
				throw exception(WALLET_BALANCE_NOT_ENOUGH);
			Integer afterBalance = walletBizType == PayWalletBizTypeEnum.RECHARGE_REFUND ? wallet.balance() : wallet.balance() - price;
			PayWalletTransaction tx = new PayWalletTransaction(null).walletId(walletId).bizType(bizType).bizId(String.valueOf(bizId)).price(-price).balance(afterBalance).title(walletBizType.getDescription());
			return transactionRepo.save(tx);
		});
	}

	@Transactional
	public PayWalletTransaction refundPayment(Long refundId, String walletPayNo, Integer refundPrice) {

		PayWalletTransaction paymentTransaction = transactionRepo.findByNo(walletPayNo).orElseThrow(() -> exception(WALLET_TRANSACTION_NOT_FOUND));
		if (transactionRepo.findByBiz(String.valueOf(refundId), PayWalletBizTypeEnum.PAYMENT_REFUND.getType()).isPresent()) {
			throw exception(WALLET_REFUND_EXIST);
		}
		return addBalance(paymentTransaction.walletId(), String.valueOf(refundId), PayWalletBizTypeEnum.PAYMENT_REFUND.getType(), refundPrice, null);
	}

	@Transactional
	public void freezePrice(Long walletId, Integer price) {

		int updateCount = walletRepo.freezePrice(walletId, price);
		if (updateCount == 0)
			throw exception(WALLET_BALANCE_NOT_ENOUGH);
	}

	@Transactional
	public void unfreezePrice(Long walletId, Integer price) {

		int updateCount = walletRepo.unFreezePrice(walletId, price);
		if (updateCount == 0)
			throw exception(WALLET_FREEZE_PRICE_NOT_ENOUGH);
	}

	public PayWallet get(Long id) {

		return walletRepo.findById(id);
	}

	public PayWallet getByUserId(Long userId, Integer userType) {

		return walletRepo.findByUserIdAndType(userId, userType).orElse(null);
	}

	public PageResult<PayWallet> getPage(Long userId, Integer userType, Integer pageNo, Integer pageSize) {

		return walletRepo.findPage(userId, userType, pageNo, pageSize);
	}

}
