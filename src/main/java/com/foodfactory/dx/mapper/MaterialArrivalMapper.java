package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MaterialArrival;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MaterialArrivalMapper {

    int insert(MaterialArrival arrival);

    Optional<MaterialArrival> findById(@Param("arrivalId") Long arrivalId);

    List<MaterialArrival> findAll();
}
