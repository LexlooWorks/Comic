package com.itfsm.mqtt.service;

public interface MqttConstants {
    String MQTT_ACTION_PUBLISH = "com.itfsm.mqtt.action.publish";
    /**
     * 启动MQTT服务命令
     */
    String MQTT_ACTION_START_MQTT_SERVER = "com.itfsm.mqtt.action.cmd.start";
    /**
     * 断开连接命令
     */
    String MQTT_ACTION_DISCONNECT_MQTT_SERVER = "com.itfsm.mqtt.action.cmd.disconnect";
    /**
     * 服务器连接成功
     */
    String MQTT_ACTION_CONNECTED_MQTT_SERVER = "com.itfsm.mqtt.action.connected";

    /**
     * IM消息
     */
    String MQTT_ACTION_RECEIVE_IM_MESSAGE = "com.itfsm.mqtt.action.receive.message.im";
    /**
     * 推送消息
     */
    String MQTT_ACTION_RECEIVE_PUSH_MESSAGE = "com.itfsm.mqtt.action.receive.message.push";
    /**
     * WebRTC消息
     */
    String MQTT_ACTION_RECEIVE_WEBRTC_MESSAGE = "com.itfsm.mqtt.action.receive.message.webrtc";

    String MQTT_ACTION_PUBLISH_CALLBACK = "com.itfsm.mqtt.action.publish.callback";
    /**
     * 连接成功回调
     */
    String MQTT_ACTION_CONNECTED_SUCCESS_CALLBACK = "com.itfsm.mqtt.action.connected.success.callback";
    /**
     * 连接失败回调
     */
    String MQTT_ACTION_CONNECTED_FAILURE_CALLBACK = "com.itfsm.mqtt.action.connected.failure.callback";

    String MQTT_EXTRA_TOPIC = "com.itfsm.mqtt.extra.topic";
    String MQTT_EXTRA_QOS = "com.itfsm.mqtt.extra.qos";
    String MQTT_EXTRA_MESSAGE = "com.itfsm.mqtt.extra.message";
    String MQTT_EXTRA_TENANTID = "com.itfsm.mqtt.extra.tenantid";
    String MQTT_EXTRA_MOBILE = "com.itfsm.mqtt.extra.mobile";
    String MQTT_EXTRA_NETWORK_CHANGED = "com.itfsm.mqtt.extra.network.changed";
}
