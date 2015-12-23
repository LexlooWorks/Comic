package com.nvapp.comic.lib.webview;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class Device {
	private Context context;

	public Device(Context context) {
		this.context = context;
	}

	@JavascriptInterface
	public void toast(String text) {
		Toast.makeText(this.context, text, Toast.LENGTH_SHORT).show();
	}

	@JavascriptInterface
	public void upload(String code, String value) {
		try {
			JSONObject jsoValue = new JSONObject(value);
			Toast.makeText(this.context, jsoValue.toString(), Toast.LENGTH_SHORT).show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
