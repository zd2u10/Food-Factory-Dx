package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Customer;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {

    int insert(Customer customer);

    Optional<Customer> findById(@Param("customerId") Long customerId);

    List<Customer> findAll();

    int update(Customer customer);
}
