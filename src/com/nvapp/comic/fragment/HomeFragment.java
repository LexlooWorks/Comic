package com.nvapp.comic.fragment;

import com.nvapp.comic.R;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

public class HomeFragment extends Fragment implements android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {
	// private HomeAdapter adapter;
	ListView listView;
	private CountDownTimer timer;
	ProgressBar progressbar;
	SwipeRefreshLayout swipe;
	// List<HomeBean> sections;
	// FinalDb db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		// db = FinalDb.create(getActivity(), false);
		progressbar = (ProgressBar) view.findViewById(R.id.progress_bar);
		listView = (ListView) view.findViewById(R.id.listView);
		swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
		swipe.setOnRefreshListener(this);
		// 顶部刷新的样式
		swipe.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_green_light,
				android.R.color.holo_blue_bright, android.R.color.holo_orange_light);
		timer = new CountDownTimer(9 * 100, 100) {

			@Override
			public void onTick(long millisUntilFinished) {
				long a = millisUntilFinished / 100;
				if (a == 1) {
					// getData(APIURL.SERVER_URL + "item_a.json");
					// Log.e("APIURL:", APIURL.SERVER_URL + "item_a.json");
					// adapter = new HomeAdapter(getActivity(), listView);
					// listView.setAdapter(adapter);
				} else {
					progressbar.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onFinish() {

			}
		};
		// sections = db.findAllByWhere(HomeBean.class, "1=1", "id");
		// if (NetWorkUtil.networkCanUse(getActivity())) {
		timer.start();
		// } else {
		// progressbar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.net_error_loading));
		// if (sections != null || sections.size() != 0) {
		// adapter = new HomeAdapter(getActivity(), listView);
		// listView.setAdapter(adapter);
		// adapter.resetData(sections);
		// }
		// }
	}

	@Override
	public void onRefresh() {

		new Handler().postDelayed(new Runnable() {
			public void run() {

				// if (NetWorkUtil.networkCanUse(getActivity())) {
				// getData(APIURL.SERVER_URL + "item_a.json");
				// adapter = new HomeAdapter(getActivity(), listView);
				// listView.setAdapter(adapter);
				// } else {
				// Toast.makeText(getActivity(), "网络连接失败..", 1).show();
				progressbar.setVisibility(View.GONE);
				// if (sections != null || sections.size() != 0) {
				// adapter = new HomeAdapter(getActivity(), listView);
				// listView.setAdapter(adapter);
				// adapter.resetData(sections);
				// }
				// }

				swipe.setRefreshing(false);

			}
		}, 1500);
	}
}
