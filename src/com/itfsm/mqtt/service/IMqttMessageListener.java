package com.itfsm.mqtt.service;

public interface IMqttMessageListener {
	void onMessage(String topic, String message);
}
