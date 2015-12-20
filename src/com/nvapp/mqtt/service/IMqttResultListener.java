package com.nvapp.mqtt.service;

public interface IMqttResultListener {
	void onSuccess();

	void onFailure();
}
