package com.foodfactory.dx.mapper;

import com.foodfactory.dx.domain.MrpRun;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MrpRunMapper {

    int insert(MrpRun run);

    List<MrpRun> findAll();
}
