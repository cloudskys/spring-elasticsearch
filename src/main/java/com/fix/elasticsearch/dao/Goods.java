package com.fix.elasticsearch.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
 
@Document(indexName="aa", type = "bb")
public class Goods {
    @Id
    private String id; // id必须有
    private String updateTime;
    private String udateTime;
    private String sku_no;
    private int num;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getSku_no() {
		return sku_no;
	}
	public void setSku_no(String sku_no) {
		this.sku_no = sku_no;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public String getUdateTime() {
		return udateTime;
	}
	public void setUdateTime(String udateTime) {
		this.udateTime = udateTime;
	}
    
}
