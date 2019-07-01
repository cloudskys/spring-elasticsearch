package com.controller;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.es.elasticsearch.repository.EsProductRepository;
import com.query.BookQuery;

@Controller
@RequestMapping("/orgin")
public class RepositoryController {
	@Autowired
	private EsProductRepository productRepository;

	public void findBook() {

		BookQuery query = new BookQuery();
		query.setQueryString("魔");
		query.setSite(2);// 1 是男生 2 是女生
		query.setSort(29); // 29 是玄幻
		query.setVip(true);// 查询 vip 作品
		query.setWordsBegin(0); // 查询字数在 0-25w 之间的作品
		query.setWordsEnd(500000);
		query.setPage(1);// 分页页码
		query.setSize(10);// 每页显示数

		// 复合查询
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// 以下为查询条件, 使用 must query 进行查询组合
		MultiMatchQueryBuilder matchQuery = QueryBuilders.multiMatchQuery(query.getQueryString(), "name", "intro",
				"author");
		boolQuery.must(matchQuery);

		// 以下为过滤筛选条件，使用 filter 比使用 must query 性能要好
		TermQueryBuilder siteQuery = QueryBuilders.termQuery("site", query.getSite());
		boolQuery.filter(siteQuery);
		TermQueryBuilder sortQuery = QueryBuilders.termQuery("sort", query.getSort());
		boolQuery.filter(sortQuery);
		TermQueryBuilder vipQuery = QueryBuilders.termQuery("vip", query.getVip());
		boolQuery.filter(vipQuery);
		RangeQueryBuilder wordsQuery = QueryBuilders.rangeQuery("words").gt(query.getWordsBegin())
				.lt(query.getWordsEnd());
		boolQuery.filter(wordsQuery);

		// 分页 同时根据 点击数 click 进行降序排列
		Pageable pageable = PageRequest.of(query.getPage(), query.getSize());
		NativeSearchQueryBuilder nativesearch = new NativeSearchQueryBuilder().withQuery(matchQuery)
				.withPageable(pageable).withSort(SortBuilders.fieldSort("name").order(SortOrder.DESC));
		// log.info("{}", boolQuery); // 打印出查询 json
		productRepository.search(nativesearch.build());
	}
}
