package com.es.elasticsearch.service;

import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;

import com.es.document.EsProduct;
import com.es.document.EsProductRelatedInfo;

import java.util.List;

/**
 * 商品搜索管理Service
 * Created by wangyt on 2019/06/29.
 */
public interface EsProductService {
    /**
     * 从数据库中导入所有商品到ES
     */
    int importAll();

    /**
     * 根据id删除商品
     */
    void delete(Long id);

    /**
     * 根据id创建商品
     */
    EsProduct create(Long id);

    /**
     * 批量删除商品
     */
    void delete(List<Long> ids);

    /**
     * 根据关键字搜索名称或者副标题
     */
    Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据关键字搜索名称或者副标题复合查询
     */
    Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize,Integer sort);

    /**
     * 根据商品id推荐相关商品
     */
    Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize);

    /**
     * 获取搜索词相关品牌、分类、属性
     */
    EsProductRelatedInfo searchRelatedInfo(String keyword);

	Page<EsProduct> searchDistance(String keyword, Long brandId, Long productCategoryId, Integer pageNum,
			Integer pageSize, Integer sort);

	EsProductRelatedInfo minmax(String keyword);

	EsProductRelatedInfo searchModel(String keyword);

	AggregatedPage<EsProduct> searchHighLight(String keyword, Integer pageNum, Integer pageSize);

	Page<EsProduct> searchSimoles(String keyword, Integer pageNum, Integer pageSize);

	Page<EsProduct> searchHighLightNew(String keyword, Integer pageNum, Integer pageSize);
}
