package com.itfsm.video.webrtc;

import com.itfsm.video.webrtc.support.RingAlarm;
import com.nvapp.comic.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

public class SendCallActivity extends Activity implements OnClickListener {
    private static final int CONNECTION_REQUEST = 1;
    /* 是否是发起者 */
    private boolean initiator;
    /* 声音报警 */
    private RingAlarm alarm;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN
                             | LayoutParams.FLAG_KEEP_SCREEN_ON
                             | LayoutParams.FLAG_DISMISS_KEYGUARD
                             | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                             | LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_sendcall);

        ImageButton btnHangUp = (ImageButton) this.findViewById(R.id.btnHangUp);
        btnHangUp.setOnClickListener(this);

        Intent intent = this.getIntent();
        initiator = intent.getBooleanExtra(RTCClient.EXTRA_INITIATOR, true);

        if (initiator) {
            String toUser = intent.getStringExtra(RTCClient.EXTRA_TO_USER);
            this.setTips(String.format("请求%s视频中...", toUser));
        } else {
            String fromUser = intent.getStringExtra(RTCClient.EXTRA_FROM_USER);
            this.setTips(String.format("%s 视频请求中...", fromUser));
        }

        alarm = new RingAlarm();
        alarm.ring(this);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECTION_REQUEST) {
            setResult(resultCode);
            finish();
        }
    }

    /* 接收EventBus消息 */
    public void onEventMainThread(RTCEvent event) {
        if (RTCClient.CMD_REPLY_CALL.equals(event.getName())) {
            Intent intent = new Intent(this, VideoActivity.class);
            intent.putExtra(RTCClient.EXTRA_INITIATOR, this.initiator);

            startActivityForResult(intent, CONNECTION_REQUEST);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnHangUp) {
            this.finish();
        }
    }

    @Override
    public void onStop() {
        if (this.alarm != null) {
            alarm.stop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void setTips(final String tips) {

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.video_request_tips);
                tv.setText(tips);
            }
        });
    }
}
