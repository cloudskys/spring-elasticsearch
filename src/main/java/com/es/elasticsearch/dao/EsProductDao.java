package com.es.elasticsearch.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.es.document.EsProduct;




/**
 * 搜索系统中的商品管理自定义Dao
 * Created by wangyt on 2019/06/29.
 */
public interface EsProductDao {
    List<EsProduct> getAllEsProductList(@Param("id") Long id);
}
