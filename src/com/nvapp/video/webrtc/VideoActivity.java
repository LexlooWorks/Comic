package com.nvapp.video.webrtc;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoRendererGui.ScalingType;

import com.nvapp.comic.R;
import com.nvapp.video.webrtc.PeerConnectionClient.PeerConnectionParameters;
import com.nvapp.video.webrtc.RTCClient.SignalingParameters;
import com.nvapp.video.webrtc.widgets.NaviBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import de.greenrobot.event.EventBus;

public class VideoActivity extends Activity implements OnClickListener, PeerConnectionClient.PeerConnectionEvents {
    private final int MOBILE_QUERY = 1;
    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 3;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 28;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private boolean initiator;
    private PeerConnectionClient peerConnectionClient = null;
    private PeerConnectionParameters peerConnectionParameters;
    private ScalingType scalingType;
    private SignalingParameters signalingParameters;

    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private AppRTCAudioManager audioManager = null;

    private GLSurfaceView videoView;
    private boolean iceConnected;

    /* 操作面板 */
    private LinearLayout btnsPane;

    private NaviBar naviBar;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN
                             | LayoutParams.FLAG_KEEP_SCREEN_ON
                             | LayoutParams.FLAG_DISMISS_KEYGUARD
                             | LayoutParams.FLAG_SHOW_WHEN_LOCKED
                             | LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView()
                   .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                          | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video);

        naviBar = (NaviBar) this.findViewById(R.id.navibar_video);
        naviBar.setReturnCaption("返回");
        naviBar.setCaption("通话中...");
        naviBar.setReturnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        ImageView ivSwitchCamera = new ImageView(this);
        ivSwitchCamera.setBackgroundResource(R.drawable.selector_img_switch_camera);
        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                peerConnectionClient.switchCamera();
            }
        });
        naviBar.addRightBarView(ivSwitchCamera);

        ImageButton btnVideoHangup = (ImageButton) this.findViewById(R.id.btn_video_hangup);
        btnVideoHangup.setOnClickListener(this);
        btnsPane = (LinearLayout) this.findViewById(R.id.btns_hangup);

        parseExtraData();

        videoView = (GLSurfaceView) findViewById(R.id.glview_call);
        videoView.setOnClickListener(this);

        initSignalingParameters();
        // Create video renderers.
        VideoRendererGui.setView(videoView, new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactory();
            }
        });

        Log.d("WebRTC", "" + scalingType);
        remoteRender = VideoRendererGui.create(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        localRender = VideoRendererGui.create(LOCAL_X_CONNECTING,
                                              LOCAL_Y_CONNECTING,
                                              LOCAL_WIDTH_CONNECTING,
                                              LOCAL_HEIGHT_CONNECTING,
                                              scalingType,
                                              true);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(this, new Runnable() {
            // This method will be called each time the audio state (number and
            // type of devices) has been changed.
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d("WebRTC", "Initializing the audio manager...");
        audioManager.init();

        EventBus.getDefault().register(this);
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
    }

    private void parseExtraData() {
        Intent intent = this.getIntent();

        this.initiator = intent.getBooleanExtra(RTCClient.EXTRA_INITIATOR, true);
        this.peerConnectionParameters = new PeerConnectionParameters(true, 320, 240, 15, 0, null, true, 0, null, true);
    }

    @Override
    protected void onDestroy() {
        disconnect();
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    /* 接收EventBus消息 */
    public void onEventMainThread(RTCEvent event) {
        if (RTCClient.CMD_BYE.equals(event.getName())) {
            this.finish();
        }
    }

    private void disconnect() {
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }

        sendCommand(RTCClient.CMD_SEND_BYE, null);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_video_hangup) {
            disconnect();
        } else if (v.getId() == R.id.glview_call) {//
            this.toggleHangUpPane();
        }
    }

    private void initSignalingParameters() {
        LinkedList<IceCandidate> iceCandidates = new LinkedList<IceCandidate>();
        SessionDescription offerSdp = null;

        LinkedList<PeerConnection.IceServer> iceServers = getIceServers();

        this.signalingParameters = new SignalingParameters(iceServers, initiator, offerSdp, iceCandidates);
    }

    // Create peer connection factory when EGL context is ready.
    private void createPeerConnectionFactory() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    peerConnectionClient = PeerConnectionClient.getInstance();
                    peerConnectionClient.createPeerConnectionFactory(VideoActivity.this,
                                                                     VideoRendererGui.getEGLContext(),
                                                                     peerConnectionParameters,
                                                                     VideoActivity.this);
                }

                callConnected();

                peerConnectionClient.createPeerConnection(localRender, remoteRender, signalingParameters);

                if (!initiator) {
                    sendCommand(RTCClient.CMD_SEND_REPLY_CALL, null);
                }

                if (signalingParameters.initiator) {
                    Log.d("WebRTC", "Creating OFFER...");
                    // Create offer. Offer SDP will be sent to answering client
                    // in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createOffer();
                } else {
                    // if (params.offerSdp != null) {
                    // peerConnectionClient.setRemoteDescription(params.offerSdp);
                    // Log.d("WebRTC", "Creating ANSWER...");
                    // // Create answer. Answer SDP will be sent to offering
                    // // client in
                    // // PeerConnectionEvents.onLocalDescription event.
                    // peerConnectionClient.createAnswer();
                    // }
                }
            }
        });
    }

    // Should be called from UI thread
    private void callConnected() {
        updateVideoView();

        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    }

    private void updateVideoView() {
        VideoRendererGui.update(remoteRender,
                                REMOTE_X,
                                REMOTE_Y,
                                REMOTE_WIDTH,
                                REMOTE_HEIGHT,
                                ScalingType.SCALE_ASPECT_FIT,
                                false);
        if (iceConnected) {
            VideoRendererGui.update(localRender,
                                    LOCAL_X_CONNECTED,
                                    LOCAL_Y_CONNECTED,
                                    LOCAL_WIDTH_CONNECTED,
                                    LOCAL_HEIGHT_CONNECTED,
                                    ScalingType.SCALE_ASPECT_FIT,
                                    false);
        } else {
            VideoRendererGui.update(localRender,
                                    LOCAL_X_CONNECTING,
                                    LOCAL_Y_CONNECTING,
                                    LOCAL_WIDTH_CONNECTING,
                                    LOCAL_HEIGHT_CONNECTING,
                                    ScalingType.SCALE_ASPECT_FIT,
                                    false);
        }
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        try {
            if (this.initiator) {
                JSONObject data = new JSONObject();
                JSONObject sdps = new JSONObject();

                sdps.put("sdp", sdp.description);

                sdps.put("type", "offer");
                data.put("sdp", sdps);

                sendCommand(RTCClient.CMD_SEND_OFFER, data);
            } else {
                JSONObject data = new JSONObject();
                JSONObject sdps = new JSONObject();
                sdps.put("sdp", sdp.description);
                sdps.put("type", "answer");
                data.put("sdp", sdps);

                sendCommand(RTCClient.CMD_SEND_ANSWER, data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        try {
            JSONObject data = new JSONObject();
            data.put("label", candidate.sdpMLineIndex);
            data.put("candidate", candidate.sdp.trim());
            data.put("id", candidate.sdpMid);

            sendCommand(RTCClient.CMD_SEND_ICE, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIceConnected() {
        this.iceConnected = true;
        this.updateVideoView();
    }

    @Override
    public void onIceDisconnected() {
        this.iceConnected = false;
        this.updateVideoView();
    }

    @Override
    public void onPeerConnectionClosed() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPeerConnectionError(String description) {
        // TODO Auto-generated method stub

    }

    private LinkedList<PeerConnection.IceServer> getIceServers() {
        LinkedList<PeerConnection.IceServer> ret = new LinkedList<PeerConnection.IceServer>();

        ret.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        ret.add(new PeerConnection.IceServer("turn:112.124.126.251:3478", "lexloo", "lexloo741018"));

        return ret;
    }

    private void toggleHangUpPane() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (btnsPane.getVisibility() == View.INVISIBLE) {
                    btnsPane.setVisibility(View.VISIBLE);
                    btnsPane.bringToFront();
                } else {
                    btnsPane.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void sendCommand(String cmd, JSONObject data) {
        try {
            JSONObject message = data;
            if (message == null) {
                message = new JSONObject();
            }
            message.put("eventName", cmd);

            Intent cmdService = new Intent(RTCClient.VIDEO_CMD_SERVICE);
            cmdService.putExtra("message", message.toString());
            this.startService(cmdService);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
