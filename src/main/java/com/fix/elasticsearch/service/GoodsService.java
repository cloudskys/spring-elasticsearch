package com.fix.elasticsearch.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fix.elasticsearch.dao.Goods;
import com.fix.elasticsearch.dao.GoodsDao;

@Service
public class GoodsService {
    @Autowired
    private GoodsDao esDao;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
 
    public Goods findBySkuNo(String skuNo) {
        return esDao.findBySkuNo(skuNo);
    }
 
    public Goods findByUdateTime(String udateTime) {
        return esDao.findByUdateTime(udateTime);
    }
    public void save(Goods GoodsVo) {
    	 esDao.save(GoodsVo);
    }
    public java.util.List<Goods> findBySkuNoList(String skuNo) {
        return esDao.findBySkuNoList(skuNo);
    }

	public void delete(Goods skuNo) {
		esDao.delete(skuNo);
	}

	public Page<Goods> findAll(Pageable pageable) {
		// TODO Auto-generated method stub
		return esDao.findAll(pageable);
	}

	public Page<Goods> findByUpdate(String updateTime, Pageable pageable) {
		// TODO Auto-generated method stub
		return esDao.findByUpdateTime(updateTime,pageable);
	}

	public Page<Goods> findByIdAndUpdateTime(int id, String updateTime,Pageable pageable) {
		// TODO Auto-generated method stub
		return esDao.findByIdAndUpdateTime(id, updateTime,pageable);
	}

	public Page<Goods> searchBuilder(int id, String updateTime,Pageable pageable) {
		QueryBuilder queryBuilder= QueryBuilders.boolQuery()
	            .must(QueryBuilders.matchQuery("id",id))
	            .must(QueryBuilders.matchQuery("updateTime",updateTime));
	    SearchQuery searchQuery = new NativeSearchQueryBuilder()
	            .withQuery(queryBuilder)
	            .build();
	    return   esDao.search(searchQuery);
	}

	public List<Goods> searchtemplete(int id, String updateTime, Pageable pageable) {
		QueryBuilder queryBuilder= QueryBuilders.boolQuery()
	            .must(QueryBuilders.matchQuery("id",id))
	            .must(QueryBuilders.matchQuery("updateTime",updateTime));

	    SearchQuery searchQuery = new NativeSearchQueryBuilder()
	            .withQuery(queryBuilder)
	            .build();
	    return elasticsearchTemplate.queryForList(searchQuery,Goods.class);
	}

	public Map getEsCashSummaryInfo(int id) {
		/*SumAggregationBuilder sb = AggregationBuilders.sum("tpPrice").field("id");
        BoolQueryBuilder bqb = QueryBuilders.boolQuery();
        bqb.must(QueryBuilders.termQuery("updateTime.keyword","25s"));
        bqb.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("sku_no.keyword", 
        		"9elasticSearch 3.0版本发布")));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(bqb).withIndices("aa").withTypes("bb")
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(sb).build();
        Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });
        Sum _sum = aggregations.get("tpPrice");
        if(_sum != null){
            System.out.println("sum="+_sum.getValue());
        }
        return null;*/
		/*AggregationBuilder sb = AggregationBuilders.terms("tpPrice").field("id").size(0);
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withIndices("aa").withTypes("bb")
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(sb).build();*/
		
		QueryBuilder queryBuilder = QueryBuilders.boolQuery()
	            .must(QueryBuilders.termQuery("updateTime", "25s"))
	            .mustNot(QueryBuilders.termQuery("id", "2"))
	            .should(QueryBuilders.termQuery("id", "44"));
		HighlightBuilder hiBuilder=new HighlightBuilder();
		hiBuilder.preTags("<h2>");
        hiBuilder.postTags("</h2>");
        hiBuilder.field("sku_no");
        SearchResponse  searchRequestBuilder = elasticsearchTemplate.getClient().prepareSearch("aa").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setScroll(new TimeValue(60000))
		        .setQuery(queryBuilder).highlighter(hiBuilder)
		        .execute().actionGet();
		//SearchResponse response =searchRequestBuilder.get(); elasticsearchTemplate.getClient().prepareSearch("aa")
        SearchHits  response = searchRequestBuilder.getHits();
		for (SearchHit hit : response.getHits()) {
			/*Map<String, HighlightField> highlightFields = hit.getHighlightFields();
			System.out.println("highlightFields:"+JSON.toJSONString(highlightFields));
			Map<String, Object> source = hit.getSourceAsMap();
			int idS = Integer.parseInt(source.get("id").toString());
			String sku_no = source.get("sku_no").toString();
			System.out.println("idS:"+idS+",sku_no"+sku_no);*/
			 System.out.println("String方式打印文档搜索内容:");
	            System.out.println(hit.getSourceAsString());
	            System.out.println("Map方式打印高亮内容");
	            System.out.println(hit.getHighlightFields());

	            System.out.println("遍历高亮集合，打印高亮片段:");
	            Text[] text = hit.getHighlightFields().get("title").getFragments();
	            for (Text str : text) {
	                System.out.println(str.string());
	            }
		}
		return  null;
    }

	public void search() {
		    QueryBuilder matchQuery = QueryBuilders.matchQuery("sku_no", "2");
	        HighlightBuilder hiBuilder=new HighlightBuilder();
	        hiBuilder.preTags("<h2>");
	        hiBuilder.postTags("</h2>");
	        hiBuilder.field("title");
	        // 搜索数据
	        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch("aa")
	                .setQuery(matchQuery)
	                .highlighter(hiBuilder)
	                .execute().actionGet();
	        //获取查询结果集
	        SearchHits searchHits = response.getHits();
	        System.out.println("共搜到:"+searchHits.getTotalHits()+"条结果!");
	        //遍历结果
	        for(SearchHit hit:searchHits){
	            System.out.println("String方式打印文档搜索内容:");
	            System.out.println(hit.getSourceAsString());
	            System.out.println("Map方式打印高亮内容");
	            System.out.println(hit.getHighlightFields());

	            System.out.println("遍历高亮集合，打印高亮片段:");
	            Text[] text = hit.getHighlightFields().get("title").getFragments();
	            for (Text str : text) {
	                System.out.println(str.string());
	            }
	        }
	    }
	public List<Goods> searchOrder(Goods request) throws Exception{
        List<Goods> woSearchModels = new ArrayList<>();
 
        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags(EagleConst.PRE_TAGS);
        highlightBuilder.postTags(EagleConst.POST_TAGS);
 
        //搜索title和orperator和detail
        //TODO: 搜索项目名称
        QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder(request.getKeyWord());
        queryBuilder.analyzer(EagleConst.ES_ANALYSER);
        queryBuilder.field(EagleConst.ORDER_TITLE).field(EagleConst.ORDER_OPERATOR).field(EagleConst.ORDER_DETAIL);
 
        //搜索
        SearchRequestBuilder searchRequestBuilder = elasticsearchTemplate.getClient().prepareSearch(EagleConst.INDEX)
                .setFrom(request.getStart())
                .setSize(request.getSize())
                .setTypes(EagleConst.TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //.setQuery(builder)
                .setQuery(queryBuilder)
                .highlighter(highlightBuilder)
                .setExplain(true); //设置是否按查询匹配度排序
        SearchResponse searchResponse = searchRequestBuilder.get();
 
        //获取搜索结果
        SearchHits searchHits = searchResponse.getHits();
 
        SearchHit[] hits = searchHits.getHits();
        if (hits != null && hits.length != 0){
            for (SearchHit hit : hits) {
                List<String> highLights = new ArrayList<>();
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                Map<String, Object> source = hit.getSource();
                //处理高亮 获取高亮字符串
                if (highlightFields != null && highlightFields.size() != 0){
                    String[] needHighLightFields = new String[]{EagleConst.ORDER_TITLE, EagleConst.ORDER_DETAIL, EagleConst.ORDER_OPERATOR};
                    for (String needHighLightField : needHighLightFields){
                        HighlightField titleField = highlightFields.get(needHighLightField);
                        if(titleField != null){
                            Text[] fragments = titleField.fragments();
                            if (fragments != null && fragments.length != 0){
                                StringBuilder name = new StringBuilder();
                                for (Text text : fragments) {
                                    name.append(text);
                                }
                                source.put(needHighLightField, name.toString());
                                highLights.add(needHighLightField + ":" + name.toString());
                            }
                        }
                    }
                }
                WOSearchModel woSearchModel = new WOSearchModel();
                woSearchModel.setHighLightTexts(highLights);
                OrderModel orderModel = new OrderModel();
                BeanUtils.populate(orderModel, source);
                woSearchModel.setModel(orderModel);
                StringBuilder urlStringBuilder = new StringBuilder();
                urlStringBuilder.append(EagleConst.URL_PRE).append("ids=").append(orderModel.getId())
                        .append("&tenantId=").append(orderModel.getTenantId()).append("&projectId=").append(orderModel.getProjectId());
                woSearchModel.setUrl(urlStringBuilder.toString());
                woSearchModels.add(woSearchModel);
            }
        }
 
        return woSearchModels;

	}
	
	
}
