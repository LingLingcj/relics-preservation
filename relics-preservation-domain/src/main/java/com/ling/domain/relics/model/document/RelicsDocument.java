package com.ling.domain.relics.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
* @Author: LingRJ
* @Description: 文物es文档
* @DateTime: 2025/7/4 10:47
**/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "relics")
public class RelicsDocument {

    @Id
    private String relicsId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Integer)
    private Integer preservation;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String era;

    @Field(type = FieldType.Keyword)
    private String material;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Integer)
    private Integer locationId;
}
