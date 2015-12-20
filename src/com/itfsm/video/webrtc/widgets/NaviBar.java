package com.itfsm.video.webrtc.widgets;

import com.nvapp.comic.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NaviBar extends RelativeLayout {
	private static LinearLayout.LayoutParams VIEW_LAYOUT_PARAMS = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	private LinearLayout llLeftBar;
	private LinearLayout llRightBar;
	private TextView tvCaption;
	private TextView tvReturn;
	// private ImageView ibSwitchCamera;

	public NaviBar(Context context) {
		super(context);

		initView(context);
	}

	public NaviBar(Context context, AttributeSet attrs) {
		super(context, attrs, 0);

		initView(context);
	}

	public NaviBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initView(context);
	}

	private void initView(final Context context) {
		View.inflate(context, R.layout.comp_navi_bar, this);

		llLeftBar = (LinearLayout) this.findViewById(R.id.navi_left_bar);
		llRightBar = (LinearLayout) this.findViewById(R.id.navi_right_bar);
		tvCaption = (TextView) this.findViewById(R.id.navi_caption);
		tvReturn = (TextView) this.findViewById(R.id.navi_return_caption);
	}

	public void setReturnCaption(String caption) {
		this.tvReturn.setText(caption);
	}

	public void setCaption(String caption) {
		this.tvCaption.setText(caption);
	}

	public void setReturnClickListener(OnClickListener listener) {
		this.tvReturn.setOnClickListener(listener);
	}

	public void addLeftBarView(View v) {
		v.setLayoutParams(VIEW_LAYOUT_PARAMS);

		this.llLeftBar.addView(v);
	}

	public void addRightBarView(View v) {
		v.setLayoutParams(VIEW_LAYOUT_PARAMS);

		this.llRightBar.addView(v);
	}
}
