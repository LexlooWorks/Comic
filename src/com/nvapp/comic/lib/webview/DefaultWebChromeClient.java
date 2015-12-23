package com.nvapp.comic.lib.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class DefaultWebChromeClient extends WebChromeClient {
	private Context context;

	public DefaultWebChromeClient(Context context) {
		this.context = context;
	}

	// 设置网页加载的进度条
	public void onProgressChanged(WebView view, int newProgress) {
		System.out.println(newProgress);
	}

	// 获取网页的标题
	public void onReceivedTitle(WebView view, String title) {
		((Activity) this.context).setTitle(title);
	}

	/**
	 * 处理alert弹出框
	 */
	@Override
	public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

		// 对alert的简单封装
		new AlertDialog.Builder(context).setTitle("Alert").setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				}).create().show();

		return true;
	}

	/**
	 * 处理confirm弹出框
	 */
	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		// 对confirm的简单封装
		new AlertDialog.Builder(context).setTitle("Confirm").setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				}).create().show();

		return true;
	}

	/**
	 * 处理prompt弹出框
	 */
	@Override
	public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
		return super.onJsPrompt(view, url, message, message, result);
	}
}