/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 */
package com.itfsm.mqtt.service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class MqttService extends Service {
	static final String TAG = "MqttService";

	private static final String brokerURL = "tcp://114.215.195.158:1883";
	private BackgroundDataPreferenceReceiver backgroundDataPreferenceMonitor;
	private DisconnectCommandReceiver diconnectCommandReceiver;

	private volatile boolean backgroundDataEnabled = true;

	private MqttConnection client;
	private static MqttConnectOptions conOpt = new MqttConnectOptions();

	static {
		conOpt.setCleanSession(true);
		conOpt.setConnectionTimeout(60);
		conOpt.setKeepAliveInterval(240);
	}

	public MqttService() {
		super();
	}

	public void close(String clientHandle) {
		client.close();
	}

	public void disconnect() {
		client.disconnect();

		// the activity has finished using us, so we can stop the service
		// the activities are bound with BIND_AUTO_CREATE, so the service will
		// remain around until the last activity disconnects
		stopSelf();
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained,
			IMqttActionListener listener) throws MqttPersistenceException, MqttException {
		return client.publish(topic, payload, qos, retained, listener);
	}

	public void subscribe(String[] topic, int[] qos, IMqttActionListener listener) {
		client.subscribe(topic, qos, listener);
	}

	public IMqttDeliveryToken[] getPendingDeliveryTokens(String clientHandle) {
		return client.getPendingDeliveryTokens();
	}

	public Status acknowledgeMessageArrival(String clientHandle, String id) {
		return Status.OK;
	}

	// Extend Service

	/**
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.d("MqttService", "Destroy ......");
		// disconnect immediately
		if (client != null) {
			client.disconnect();
		}
		unregisterBroadcastReceivers();

		Intent intent = new Intent(MqttConstants.MQTT_ACTION_START_MQTT_SERVER);
		this.sendBroadcast(intent);

		super.onDestroy();
	}

	/**
	 * @see android.app.Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * @see android.app.Service#onStartCommand(Intent,int,int)
	 */
	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {
		startMqtt(intent);
		// run till explicitly stopped, restart when
		// process restarted
		registerBroadcastReceivers();

		return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId);
	}

	/**
	 * 启动MQTT
	 * 
	 * @param intent intent
	 */
	private synchronized void startMqtt(Intent intent) {
		Log.d(TAG, "startMqtt：command + " + intent);
		if (intent == null) {
			Log.d(TAG, "服务后台自动重启");
			backgroundStartup();

			return;
		}

		String tenantId = intent.getStringExtra(MqttConstants.MQTT_EXTRA_TENANTID);
		String mobile = intent.getStringExtra(MqttConstants.MQTT_EXTRA_MOBILE);
		boolean netWorkTypeChanged = intent.getBooleanExtra(MqttConstants.MQTT_EXTRA_NETWORK_CHANGED, false);

		Log.d(TAG, "startMqtt：" + tenantId + "," + mobile);
		IClientInfoStore clientInfoStore = new FileClientInfoStore(this);
		ClientInfo info = clientInfoStore.read();
		if (info == null) {
			info = new ClientInfo(tenantId, mobile, ClientInfo.genRandomId());
			clientInfoStore.store(info);

			this.connect(info.getClientId(), tenantId, mobile, netWorkTypeChanged);
		} else {
			if (info.getTenantId().equals(tenantId) && info.getMobile().equals(mobile)) {
				this.connect(info.getClientId(), tenantId, mobile, netWorkTypeChanged);
			} else {
				if (mobile == null || mobile == null) {
					Log.d(TAG, "startMqtt Error：参数都为空");
				} else {
					info = new ClientInfo(tenantId, mobile, ClientInfo.genRandomId());
					clientInfoStore.store(info);

					this.connect(info.getClientId(), tenantId, mobile, true);
				}
			}
		}
	}

	/**
	 * 后台自动重启
	 */
	private synchronized void backgroundStartup() {
		IClientInfoStore clientInfoStore = new FileClientInfoStore(this);
		ClientInfo info = clientInfoStore.read();
		if (info != null) {
			this.connect(info.getClientId(), info.getTenantId(), info.getMobile(), false);
		}
	}

	private synchronized void connect(String clientId, String tenantId, String mobile, boolean isNetWorkTypeChanged) {
		Log.d("MqttService", "connect：" + clientId + ":" + brokerURL);
		// 判断是否在线
		if (!isOnline()) {
			Log.d("MqttService", "is offline");

			notifyClientsOffline();
			return;
		}

		if (this.client != null) {
			Log.d("MqttService", "this.isConnected():" + this.isConnected() + " this.client.isConnecting(): "
					+ this.client.isConnecting());

			if (this.client.isConnecting()) {
				return;
			}

			if (isNetWorkTypeChanged) {
				if (this.client != null) {
					this.client.disconnect();
					this.client.close();
				}

				this.client = null;
			}

			if (this.client != null) {
				this.client.connect(conOpt);
			}
		}

		if (this.client == null) {
			this.client = new MqttConnection(this, brokerURL, clientId, tenantId, mobile, null);
			Log.d("MqttService", "连接到服务器，使用客户端：" + clientId + ":" + brokerURL);

			client.connect(conOpt);
		}
	}

	@SuppressWarnings("deprecation")
	private void registerBroadcastReceivers() {
		// Register publish command
		if (diconnectCommandReceiver == null) {
			diconnectCommandReceiver = new DisconnectCommandReceiver();
			registerReceiver(diconnectCommandReceiver,
					new IntentFilter(MqttConstants.MQTT_ACTION_DISCONNECT_MQTT_SERVER));
		}

		if (Build.VERSION.SDK_INT < 14 /**
										 * Build.VERSION_CODES.
										 * ICE_CREAM_SANDWICH
										 **/
		) {
			// Support the old system for background data preferences
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			backgroundDataEnabled = cm.getBackgroundDataSetting();
			if (backgroundDataPreferenceMonitor == null) {
				backgroundDataPreferenceMonitor = new BackgroundDataPreferenceReceiver();
				registerReceiver(backgroundDataPreferenceMonitor,
						new IntentFilter(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));
			}
		}
	}

	private void unregisterBroadcastReceivers() {
		if (diconnectCommandReceiver != null) {
			unregisterReceiver(diconnectCommandReceiver);
			diconnectCommandReceiver = null;
		}

		if (Build.VERSION.SDK_INT < 14 /**
										 * Build.VERSION_CODES.
										 * ICE_CREAM_SANDWICH
										 **/
		) {
			if (backgroundDataPreferenceMonitor != null) {
				unregisterReceiver(backgroundDataPreferenceMonitor);
				backgroundDataPreferenceMonitor = null;
			}
		}
	}

	/**
	 * @return whether the android service can be regarded as online
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected() && backgroundDataEnabled) {
			return true;
		}

		return false;
	}

	/**
	 * Notify clients we're offline
	 */
	private void notifyClientsOffline() {
		if (this.client != null) {
			client.offline();
		}
	}

	/**
	 * Detect changes of the Allow Background Data setting - only used below
	 * ICE_CREAM_SANDWICH
	 */
	private class BackgroundDataPreferenceReceiver extends BroadcastReceiver {

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			if (cm.getBackgroundDataSetting()) {
				if (!backgroundDataEnabled) {
					backgroundDataEnabled = true;
					// we have the Internet connection - have another try at
					// connecting
					// reconnect();
				}
			} else {
				backgroundDataEnabled = false;
				notifyClientsOffline();
			}
		}
	}

	private class DisconnectCommandReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (client != null) {
				client.disconnect();
			}
		}
	}

}
