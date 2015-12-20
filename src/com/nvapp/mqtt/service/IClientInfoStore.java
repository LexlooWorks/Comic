package com.nvapp.mqtt.service;

/**
 * 客户端信息存储
 */
public interface IClientInfoStore {
    /**
     * 存储客户端信息
     * 
     * @param clientInfo 客户信息
     */
    void store(ClientInfo clientInfo);

    /**
     * 读取客户端信息
     * 
     * @return 客户端信息
     */
    ClientInfo read();
}
