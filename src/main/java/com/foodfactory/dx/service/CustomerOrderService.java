package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.CustomerOrder;
import com.foodfactory.dx.domain.OrderLine;
import com.foodfactory.dx.mapper.CustomerOrderMapper;
import com.foodfactory.dx.mapper.OrderLineMapper;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerOrderService {

    private final CustomerOrderMapper customerOrderMapper;
    private final OrderLineMapper orderLineMapper;

    public CustomerOrderService(CustomerOrderMapper customerOrderMapper, OrderLineMapper orderLineMapper) {
        this.customerOrderMapper = customerOrderMapper;
        this.orderLineMapper = orderLineMapper;
    }

    public CustomerOrder createOrder(CustomerOrder order) {
        customerOrderMapper.insert(order);
        return order;
    }

    public List<CustomerOrder> listOrders() {
        return customerOrderMapper.findAll();
    }

    /**
     * 受注明細を1件登録する。
     * amount(金額)は、unitPriceが指定されていればqty×unitPriceをここで計算して保存する
     * (単価が後で変わっても、受注時点の金額を保持できるようにするため)。
     */
    public OrderLine createOrderLine(OrderLine line) {
        if (line.getUnitPrice() != null) {
            line.setAmount(line.getQty().multiply(line.getUnitPrice()));
        }
        orderLineMapper.insert(line);
        return line;
    }

    public List<OrderLine> listOrderLines(Long orderId) {
        return orderLineMapper.findByOrderId(orderId);
    }

    /** NEW → CONFIRMED。受注内容を確定する操作。 */
    public void confirmOrder(Long orderId) {
        CustomerOrder order = customerOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderId));
        if (order.getStatus() != CustomerOrder.Status.NEW) {
            throw new IllegalStateException("NEW状態の受注のみ確定できます。現在の状態: " + order.getStatus());
        }
        customerOrderMapper.updateStatus(orderId, CustomerOrder.Status.CONFIRMED);
    }

    /** キャンセル。在庫プール型の設計のため、ステータス変更のみで在庫やバッチには一切手を加えない。 */
    public void cancelOrder(Long orderId) {
        CustomerOrder order = customerOrderMapper.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("指定された受注が見つかりません: orderId=" + orderId));
        if (order.getStatus() == CustomerOrder.Status.COMPLETED
                || order.getStatus() == CustomerOrder.Status.CANCELLED) {
            throw new IllegalStateException("この受注はキャンセルできない状態です。現在の状態: " + order.getStatus());
        }
        customerOrderMapper.updateStatus(orderId, CustomerOrder.Status.CANCELLED);
    }
}
