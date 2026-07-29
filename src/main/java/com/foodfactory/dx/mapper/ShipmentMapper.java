package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.Shipment;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShipmentMapper {

    int insert(Shipment shipment);

    Optional<Shipment> findById(@Param("shipmentId") Long shipmentId);

    List<Shipment> findAll();
}
