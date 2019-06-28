package com.fix.elasticsearch.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fix.domain.EsProduct;
import com.fix.domain.EsProductRelatedInfo;
import com.fix.elasticsearch.dao.EsProductDao;
import com.fix.elasticsearch.repository.EsProductRepository;
import com.fix.elasticsearch.service.EsProductService;

/**
 * 商品搜索管理Service实现类
 * Created by macro on 2018/6/19.
 */
@Service
public class EsProductServiceImpl implements EsProductService {
	private static Logger logger = LogManager.getLogger(EsProductServiceImpl.class);  
    @Autowired
    private EsProductDao productDao;
    @Resource
    private MyResultMapper myResultMapper;
    @Autowired
    private EsProductRepository productRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Override
    public int importAll() {
        List<EsProduct> esProductListOld = productDao.getAllEsProductList(null);
        List<EsProduct> esProductList =new ArrayList<>();
        for (int i = 0; i < esProductListOld.size(); i++) {
        	EsProduct evo = esProductListOld.get(i);
        	evo.setLocation(new GeoPoint(evo.getLat(), evo.getLnt()));
        	esProductList.add(evo);
		}
        Iterable<EsProduct> esProductIterable = productRepository.saveAll(esProductList);
        Iterator<EsProduct> iterator = esProductIterable.iterator();
        int result = 0;
        while (iterator.hasNext()) {
            result++;
            iterator.next();
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            result = productRepository.save(esProduct);
        }
        return result;
    }

    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            productRepository.deleteAll(esProductList);
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return productRepository.findByNameOrSubTitleOrKeywords(keyword, keyword, keyword, pageable);
    }

    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize,Integer sort) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        RangeQueryBuilder rangeQueryBuilder = null;
        BoolQueryBuilder boolQueryBuilder=null;
        rangeQueryBuilder = QueryBuilders.rangeQuery("id").from(26).to(30);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //分页
        nativeSearchQueryBuilder.withPageable(pageable);
        //过滤
        if (brandId != null || productCategoryId != null) {
        	boolQueryBuilder= QueryBuilders.boolQuery();
            
            if (brandId != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandId", brandId));
            }
            if (productCategoryId != null) {
                boolQueryBuilder.must(QueryBuilders.termQuery("productCategoryId", productCategoryId));
                //类别名称包含手机的字段
                boolQueryBuilder.must(QueryBuilders.matchQuery("productSn","6946605"));
            }
            nativeSearchQueryBuilder.withFilter(boolQueryBuilder).withFilter(rangeQueryBuilder);
        }else{
        	boolQueryBuilder= QueryBuilders.boolQuery();
        	boolQueryBuilder.filter(rangeQueryBuilder);
        	nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        }
        
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        distanceQueryBuilder.point(31.208496,121.633021);
        distanceQueryBuilder.distance(50, DistanceUnit.KILOMETERS);
    	boolQueryBuilder.must(distanceQueryBuilder);
    	nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
            GeoDistanceSortBuilder sortBuilder =SortBuilders.geoDistanceSort("location",31.208496,121.633021)
                    .unit(DistanceUnit.KILOMETERS)
                    .order(SortOrder.ASC).geoDistance(GeoDistance.ARC);
   
    	nativeSearchQueryBuilder.withSort(sortBuilder);
        
        //搜索
        if (StringUtils.isEmpty(keyword)) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<FunctionScoreQueryBuilder.FilterFunctionBuilder>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(10)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(5)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(builders);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                    .setMinScore(2);
            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
        }
        //排序
        if(sort==1){
            //按新品从新到旧
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));
        }else if(sort==2){
            //按销量从高到低
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("sale").order(SortOrder.DESC));
        }else if(sort==3){
            //按价格从低到高
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        }else if(sort==4){
            //按价格从高到低
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        }else{
            //按相关度
            nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        }
        nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
        
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        logger.info("DSL:{}", searchQuery.getQuery().toString());
       /* Page<EsProduct> page =
                elasticsearchTemplate.queryForPage(
                		nativeSearchQueryBuilder.build(), EsProduct.class);
        elasticsearchTemplate.getClient().prepareSearch(nativeSearchQueryBuilder.build());*/
        return productRepository.search(searchQuery);
    }

    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            String keyword = esProduct.getName();
            Long brandId = esProduct.getBrandId();
            Long productCategoryId = esProduct.getProductCategoryId();
            //根据商品标题、品牌、分类进行搜索   权重查询Weight
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<FunctionScoreQueryBuilder.FilterFunctionBuilder>();
            //第一个filter(使用weight加强函数)
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(8)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
                    ScoreFunctionBuilders.weightFactorFunction(2)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("brandId", brandId),
                    ScoreFunctionBuilders.weightFactorFunction(10)));
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("productCategoryId", productCategoryId),
                    ScoreFunctionBuilders.weightFactorFunction(6)));
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(builders);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)//设置functions里面的加强score们怎麽合併成一个总加强score
                    .setMinScore(2);
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            builder.withQuery(functionScoreQueryBuilder);
            builder.withPageable(pageable);
            NativeSearchQuery searchQuery = builder.build();
            logger.info("DSL:{}", searchQuery.getQuery().toString());
            return productRepository.search(searchQuery);
        }
        return new PageImpl<>(null);
    }

    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //搜索条件
        if(StringUtils.isEmpty(keyword)){
            builder.withQuery(QueryBuilders.matchAllQuery());
        }else{
            builder.withQuery(QueryBuilders.multiMatchQuery(keyword,"name","subTitle","keywords"));
        }
        //聚合搜索品牌名称
        builder.addAggregation(AggregationBuilders.terms("brandNames").field("brandName"));
        //集合搜索分类名称
        builder.addAggregation(AggregationBuilders.terms("productCategoryNames").field("productCategoryName"));
        //聚合搜索商品属性，去除type=1的属性
        AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.nested("allAttrValues","attrValueList")
                .subAggregation(AggregationBuilders.filter("productAttrs",QueryBuilders.termQuery("attrValueList.type",1))
                .subAggregation(AggregationBuilders.terms("attrIds")
                        .field("attrValueList.productAttributeId")
                        .subAggregation(AggregationBuilders.terms("attrValues")
                                .field("attrValueList.value"))
                        .subAggregation(AggregationBuilders.terms("attrNames")
                                .field("attrValueList.name"))));
        builder.addAggregation(aggregationBuilder);
        NativeSearchQuery searchQuery = builder.build();
        return elasticsearchTemplate.query(searchQuery, response -> {
        	logger.info("DSL:{}",searchQuery.getQuery().toString());
            return convertProductRelatedInfo(response);
        });
    }
    /**
     * 将返回结果转换为对象
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchResponse response) {
        EsProductRelatedInfo productRelatedInfo = new EsProductRelatedInfo();
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        //设置品牌
        Aggregation brandNames = aggregationMap.get("brandNames");
        List<String> brandNameList = new ArrayList<>();
        for(int i = 0; i<((Terms) brandNames).getBuckets().size(); i++){
            brandNameList.add(((Terms) brandNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setBrandNames(brandNameList);
        //设置分类
        Aggregation productCategoryNames = aggregationMap.get("productCategoryNames");
        List<String> productCategoryNameList = new ArrayList<>();
        for(int i=0;i<((Terms) productCategoryNames).getBuckets().size();i++){
            productCategoryNameList.add(((Terms) productCategoryNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setProductCategoryNames(productCategoryNameList);
        //设置参数
        Aggregation productAttrs = aggregationMap.get("allAttrValues");
        List<LongTerms.Bucket> attrIds = ((LongTerms) ((InternalFilter) ((InternalNested) productAttrs).getProperty("productAttrs")).getProperty("attrIds")).getBuckets();
        List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();
        for (Terms.Bucket attrId : attrIds) {
            EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
            attr.setAttrId((Long) attrId.getKey());
            List<String> attrValueList = new ArrayList<>();
            List<StringTerms.Bucket> attrValues = ((StringTerms) attrId.getAggregations().get("attrValues")).getBuckets();
            List<StringTerms.Bucket> attrNames = ((StringTerms) attrId.getAggregations().get("attrNames")).getBuckets();
            for (Terms.Bucket attrValue : attrValues) {
                attrValueList.add(attrValue.getKeyAsString());
            }
            attr.setAttrValues(attrValueList);
            if(!CollectionUtils.isEmpty(attrNames)){
                String attrName = attrNames.get(0).getKeyAsString();
                attr.setAttrName(attrName);
            }
            attrList.add(attr);
        }
        productRelatedInfo.setProductAttrs(attrList);
        return productRelatedInfo;
    }

	@Override
	public Page<EsProduct> searchDistance(String keyword, Long brandId, Long productCategoryId, Integer pageNum,
			Integer pageSize, Integer sort) {
		
		    Pageable pageable = PageRequest.of(pageNum, pageSize);
	        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
	        //分页
	        nativeSearchQueryBuilder.withPageable(pageable);
	        //过滤
	        if (brandId != null || productCategoryId != null) {
	        	BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
	            if (brandId != null) {
	                boolQueryBuilder.must(QueryBuilders.termQuery("brandId", brandId));
	            }
	            if (productCategoryId != null) {
	                boolQueryBuilder.must(QueryBuilders.termQuery("productCategoryId", productCategoryId));
	            }
	            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
	        }
	        
	        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
	        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location").point(31.208496,121.633021).distance(5000, DistanceUnit.KILOMETERS);
	    	boolQueryBuilder.must(distanceQueryBuilder);
	    	nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
	    	
	        GeoDistanceSortBuilder sortBuilder =SortBuilders.geoDistanceSort("location",31.208496,121.633021).point(31.208496,121.633021).unit(DistanceUnit.KILOMETERS).order(SortOrder.ASC);
	    	nativeSearchQueryBuilder.withSort(sortBuilder);
	        
	        //搜索
	        if (StringUtils.isEmpty(keyword)) {
	            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
	        } else {
	            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
	            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("name", keyword),
	                    ScoreFunctionBuilders.weightFactorFunction(10)));
	            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("subTitle", keyword),
	                    ScoreFunctionBuilders.weightFactorFunction(5)));
	            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery("keywords", keyword),
	                    ScoreFunctionBuilders.weightFactorFunction(2)));
	            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
	            filterFunctionBuilders.toArray(builders);
	            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
	                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
	                    .setMinScore(2);
	            nativeSearchQueryBuilder.withQuery(functionScoreQueryBuilder);
	        }
	        //排序
	        if(sort==1){
	            //按新品从新到旧
	            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));
	        }else if(sort==2){
	            //按销量从高到低
	            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("sale").order(SortOrder.DESC));
	        }else if(sort==3){
	            //按价格从低到高
	            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
	        }else if(sort==4){
	            //按价格从高到低
	            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
	        }else{
	            //按相关度
	            nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
	        }
	        nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
	        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
	        logger.info("DSL:{}", searchQuery.getQuery().toString());
	        return productRepository.search(searchQuery);
	}

	@Override
	public EsProductRelatedInfo minmax(String keyword) {
		
		//简单查询模板
		SearchRequestBuilder request = elasticsearchTemplate.getClient().prepareSearch("pms")
                .setTypes("product")
                .setQuery(QueryBuilders.matchQuery("productCategoryName","T恤"))
                //.setQuery(QueryBuilders.termQuery("productSn","HNTBJ2E080A"))
                .addAggregation(
                        AggregationBuilders.terms("brandNames").field("brandName")
                        .subAggregation(AggregationBuilders.avg("avg_brandId").field("brandId"))
                        .subAggregation(AggregationBuilders.sum("sum_price").field("price"))
                        .subAggregation(AggregationBuilders.min("min_price").field("price"))
                        .subAggregation(AggregationBuilders.max("max_price").field("price"))
                        .subAggregation(AggregationBuilders.count("count_price").field("price"))
                ).setExplain(true);
        SearchResponse response = request.get();
        Aggregations aggs = response.getAggregations();
        Map<String,Aggregation> map= aggs.asMap();
        Set<String> set = map.keySet();
        for (String str : set) {
            System.out.println("agg name="+str);
            Aggregation agg = map.get(str);
            Map<String,Object> data = agg.getMetaData();
            Set<String> dataSet = map.keySet();
            for (String str2 : dataSet) {
                StringTerms obj = (StringTerms) map.get(str2);
                System.out.println("DocCountError="+obj.getDocCountError());
                System.out.println("SumOfOtherDocCounts="+obj.getSumOfOtherDocCounts());
                List<Bucket> buckes = obj.getBuckets();
                for (Iterator iterator = buckes.iterator(); iterator.hasNext();) {
                    Bucket bucket = (Bucket) iterator.next();
                    String key = bucket.getKeyAsString();
                    List<Aggregation> list = bucket.getAggregations().asList();
                    System.out.println(key+"="+bucket.getDocCount()+".."+JSON.toJSONString(list));
                }
            }
        } 
		return null;
	}
	@Override
	public EsProductRelatedInfo searchModel(String keyword) {
		RangeQueryBuilder rangeBuilder = new RangeQueryBuilder("logtime").gte(""+"T00:00:00").lte(""+"T23:59:59").timeZone("+08:00");
		BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		booleanQueryBuilder.mustNot(QueryBuilders.matchPhraseQuery("id", "3086")).must(QueryBuilders.matchPhraseQuery("price", "3087"))
		.must(QueryBuilders.termQuery("is_steal", "1")).filter(rangeBuilder)
		;
		SearchRequestBuilder b = elasticsearchTemplate.getClient().prepareSearch("pms")
        .setTypes("product").setQuery(booleanQueryBuilder);
		 SearchResponse response = b.get();
		SearchHits s =  response.getHits();
		return null;
	}

	@Override
	public AggregatedPage<EsProduct> searchHighLight(String keyword, Integer pageNum, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum, pageSize);

        String preTag = "<font color='#dd4b39'>";//google的色值
        String postTag = "</font>";

        SearchQuery searchQuery = new NativeSearchQueryBuilder().
                withQuery(QueryBuilders.matchQuery("productCategoryName", "T恤")).
               // withQuery(QueryBuilders.matchQuery("productCategoryId", "9")).
                withHighlightFields(new HighlightBuilder.Field("productCategoryName").preTags(preTag).postTags(postTag)
                        // ,new HighlightBuilder.Field("productCategoryId").preTags(preTag).postTags(postTag)
                		)
                .build();
        searchQuery.setPageable(pageable);
        
        // 不需要高亮直接return ideas 
         AggregatedPage<EsProduct> ideas = elasticsearchTemplate.queryForPage(searchQuery, EsProduct.class);
         // 高亮字段
        AggregatedPage<EsProduct> ideasq = elasticsearchTemplate.queryForPage(searchQuery, EsProduct.class, new SearchResultMapper() {

            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                List<EsProduct> chunk = new ArrayList<>();
                for (SearchHit searchHit : response.getHits()) {
                    if (response.getHits().getHits().length <= 0) {
                        return null;
                    }
                    EsProduct idea = new EsProduct();
                    //name or memoe
                    HighlightField ideaTitle = searchHit.getHighlightFields().get("productCategoryName");
                    if (ideaTitle != null) {
                        idea.setBrandName(ideaTitle.fragments()[0].toString());
                    }
                    HighlightField ideaContent = searchHit.getHighlightFields().get("productCategoryId");
                    if (ideaContent != null) {
                        idea.setProductCategoryName(ideaTitle.fragments()[0].toString());
                    }
                    chunk.add(idea);
                }
                if (chunk.size() > 0) {
                    return new AggregatedPageImpl<>((List<T>) chunk);
                }
                return null;
            }
        });
        return ideasq;
	}
	
	
	@Override
	public Page<EsProduct> searchSimoles(String keyword, Integer pageNum, Integer pageSize) {
		 // 构建查询条件
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 添加基本分词查询
	    queryBuilder.withQuery(QueryBuilders.matchQuery("title", "小米手机"));
	    // 搜索，获取结果
	    Page<EsProduct> items = this.productRepository.search(queryBuilder.build());
	    // 总条数
	    long total = items.getTotalElements();
	    System.out.println("total = " + total);
	    for (EsProduct item : items) {
	        System.out.println(item);
	    }
	    return items;
	}
	public void searchByPage(){
	    // 构建查询条件
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 添加基本分词查询
	    queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));
	    // 分页：
	    int page = 0;
	    int size = 2;
	    queryBuilder.withPageable(PageRequest.of(page,size));

	    // 搜索，获取结果
	    Page<EsProduct> items = this.productRepository.search(queryBuilder.build());
	    // 总条数
	    long total = items.getTotalElements();
	    System.out.println("总条数 = " + total);
	    // 总页数
	    System.out.println("总页数 = " + items.getTotalPages());
	    // 当前页
	    System.out.println("当前页：" + items.getNumber());
	    // 每页大小
	    System.out.println("每页大小：" + items.getSize());

	    for (EsProduct item : items) {
	        System.out.println(item);
	    }
	}

	public void searchAndSort(){
	    // 构建查询条件
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 添加基本分词查询
	    queryBuilder.withQuery(QueryBuilders.termQuery("category", "手机"));

	    // 排序
	    queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));

	    // 搜索，获取结果
	    Page<EsProduct> items = this.productRepository.search(queryBuilder.build());
	    // 总条数
	    long total = items.getTotalElements();
	    System.out.println("总条数 = " + total);

	    for (EsProduct item : items) {
	        System.out.println(item);
	    }
	}
	/**
	 * 聚合为桶
	 */
	public void testAgg(){
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 不查询任何结果
	    queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
	    // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
	    queryBuilder.addAggregation(
	            AggregationBuilders.terms("brands").field("brand"));
	    // 2、查询,需要把结果强转为AggregatedPage类型
	    AggregatedPage<EsProduct> aggPage = (AggregatedPage<EsProduct>) this.productRepository.search(queryBuilder.build());
	    // 3、解析
	    // 3.1、从结果中取出名为brands的那个聚合，
	    // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
	    StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
	    // 3.2、获取桶
	    List<StringTerms.Bucket> buckets = agg.getBuckets();
	    // 3.3、遍历
	    for (StringTerms.Bucket bucket : buckets) {
	        // 3.4、获取桶中的key，即品牌名称
	        System.out.println(bucket.getKeyAsString());
	        // 3.5、获取桶中的文档数量
	        System.out.println(bucket.getDocCount());
	    }
	}

	/**
	 * 嵌套聚合，求平均值
	 */

	public void testSubAgg(){
	    NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
	    // 不查询任何结果
	    queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
	    // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
	    queryBuilder.addAggregation(
	            AggregationBuilders.terms("brands").field("brand")
	                    .subAggregation(AggregationBuilders.avg("priceAvg").field("price")) // 在品牌聚合桶内进行嵌套聚合，求平均值
	    );
	    // 2、查询,需要把结果强转为AggregatedPage类型
	    AggregatedPage<EsProduct> aggPage = (AggregatedPage<EsProduct>) this.productRepository.search(queryBuilder.build());
	    // 3、解析
	    // 3.1、从结果中取出名为brands的那个聚合，
	    // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
	    StringTerms agg = (StringTerms) aggPage.getAggregation("brands");
	    // 3.2、获取桶
	    List<StringTerms.Bucket> buckets = agg.getBuckets();
	    // 3.3、遍历
	    for (StringTerms.Bucket bucket : buckets) {
	        // 3.4、获取桶中的key，即品牌名称  3.5、获取桶中的文档数量
	        System.out.println(bucket.getKeyAsString() + "，共" + bucket.getDocCount() + "台");

	        // 3.6.获取子聚合结果：
	        InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("priceAvg");
	        System.out.println("平均售价：" + avg.getValue());
	    }

	}
}
