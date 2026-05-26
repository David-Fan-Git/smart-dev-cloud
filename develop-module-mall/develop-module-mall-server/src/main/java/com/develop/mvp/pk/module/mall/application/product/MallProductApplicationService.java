package com.develop.mvp.pk.module.mall.application.product;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mall.domain.product.MallProduct;
import com.develop.mvp.pk.module.mall.domain.product.repository.MallProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class MallProductApplicationService {
    private final MallProductRepository repo;
    @Transactional public Long create(String name, Long categoryId, Integer price, Integer stock, Integer status) { var p = MallProduct.of(null, name).categoryId(categoryId).price(price).stock(stock).status(status); repo.save(p); return p.id(); }
    @Transactional public void update(Long id, String name, Long categoryId, Integer price, Integer stock, Integer status) { repo.save(MallProduct.of(id, name).categoryId(categoryId).price(price).stock(stock).status(status)); }
    @Transactional public void delete(Long id) { repo.delete(id); }
    public MallProduct get(Long id) { return repo.findById(id); }
    public PageResult<MallProduct> getPage(String name, Long categoryId, Integer status, Integer pageNo, Integer pageSize) { return repo.findPage(name, categoryId, status, pageNo, pageSize); }
}
