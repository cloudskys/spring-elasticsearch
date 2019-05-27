package com.fix.elasticsearch.dao;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
 
public interface GoodsDao extends ElasticsearchRepository<Goods, String> {
 
    /**
     * 根据skuNo查询商品
     * 此处必须有 @Query指定查询字段名称：sku_no，否则报错
     */
    @Query("{\"bool\" : {\"must\" : {\"term\" : {\"sku_no\" : \"?0\"}}}}")
    Goods findBySkuNo(String skuNo);
    
    @Query("{\"bool\" : {\"must\" : {\"term\" : {\"sku_no\" : \"?0\"}}}}")
    List<Goods> findBySkuNoList(String skuNo);
 
    /**
     * 功能描述：根据udateTime查询商品
     */
    Goods findByUdateTime(String udateTime);
    Goods findByUdateTimeAndId(String udateTime);

	Page<Goods> findByUpdateTime(String updateTime, Pageable pageable);
	
	@Query("{\"bool\": {\"must\": [{ \"match\": { \"newsTitle\": \"?0\"}},{ \"match\": { \"newsCate\": \"?1\"}}]}}")
	List<Goods> findByQuery(String newsTitle, String newsCate);
	
	Page<Goods> findByIdAndUpdateTime(int id,String updateTime, Pageable pageable);
}

