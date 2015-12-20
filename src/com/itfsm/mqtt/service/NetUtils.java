package com.itfsm.mqtt.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
	/** 没有网络 */
	public static final int NETWORKTYPE_INVALID = 0;
	/** DATA网络 */
	public static final int NETWORKTYPE_MOBILE = 1;
	/** wifi网络 */
	public static final int NETWORKTYPE_WIFI = 2;

	/**
	 * 获取网络状态.
	 * 
	 * @param context 上下文
	 * @return int 网络状态
	 */
	public static int getNetWorkType(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		int mNetWorkType = NETWORKTYPE_INVALID;

		if (networkInfo != null && networkInfo.isConnected()) {
			String type = networkInfo.getTypeName();

			if (type.equalsIgnoreCase("WIFI")) {
				mNetWorkType = NETWORKTYPE_WIFI;
			} else if (type.equalsIgnoreCase("MOBILE")) {
				mNetWorkType = NETWORKTYPE_MOBILE;
			}
		}

		return mNetWorkType;
	}
}
