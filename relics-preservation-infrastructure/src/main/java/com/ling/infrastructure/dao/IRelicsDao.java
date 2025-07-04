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

    List<Relics> selectByEra(@Param("era") String era);
    
    /**
     * 随机获取指定数量的文物记录
     * @param limit 获取数量
     * @return 文物列表
     */
    List<Relics> selectRandomRelics(@Param("limit") int limit);

    Relics selectRelicById(@Param("id") Long id);
    
    /**
     * 获取除指定朝代外的所有文物
     * @param excludeEras 要排除的朝代列表
     * @return 文物列表
     */
    List<Relics> selectRelicsExceptEras(@Param("excludeEras") List<String> excludeEras);
    
    /**
     * 根据名称模糊查询文物
     * @param name 名称关键词
     * @return 文物列表
     */
    List<Relics> selectByNameContaining(@Param("name") String name);
    
    /**
     * 获取所有文物
     * @return 文物列表
     */
    List<Relics> selectAll();
}
