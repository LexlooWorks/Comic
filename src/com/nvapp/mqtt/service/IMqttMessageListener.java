package com.nvapp.mqtt.service;

public interface IMqttMessageListener {
	void onMessage(String topic, String message);
}
