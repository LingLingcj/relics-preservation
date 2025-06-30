package com.ling.infrastructure.dao;

import com.ling.infrastructure.dao.po.Relics;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 文物Dao
 * @DateTime: 2025/6/27 23:57
 **/
@Mapper
public interface IRelicsDao {
    @Insert("INSERT INTO relics (relics_id, name, description, preservation, category, era, material, image_url, status, location_id, create_time, update_time) " +
            "VALUES (#{relics.relicsId}, #{relics.name}, #{relics.description}, #{relics.preservation}, #{relics.category}, #{relics.era}, #{relics.material}, #{relics.imageUrl}, #{relics.status}, #{relics.locationId}, NOW(), NOW())")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "relics.id", before = false, resultType = int.class)
    int insertRelics(@Param("relics") Relics relics);

    @Select("SELECT * FROM relics WHERE era = #{era}")
    List<Relics> selectByEra(@Param("era") String era);
}
