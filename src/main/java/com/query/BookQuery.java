package com.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * 类名称：BookQuery
 * 类描述：封装Book 查询参数
 * 创建人：WeJan
 * 创建时间：2018年09月04日 13:30
 */
public class BookQuery {

    private String queryString;

    private Integer page = 1;

    private Integer size = 20;

    private Integer wordsBegin;

    private Integer wordsEnd;

    private Integer sort;

    private Boolean vip;

    private Integer site;

    private Integer collection;

    private Integer click;

    private Integer popularity;

    private Integer goods;

    private Integer status;

    private Date updatetime;

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getWordsBegin() {
		return wordsBegin;
	}

	public void setWordsBegin(Integer wordsBegin) {
		this.wordsBegin = wordsBegin;
	}

	public Integer getWordsEnd() {
		return wordsEnd;
	}

	public void setWordsEnd(Integer wordsEnd) {
		this.wordsEnd = wordsEnd;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Boolean getVip() {
		return vip;
	}

	public void setVip(Boolean vip) {
		this.vip = vip;
	}

	public Integer getSite() {
		return site;
	}

	public void setSite(Integer site) {
		this.site = site;
	}

	public Integer getCollection() {
		return collection;
	}

	public void setCollection(Integer collection) {
		this.collection = collection;
	}

	public Integer getClick() {
		return click;
	}

	public void setClick(Integer click) {
		this.click = click;
	}

	public Integer getPopularity() {
		return popularity;
	}

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public Integer getGoods() {
		return goods;
	}

	public void setGoods(Integer goods) {
		this.goods = goods;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}


}
