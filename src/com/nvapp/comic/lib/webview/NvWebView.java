package com.nvapp.comic.lib.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

public class NvWebView extends WebView {
	public NvWebView(Context context) {
		super(context);

		this.config();
	}

	public NvWebView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.config();
	}

	public NvWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.config();
	}

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	private void config() {
		this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		this.addJavascriptInterface(new JsExtend(this.getContext()), "plus");
		this.addJavascriptInterface(new Device(this.getContext()), "plus.device");
		this.setWebViewClient(new DefaultWebViewClient());
		this.setWebChromeClient(new DefaultWebChromeClient(this.getContext()));

		WebSettings ws = this.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setJavaScriptCanOpenWindowsAutomatically(true);
		ws.setAllowFileAccess(true);
		// ws.setMediaPlaybackRequiresUserGesture(true);
		ws.setSupportZoom(false);
		ws.setBuiltInZoomControls(false);
		ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		ws.setDomStorageEnabled(true);
		ws.setDatabaseEnabled(true);
		ws.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
	}
}
