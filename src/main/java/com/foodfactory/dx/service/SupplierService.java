package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Supplier;
import com.foodfactory.dx.mapper.SupplierMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    public List<Supplier> listSuppliers(Boolean active) {
        return supplierMapper.findByFilters(active);
    }

    public Supplier createSupplier(Supplier supplier) {
        supplierMapper.insert(supplier);
        return supplier;
    }

    public Supplier updateSupplier(Long supplierId, Supplier supplier) {
        supplierMapper.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("指定された仕入先が見つかりません: supplierId=" + supplierId));
        supplier.setSupplierId(supplierId);
        supplierMapper.update(supplier);
        return supplier;
    }

    /**
     * 仕入先を廃版(論理削除)にする。倒産・取引停止等でも過去の発注・入荷記録は
     * そのまま追跡できるよう、物理削除ではなく論理削除にしている。
     */
    public void deactivateSupplier(Long supplierId) {
        supplierMapper.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("指定された仕入先が見つかりません: supplierId=" + supplierId));
        supplierMapper.setActive(supplierId, false);
    }

    public void reactivateSupplier(Long supplierId) {
        supplierMapper.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("指定された仕入先が見つかりません: supplierId=" + supplierId));
        supplierMapper.setActive(supplierId, true);
    }
}
