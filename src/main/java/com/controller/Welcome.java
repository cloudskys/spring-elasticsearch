package com.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fix.elasticsearch.dao.Goods;
import com.fix.elasticsearch.service.GoodsService;


@Controller
@RequestMapping("/Welcome")
public class Welcome {
	 @Autowired
	 private GoodsService goodsService;
	@RequestMapping(value = "/all")
	public ModelAndView Welcome(HttpServletRequest request, HttpSession session) throws UnsupportedEncodingException {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("message", "CLICK Successful");
		modelAndView.setViewName("forward:/welcome.jsp");
		return modelAndView;
	}
	
	//增加或者更新
	@RequestMapping(value = "/b")
    public ModelAndView addEsCashInfo() throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		//elasticsearchTemplate.createIndex(Item.class);
		modelAndView.setViewName("forward:/welcome.jsp");
		return modelAndView;
}
	@RequestMapping("/save")
    @ResponseBody
    public Object save(String skuNo) {
		Goods g =new Goods();
		g.setId("3");
		g.setNum(12);
		g.setSku_no("878787");
		g.setUdateTime("2019-10-10 12:12:12");
		 goodsService.save(g);
         Object b= goodsService.findBySkuNo("878787");
         return b;
	}
	@RequestMapping("/update")
    @ResponseBody
    public Object update(String skuNo) {
		Goods g =new Goods();
		g.setId("26");
		g.setNum(9999);
		g.setSku_no("878787");
		g.setUpdateTime("25s");
		 goodsService.save(g);
        // Object b= goodsService.findBySkuNo("878787");
         return null;
	}
	@RequestMapping("/get")
	@ResponseBody
	public Object get(String skuNo) {
		Object b= goodsService.findBySkuNoList("878787");
		return b;
	}
	@RequestMapping("/delete")
	@ResponseBody
	public Object delete(String skuNo) {
		Goods g =new Goods();
		g.setId("3");
		goodsService.delete(g);
		return null;
	}
	@RequestMapping("/save100")
	@ResponseBody
    public void save100(){
        for(int i=1;i<=800;i++){
        	System.out.println("保存:"+i);
        	Goods article = new Goods();
            article.setId(i+"");
            article.setSku_no(i+"elasticSearch 3.0版本发布..，更新");
            article.setUpdateTime(i+"s");
            goodsService.save(article);
        }
    }
	/**分页查询*/
	@RequestMapping("/findAllPage")
	@ResponseBody
    public void findAllPage(){
        Pageable pageable = PageRequest.of(2,10);
        Page<Goods> page = goodsService.findAll(pageable);
        for(Goods article:page.getContent()){
            System.out.println(article.getId());
        }
    }
	/**分页查询*/
	@RequestMapping("/findBySku")
	@ResponseBody
    public void findBySku(){
        Pageable pageable = PageRequest.of(0,10);
        String updateTime = "25s";
        Page<Goods> page = goodsService.findByUpdate(updateTime,pageable);
        for(Goods article:page.getContent()){
            System.out.println(article.getId());
        }
    }
	/**分页查询*/
	@RequestMapping("/findByIdAndUpdateTime")
	@ResponseBody
	public void findByIdAndUpdateTime(){
		Pageable pageable = PageRequest.of(0,10);
		String updateTime = "25s";
		int id = 25;
		Page<Goods> page = goodsService.findByIdAndUpdateTime(id,updateTime,pageable);
		for(Goods article:page.getContent()){
			System.out.println(article.getId());
		}
	}
	/**分页查询*/
	@RequestMapping("/searchBuilder")
	@ResponseBody
	public void searchBuilder(){
		Pageable pageable = PageRequest.of(0,10);
		String updateTime = "25s";
		int id = 25;
		Page<Goods> page = goodsService.searchBuilder(id,updateTime,pageable);
		for(Goods article:page.getContent()){
			System.out.println(article.getId());
		}
	}
	@RequestMapping("/templete")
	@ResponseBody
	public void templete(){
		Pageable pageable = PageRequest.of(0,10);
		String updateTime = "25s";
		int id = 25;
		List<Goods> page = goodsService.searchtemplete(id,updateTime,pageable);
		for(Goods article:page){
			System.out.println(article.getId());
		}
	}
	@RequestMapping("/templete_sum")
	@ResponseBody
	public void templete_sum(){
		Pageable pageable = PageRequest.of(0,10);
		String updateTime = "25s";
		int id = 25;
		Map map =  goodsService.getEsCashSummaryInfo(id);
		
	}
	@RequestMapping("/search")
	@ResponseBody
	public void search(){
		Pageable pageable = PageRequest.of(0,10);
		String updateTime = "25s";
		int id = 25;
		 goodsService.search();
		
	}
	
	
}
