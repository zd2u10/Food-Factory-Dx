package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.Customer;
import com.foodfactory.dx.mapper.CustomerMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;

    public CustomerService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public Customer createCustomer(Customer customer) {
        customerMapper.insert(customer);
        return customer;
    }

    public List<Customer> listCustomers() {
        return customerMapper.findAll();
    }

    public Customer updateCustomer(Long customerId, Customer customer) {
        customerMapper.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("指定された取引先が見つかりません: customerId=" + customerId));
        customer.setCustomerId(customerId);
        customerMapper.update(customer);
        return customer;
    }
}
