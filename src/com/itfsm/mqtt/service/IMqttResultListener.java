package com.itfsm.mqtt.service;

public interface IMqttResultListener {
	void onSuccess();

	void onFailure();
}
