package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Carrier;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CarrierMapper {

    int insert(Carrier carrier);

    Optional<Carrier> findById(@Param("carrierId") Long carrierId);

    List<Carrier> findAll();
}
