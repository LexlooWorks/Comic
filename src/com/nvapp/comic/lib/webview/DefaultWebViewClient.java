package com.nvapp.comic.lib.webview;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DefaultWebViewClient extends WebViewClient {
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}

	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
	}
}
