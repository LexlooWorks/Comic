/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package com.itfsm.mqtt.service;

/**
 * Various strings used to identify operations or data in the Android MQTT
 * service, mainly used in Intents passed between Activities and the Service.
 */
interface MqttServiceConstants {

	/*
	 * Version information
	 */
	
	static final String VERSION = "v0";
	
  /*
   * Attributes of messages <p> Used for the column names in the database
   */
  static final String DUPLICATE = "duplicate";
  static final String RETAINED = "retained";
  static final String QOS = "qos";
  static final String PAYLOAD = "payload";
  static final String DESTINATION_NAME = "destinationName";
  static final String CLIENT_HANDLE = "clientHandle";
  static final String MESSAGE_ID = "messageId";

  //Intent prefix for Ping sender.
  static final String PING_SENDER = MqttService.TAG + ".pingSender.";
  
  //Constant for wakelock
  static final String PING_WAKELOCK = MqttService.TAG + ".client.";  
  static final String WAKELOCK_NETWORK_INTENT = MqttService.TAG + "";  
}