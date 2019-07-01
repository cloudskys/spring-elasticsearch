package com.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.es.document.EsProduct;
import com.es.elasticsearch.service.EsProductService;

/**
 * 高亮查询 Created by wangyt on 2019/07/01.
 */
@Controller
@RequestMapping("/esHighLight")
public class EsHighLightProductController {

	private static Logger logger = LogManager.getLogger(EsHighLightProductController.class);

	@Autowired
	private EsProductService esProductService;

	@RequestMapping(value = "/search/searchHighLight", method = RequestMethod.GET)
	public ModelAndView searchHighLight(@RequestParam(required = false) String keyword,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		ModelAndView model = new ModelAndView();
		AggregatedPage<EsProduct> esProductPage = esProductService.searchHighLight(keyword, pageNum, pageSize);
		System.out.println(JSON.toJSONString(esProductPage));
		model.addObject("esProductPage", esProductPage.getContent().get(0).getBrandName());
		model.setViewName("searchHighLight");
		return model;
	}

	@RequestMapping(value = "/search/searchHighLightNew", method = RequestMethod.GET)
	public ModelAndView searchHighLightNew(@RequestParam(required = false) String keyword,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		ModelAndView model = new ModelAndView();
		Page<EsProduct> esProductPage = esProductService.searchHighLightNew(keyword, pageNum, pageSize);
		System.out.println(JSON.toJSONString(esProductPage));
		model.addObject("esProductPage", esProductPage.getContent().get(0).getProductCategoryName() + "<<<"
				+ esProductPage.getContent().get(0).getSubTitle());
		model.setViewName("searchHighLight");
		return model;
	}
}
