package com.nvapp.comic.lib.fragment;

import com.nvapp.comic.R;

import android.view.View;
import android.widget.ProgressBar;

public abstract class MainBaseFragment extends BaseFragment {
	private ProgressBar progressbar;

	public void init(View curView) {
		progressbar = (ProgressBar) curView.findViewById(R.id.progress_bar);
	}

	public void showProgressBar() {
		progressbar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		progressbar.setVisibility(View.GONE);
	}

}
