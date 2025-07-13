package com.ling.domain.interaction.model.entity;

import com.ling.domain.interaction.model.valobj.RelicsComment;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: LingRJ
 * @Description: 文物聚合根
 * @DateTime: 2025/7/12 5:08
 **/
@Getter
@Builder
public class RelicsAggregate {

    private final Long relicsId;
    private final List<RelicsComment> comments = new ArrayList<>();

}
