package com.itfsm.video.webrtc.support;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 来电铃声报警
 *
 */
public class RingAlarm {
    private MediaPlayer mMediaPlayer;

    /**
     * 播放铃声
     */
    public void ring(Context context) {
        if (mMediaPlayer == null) {
            Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mMediaPlayer = MediaPlayer.create(context, ringtone);
        }

        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    /**
     * 停止铃声
     */
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
