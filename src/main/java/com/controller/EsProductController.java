package com.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.es.document.EsProduct;
import com.es.document.EsProductRelatedInfo;
import com.es.elasticsearch.common.CommonPage;
import com.es.elasticsearch.common.CommonResult;
import com.es.elasticsearch.service.EsProductService;


/**
 * 简单的增删改查 Created by wangyt on 2019/07/01.
 */
@Controller
@RequestMapping("/esProduct")
public class EsProductController {

	private static Logger logger = LogManager.getLogger(EsProductController.class);

	@Autowired
	private EsProductService esProductService;

	@RequestMapping(value = "/importAll", method = RequestMethod.POST)
	@ResponseBody
	public CommonResult<Integer> importAllList() {
		logger.info("{}", "插入开始");
		int count = esProductService.importAll();
		return CommonResult.success(count);
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<Object> delete(@PathVariable Long id) {
		esProductService.delete(id);
		return CommonResult.success(null);
	}

	@RequestMapping(value = "/delete/batch", method = RequestMethod.POST)
	@ResponseBody
	public CommonResult<Object> delete(@RequestParam("ids") List<Long> ids) {
		esProductService.delete(ids);
		return CommonResult.success(null);
	}

	@RequestMapping(value = "/create/{id}", method = RequestMethod.POST)
	@ResponseBody
	public CommonResult<EsProduct> create(@PathVariable Long id) {
		EsProduct esProduct = esProductService.create(id);
		if (esProduct != null) {
			return CommonResult.success(esProduct);
		} else {
			return CommonResult.failed();
		}
	}

	@RequestMapping(value = "/search/simple", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		Page<EsProduct> esProductPage = esProductService.search(keyword, pageNum, pageSize);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<CommonPage<EsProduct>> search(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long brandId, @RequestParam(required = false) Long productCategoryId,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(required = false, defaultValue = "0") Integer sort) {
		Page<EsProduct> esProductPage = esProductService.search(keyword, brandId, productCategoryId, pageNum, pageSize,
				sort);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}
    /**
     * 查询距离
     * @param keyword
     * @param brandId
     * @param productCategoryId
     * @param pageNum
     * @param pageSize
     * @param sort
     * @return
     */
	@RequestMapping(value = "/searchDistance", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<CommonPage<EsProduct>> searchDistance(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long brandId, @RequestParam(required = false) Long productCategoryId,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize,
			@RequestParam(required = false, defaultValue = "0") Integer sort) {
		logger.info("{}", "查询开始");
		Page<EsProduct> esProductPage = esProductService.searchDistance(keyword, brandId, productCategoryId, pageNum,
				pageSize, sort);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@RequestMapping(value = "/recommend/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<CommonPage<EsProduct>> recommend(@PathVariable Long id,
			@RequestParam(required = false, defaultValue = "0") Integer pageNum,
			@RequestParam(required = false, defaultValue = "5") Integer pageSize) {
		Page<EsProduct> esProductPage = esProductService.recommend(id, pageNum, pageSize);
		return CommonResult.success(CommonPage.restPage(esProductPage));
	}

	@RequestMapping(value = "/search/relate", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<EsProductRelatedInfo> searchRelatedInfo(@RequestParam(required = false) String keyword) {

		EsProductRelatedInfo productRelatedInfo = esProductService.searchRelatedInfo(keyword);
		return CommonResult.success(productRelatedInfo);
	}
	/**
	 * 范围查找
	 * @param keyword
	 * @return
	 */
	@RequestMapping(value = "/search/minmax", method = RequestMethod.GET)
	@ResponseBody
	public CommonResult<EsProductRelatedInfo> minmax(@RequestParam(required = false) String keyword) {

		EsProductRelatedInfo productRelatedInfo = esProductService.minmax(keyword);
		return CommonResult.success(productRelatedInfo);
	}
}
