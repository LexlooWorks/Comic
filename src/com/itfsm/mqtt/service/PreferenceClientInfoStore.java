//package com.itfsm.mqtt.service;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.preference.PreferenceManager;
//
//public class PreferenceClientInfoStore implements IClientInfoStore {
//	private Context context;
//
//	public PreferenceClientInfoStore(Context context) {
//		this.context = context;
//	}
//
//	@Override
//	public void store(ClientInfo clientInfo) {
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//		Editor edit = sp.edit();
//		edit.putString("tenantId", clientInfo.getTenantId());
//		edit.putString("mobile", clientInfo.getMobile());
//		edit.putString("randomId", clientInfo.getRandomId());
//
//		edit.commit();
//	}
//
//	@Override
//	public ClientInfo read() {
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//		String tenantId = sp.getString("tenantId", null);
//		String mobile = sp.getString("mobile", null);
//		String randomId = sp.getString("randomId", null);
//
//		if (tenantId != null && mobile != null) {
//			return new ClientInfo(tenantId, mobile, randomId);
//		}
//
//		return null;
//	}
//
//}
