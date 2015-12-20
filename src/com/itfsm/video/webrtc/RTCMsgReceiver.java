package com.itfsm.video.webrtc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * WebRTC消息接收器
 */
public class RTCMsgReceiver extends BroadcastReceiver {
    public static final String EXTRA_TOPIC = "extra_topic";
    public static final String EXTRA_MESSAGE = "extra_message";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String topic = intent.getStringExtra(EXTRA_TOPIC);
        Log.d("WebRTC X", topic);
        Log.d("WebRTC X", topic);
        if (topic == null || message == null || !"WebRTC".equals(topic.split("/")[0])) {
            return;
        }

        Log.d("WebRTC", message);

        Intent cmdService = new Intent(RTCClient.VIDEO_CMD_SERVICE);
        cmdService.putExtra("message", message);
        context.startService(cmdService);
    }
}
