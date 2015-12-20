package com.itfsm.video.webrtc;

import java.util.List;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

/**
 * WebRTC客户端
 */
public interface RTCClient {
    /**
     * 消息发送广播属性
     */
    public static final String ACTION_WEBRTC_MESSAGE_BROADCAST = "com.itfsm.webrtc.client.message.broadcast";
    public static final String ACTION_WEBRTC_MESSAGE_RECEIVE = "com.itfsm.webrtc.client.message.receiver";
    /* 视频命令服务 */
    public static final String VIDEO_CMD_SERVICE = "com.itfsm.video.webrtc.RTCService";

    public static final String CMD_ONLINE_LIST = "online_list";

    public static final String CMD_CALL = "call";
    public static final String CMD_SEND_CALL = "send_call";
    public static final String CMD_REPLY_CALL = "reply_call";
    public static final String CMD_SEND_REPLY_CALL = "send_reply_call";
    public static final String CMD_BYE = "bye";
    public static final String CMD_SEND_BYE = "send_bye";
    public static final String CMD_OFFER = "offer";
    public static final String CMD_SEND_OFFER = "send_offer";
    public static final String CMD_ANSWER = "answer";
    public static final String CMD_SEND_ANSWER = "send_answer";
    public static final String CMD_ICE = "ice_candidate";
    public static final String CMD_SEND_ICE = "send_ice_candidate";

    public static final String CMD_ERROR_CALL = "error_call";
    public static final String CMD_ADD_USER = "add_user";
    public static final String CMD_REMOVE_USER = "remove_user";

    /* 是否发起者 */
    public final static String EXTRA_INITIATOR = "initiator";
    /* 企业Id */
    public final static String EXTRA_TENANTID = "tenantId";
    /* 信号发起用户 */
    public final static String EXTRA_FROM_USER = "fromId";
    /* 信号接收用户 */
    public final static String EXTRA_TO_USER = "toId";

    /**
     * Struct holding the connection parameters.
     */
    public static class ConnectionParameters {
        public final String tenantId;
        public final String userId;
        public final String callId;

        public ConnectionParameters(String tenantId, String userId, String callId) {
            this.tenantId = tenantId;
            this.userId = userId;
            this.callId = callId;
        }
    }

    /**
     * receive a call commnad
     */
    public void onCall(JSONObject msg);

    public void onSendCall(JSONObject msg);

    /**
     * receive a answer command
     */
    public void onAnswer(JSONObject msg);

    public void onSendAnswer(JSONObject msg);

    /**
     * receive a offer command
     */
    public void onOffer(JSONObject msg);

    public void onSendOffer(JSONObject msg);

    /**
     * receive a ICE command
     */
    public void onICE(JSONObject msg);

    public void onSendICE(JSONObject msg);

    /**
     * receive a Bye command
     */

    public void onBye(JSONObject msg);

    public void onSendBye();

    /**
     * receive a Reply Call command
     */
    public void onReplyCall(JSONObject msg);

    public void onSendReplyCall();

    /**
     * 信号参数
     */
    public static class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        public final boolean initiator;
        public final SessionDescription offerSdp;
        public final List<IceCandidate> iceCandidates;

        public SignalingParameters(List<PeerConnection.IceServer> iceServers,
                                   boolean initiator,
                                   SessionDescription offerSdp,
                                   List<IceCandidate> iceCandidates) {
            this.iceServers = iceServers;
            this.initiator = initiator;
            this.offerSdp = offerSdp;
            this.iceCandidates = iceCandidates;
        }
    }
}
