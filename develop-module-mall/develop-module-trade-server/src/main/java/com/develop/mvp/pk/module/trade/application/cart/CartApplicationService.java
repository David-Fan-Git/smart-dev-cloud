package com.develop.mvp.pk.module.trade.application.cart;

// Skill: AggregateRoot_Cart_Validation_Skill — 应用服务 CartApplicationService

import com.develop.mvp.pk.module.trade.domain.cart.Cart;
import com.develop.mvp.pk.module.trade.domain.cart.CartFactory;
import com.develop.mvp.pk.module.trade.domain.cart.repository.CartRepository;
import com.develop.mvp.pk.module.trade.domain.cart.valueobject.CartId;
import com.develop.mvp.pk.module.trade.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.trade.enums.ErrorCodeConstants.*;

@Service
public class CartApplicationService {

    private final CartRepository cartRepository;
    private final DomainEventPublisher eventPublisher;

    public CartApplicationService(CartRepository cartRepository, DomainEventPublisher eventPublisher) {
        this.cartRepository = cartRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Cart addToCart(Long userId, Long spuId, Long skuId, Integer count) {
        Cart existing = cartRepository.findByUserIdAndSkuId(userId, skuId);
        if (existing != null) {
            existing.updateCount(existing.count() + count);
            cartRepository.save(existing);
            return existing;
        }
        return cartRepository.save(CartFactory.create(null, userId, spuId, skuId, count, true));
    }

    @Transactional
    public void updateCount(Long userId, Long skuId, Integer count) {
        Cart cart = cartRepository.findByUserIdAndSkuId(userId, skuId);
        if (cart == null) throw exception(CARD_ITEM_NOT_FOUND);
        cart.updateCount(count);
        cartRepository.save(cart);
    }

    @Transactional
    public void deleteCartItem(Long id) {
        cartRepository.delete(CartId.of(id));
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteSelected(Long userId) {
        cartRepository.deleteSelectedByUserId(userId);
    }

    public List<Cart> getUserCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart getCartItem(Long id) {
        return cartRepository.findById(CartId.of(id));
    }

    @Transactional
    public void selectAll(Long userId) { cartRepository.selectAllByUserId(userId); }

    @Transactional
    public void unselectAll(Long userId) { cartRepository.unselectAllByUserId(userId); }
}
