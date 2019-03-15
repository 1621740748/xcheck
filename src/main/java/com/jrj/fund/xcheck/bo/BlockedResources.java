package com.jrj.fund.xcheck.bo;

import java.util.Date;

import org.jfaster.mango.annotation.ID;

public class BlockedResources {
	@ID
	private Integer resId;
	private String resUrl;
	private String resHost;
	private String resHostPath;
	private String pageUrl;
	private String initiateUrl;
	private Date createTime;
	public Integer getResId() {
		return resId;
	}
	public void setResId(Integer resId) {
		this.resId = resId;
	}
	public String getResUrl() {
		return resUrl;
	}
	public void setResUrl(String resUrl) {
		this.resUrl = resUrl;
	}
	public String getResHost() {
		return resHost;
	}
	public void setResHost(String resHost) {
		this.resHost = resHost;
	}
	public String getResHostPath() {
		return resHostPath;
	}
	public void setResHostPath(String resHostPath) {
		this.resHostPath = resHostPath;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getInitiateUrl() {
		return initiateUrl;
	}
	public void setInitiateUrl(String initiateUrl) {
		this.initiateUrl = initiateUrl;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	
}
