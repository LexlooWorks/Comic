package com.itfsm.video.webrtc;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import de.greenrobot.event.EventBus;

public class RTCService extends Service implements RTCClient {
    private PeerConnectionClient peerConnectionClient = PeerConnectionClient.getInstance();
    private ConnectionParameters connectionParameters;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            JSONObject msg = new JSONObject(intent.getStringExtra("message"));

            String eventName = msg.getString("eventName");
            if (RTCClient.CMD_CALL.equals(eventName)) {
                this.onCall(msg);
            } else if (RTCClient.CMD_ANSWER.equals(eventName)) {
                this.onAnswer(msg);
            } else if (RTCClient.CMD_OFFER.equals(eventName)) {
                this.onOffer(msg);
            } else if (RTCClient.CMD_ICE.equals(eventName)) {
                this.onICE(msg);
            } else if (RTCClient.CMD_BYE.equals(eventName)) {
                this.onBye(msg);
            } else if (RTCClient.CMD_REPLY_CALL.equals(eventName)) {
                this.onReplyCall(msg);
            } else if (RTCClient.CMD_SEND_CALL.equals(eventName)) {
                this.onSendCall(msg);
            } else if (RTCClient.CMD_SEND_ANSWER.equals(eventName)) {
                this.onSendAnswer(msg);
            } else if (RTCClient.CMD_SEND_OFFER.equals(eventName)) {
                this.onSendOffer(msg);
            } else if (RTCClient.CMD_SEND_ICE.equals(eventName)) {
                this.onSendICE(msg);
            } else if (RTCClient.CMD_SEND_BYE.equals(eventName)) {
                this.onSendBye();
            } else if (RTCClient.CMD_SEND_REPLY_CALL.equals(eventName)) {
                this.onSendReplyCall();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCall(JSONObject msg) {
        try {
            this.setConnectionParameters(new ConnectionParameters(msg.getString("tenantId"),
                                                                  msg.getString("toId"),
                                                                  msg.getString("fromId")));

            Intent intent = new Intent(this, ConnectActivity.class);
            intent.putExtra(RTCClient.EXTRA_INITIATOR, false);
            intent.putExtra(RTCClient.EXTRA_TENANTID, msg.getString("tenantId"));
            intent.putExtra(RTCClient.EXTRA_FROM_USER, msg.getString("fromId"));
            intent.putExtra(RTCClient.EXTRA_TO_USER, msg.getString("toId"));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSendCall(JSONObject msg) {
        try {
            this.setConnectionParameters(new ConnectionParameters(msg.getString("tenantId"),
                                                                  msg.getString("fromId"),
                                                                  msg.getString("toId")));

            this.sendMessage(CMD_CALL, msg);

            Intent intent = new Intent(this, SendCallActivity.class);
            intent.putExtra(RTCClient.EXTRA_INITIATOR, true);

            intent.putExtra(RTCClient.EXTRA_TENANTID, msg.getString("tenantId"));
            intent.putExtra(RTCClient.EXTRA_FROM_USER, msg.getString("fromId"));
            intent.putExtra(RTCClient.EXTRA_TO_USER, msg.getString("toId"));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAnswer(JSONObject msg) {
        try {
            JSONObject data = msg.getJSONObject("data");
            JSONObject sdps = data.getJSONObject("sdp");

            SessionDescription sdp =
                    new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdps.getString("type")),
                                           sdps.getString("sdp"));

            peerConnectionClient.setRemoteDescription(sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSendAnswer(JSONObject msg) {
        this.sendMessage(CMD_ANSWER, msg);
    }

    @Override
    public void onOffer(JSONObject msg) {
        try {
            JSONObject data = msg.getJSONObject("data");
            JSONObject sdps = data.getJSONObject("sdp");

            SessionDescription sdp =
                    new SessionDescription(SessionDescription.Type.fromCanonicalForm(sdps.getString("type")),
                                           sdps.getString("sdp"));

            peerConnectionClient.setRemoteDescription(sdp);
            peerConnectionClient.createAnswer();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSendOffer(JSONObject msg) {
        this.sendMessage(CMD_OFFER, msg);
    }

    @Override
    public void onICE(JSONObject msg) {
        try {
            JSONObject data = msg.getJSONObject("data");
            IceCandidate candidate =
                    new IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate"));

            peerConnectionClient.addRemoteIceCandidate(candidate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSendICE(JSONObject msg) {
        this.sendMessage(CMD_ICE, msg);
    }

    @Override
    public void onBye(JSONObject msg) {
        EventBus.getDefault().post(new RTCEvent(RTCClient.CMD_BYE));
    }

    @Override
    public void onSendBye() {
        this.sendMessage(CMD_BYE, null);
    }

    @Override
    public void onReplyCall(JSONObject msg) {
        EventBus.getDefault().post(new RTCEvent(RTCClient.CMD_REPLY_CALL));
    }

    @Override
    public void onSendReplyCall() {
        this.sendMessage(CMD_REPLY_CALL, null);
    }

    /**
     * 发送信令函数
     */
    private void sendMessage(String cmd, JSONObject data) {
        try {
            if (connectionParameters == null) {
                Log.d("WebRTC", "发送消息前没有指定连接参数");

                return;
            }

            JSONObject json = new JSONObject();
            json.put("eventName", cmd);
            json.put("tenantId", connectionParameters.tenantId);
            json.put("fromId", connectionParameters.userId);
            json.put("toId", connectionParameters.callId);

            if (data != null) {
                json.put("data", data);
            }

            Log.d("WebRTC", json.toString());

            this.sendWebRTCMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    public void setConnectionParameters(ConnectionParameters connectionParameters) {
        this.connectionParameters = connectionParameters;
    }

    /**
     * 发送WebRTC消息
     * 
     * @param context context
     * @param message 消息
     */
    private void sendWebRTCMessage(String message) {
        Intent intent = new Intent(RTCClient.ACTION_WEBRTC_MESSAGE_BROADCAST);
        intent.putExtra("message", message);

        this.sendBroadcast(intent);
    }
}
