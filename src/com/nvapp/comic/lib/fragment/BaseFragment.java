package com.nvapp.comic.lib.fragment;

import com.nvapp.comic.R;
import com.nvapp.comic.lib.view.SearchEditText;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class BaseFragment extends Fragment {
	protected ImageView topLeftBtn;
	protected ImageView topRightBtn;
	protected TextView topTitleTxt;
	protected TextView topLetTitleTxt;
	protected TextView topRightTitleTxt;

	protected ViewGroup topBar;
	protected SearchEditText topSearchEdt;
	protected ViewGroup topContentView;
	protected LinearLayout topLeftContainerLayout;
	protected float x1, y1, x2, y2 = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		topContentView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.im_fragment_base, null);

		topBar = (ViewGroup) topContentView.findViewById(R.id.topbar);
		topTitleTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title);
		topLetTitleTxt = (TextView) topContentView.findViewById(R.id.left_txt);
		topRightTitleTxt = (TextView) topContentView.findViewById(R.id.right_txt);
		topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
		topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
		topSearchEdt = (SearchEditText) topContentView.findViewById(R.id.chat_title_search);
		topLeftContainerLayout=(LinearLayout)topContentView.findViewById(R.id.top_left_container);
				
		topTitleTxt.setVisibility(View.GONE);
		topRightBtn.setVisibility(View.GONE);
		topLeftBtn.setVisibility(View.GONE);
		topLetTitleTxt.setVisibility(View.GONE);
		topRightTitleTxt.setVisibility(View.GONE);
		topSearchEdt.setVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle bundle) {
		if (null != topContentView) {
			((ViewGroup) topContentView.getParent()).removeView(topContentView);
			return topContentView;
		}
		return topContentView;
	}

	protected void setTopTitle(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void hideTopTitle() {
		topTitleTxt.setVisibility(View.GONE);
	}

	protected void setTopLeftButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topLeftBtn.setImageResource(resID);
		topLeftBtn.setVisibility(View.VISIBLE);
	}

	protected void hideTopLeftButton() {
		topLeftBtn.setVisibility(View.GONE);
	}

	protected void setTopLeftText(String text) {
		if (null == text) {
			return;
		}
		topLetTitleTxt.setText(text);
		topLetTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightText(String text) {
		if (null == text) {
			return;
		}
		topRightTitleTxt.setText(text);
		topRightTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topRightBtn.setImageResource(resID);
		topRightBtn.setVisibility(View.VISIBLE);
	}

	protected void hideTopRightButton() {
		topRightBtn.setVisibility(View.GONE);
	}

	protected void setTopBar(int resID) {
		if (resID <= 0) {
			return;
		}
		topBar.setBackgroundResource(resID);
	}

	protected void showTopSearchBar() {
		topSearchEdt.setVisibility(View.VISIBLE);
	}

	protected void hideTopSearchBar() {
		topSearchEdt.setVisibility(View.GONE);
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	protected void initSearch() {
		setTopRightButton(R.drawable.tt_top_search);
		topRightBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				showSearchView();
			}
		});
	}

	private void showSearchView() {
//		startActivity(new Intent(getActivity(), SearchActivity.class));
	}
}
