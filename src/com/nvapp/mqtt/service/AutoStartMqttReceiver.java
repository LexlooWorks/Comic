package com.nvapp.mqtt.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class AutoStartMqttReceiver extends BroadcastReceiver {
	private static volatile int netWorkType = NetUtils.NETWORKTYPE_INVALID;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			Log.d("MqttService", intent.getAction());
		}


		IClientInfoStore clientInfoStore = new FileClientInfoStore(context);
		ClientInfo info = clientInfoStore.read();

		Log.d("MqttService", "info "  + info);
		if (info == null) {
			return;
		}

		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			// we protect against the phone switching off
			// by requesting a wake lock - we request the minimum possible wake
			// lock - just enough to keep the CPU running until we've finished
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
			wl.acquire();
			int tmpNetWorkType = NetUtils.getNetWorkType(context);
			// we have an internet connection - have another try at
			// connecting
			if (tmpNetWorkType == netWorkType) {
				MqttManager.start(context, info.getTenantId(), info.getMobile(), false);
			} else {
				netWorkType = tmpNetWorkType;
				MqttManager.start(context, info.getTenantId(), info.getMobile(), true);
			}

			wl.release();
		} else {
			MqttManager.start(context, info.getTenantId(), info.getMobile());
		}
	}
}
