package com.develop.mvp.pk.module.pay.infrastructure.wallet;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackagePageReqVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import com.develop.mvp.pk.module.pay.dal.mysql.wallet.PayWalletRechargePackageMapper;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PayWalletRechargePackageRepositoryImpl implements PayWalletRechargePackageRepository {

    @Resource
    private PayWalletRechargePackageMapper walletRechargePackageMapper;

    @Override
    public PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage) {
        PayWalletRechargePackageDO rechargePackageDO = toDO(rechargePackage);
        if (rechargePackage.id() == null) {
            walletRechargePackageMapper.insert(rechargePackageDO);
            return toDomain(rechargePackageDO);
        }
        walletRechargePackageMapper.updateById(rechargePackageDO);
        return rechargePackage;
    }

    @Override
    public PayWalletRechargePackage findById(Long id) {
        return toDomain(walletRechargePackageMapper.selectById(id));
    }

    @Override
    public PayWalletRechargePackage findByName(String name) {
        return toDomain(walletRechargePackageMapper.selectByName(name));
    }

    @Override
    public void deleteById(Long id) {
        walletRechargePackageMapper.deleteById(id);
    }

    @Override
    public PageResult<PayWalletRechargePackage> findPage(String name, Integer status, LocalDateTime[] createTime,
                                                         Integer pageNo, Integer pageSize) {
        WalletRechargePackagePageReqVO reqVO = new WalletRechargePackagePageReqVO();
        reqVO.setName(name);
        reqVO.setStatus(status);
        reqVO.setCreateTime(createTime);
        if (pageNo != null) {
            reqVO.setPageNo(pageNo);
        }
        if (pageSize != null) {
            reqVO.setPageSize(pageSize);
        }
        PageResult<PayWalletRechargePackageDO> page = walletRechargePackageMapper.selectPage(reqVO);
        return new PageResult<>(page.getList().stream().map(PayWalletRechargePackageRepositoryImpl::toDomain).toList(),
                page.getTotal());
    }

    @Override
    public List<PayWalletRechargePackage> findListByStatus(Integer status) {
        return walletRechargePackageMapper.selectListByStatus(status).stream()
                .map(PayWalletRechargePackageRepositoryImpl::toDomain).toList();
    }

    private static PayWalletRechargePackage toDomain(PayWalletRechargePackageDO rechargePackageDO) {
        if (rechargePackageDO == null) {
            return null;
        }
        return new PayWalletRechargePackage(rechargePackageDO.getId())
                .name(rechargePackageDO.getName())
                .payPrice(rechargePackageDO.getPayPrice())
                .bonusPrice(rechargePackageDO.getBonusPrice())
                .status(rechargePackageDO.getStatus());
    }

    private static PayWalletRechargePackageDO toDO(PayWalletRechargePackage rechargePackage) {
        PayWalletRechargePackageDO rechargePackageDO = new PayWalletRechargePackageDO();
        rechargePackageDO.setId(rechargePackage.id());
        rechargePackageDO.setName(rechargePackage.name());
        rechargePackageDO.setPayPrice(rechargePackage.payPrice());
        rechargePackageDO.setBonusPrice(rechargePackage.bonusPrice());
        rechargePackageDO.setStatus(rechargePackage.status());
        return rechargePackageDO;
    }
}
