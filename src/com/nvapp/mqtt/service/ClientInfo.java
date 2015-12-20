package com.nvapp.mqtt.service;

import java.util.Random;

/**
 * 客户端信息
 */
public class ClientInfo {
	/**
	 * 企业Id
	 */
	private String tenantId;
	/**
	 * 手机号码
	 */
	private String mobile;
	/**
	 * 唯一Id
	 */
	private String randomId;

	public ClientInfo(String tenantId, String mobile, String randomId) {
		this.tenantId = tenantId;
		this.mobile = mobile;
		this.randomId = randomId;
	}

	public static String genRandomId() {
		return String.valueOf((new Random()).nextInt(99));
	}

	/**
	 * 
	 * @return a Unique mqtt clientid
	 */
	public String getClientId() {
		return "A" + tenantId + "/" + mobile + "/" + this.randomId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getRandomId() {
		return randomId;
	}

	public void setRandomId(String randomId) {
		this.randomId = randomId;
	}
}
