package com.itfsm.mqtt.service;

import android.content.Context;
import android.content.Intent;

public class MqttManager {
    /**
     * 启动MQTT
     * 
     * @param context 上下文
     * @param tenantId 企业Id
     * @param mobile 手机号
     */
    public static void start(Context context, String tenantId, String mobile) {
        start(context, tenantId, mobile, false);
    }

    /**
     * 
     * 启动MQTT
     * 
     * @param context 上下文
     * @param tenantId 企业Id
     * @param mobile 手机号
     * @param netWorkTypeChanged 网络类型是否改变
     */
    public static void start(Context context, String tenantId, String mobile, boolean netWorkTypeChanged) {
        Intent intent = new Intent(context, MqttService.class);
        intent.putExtra(MqttConstants.MQTT_EXTRA_TENANTID, tenantId);
        intent.putExtra(MqttConstants.MQTT_EXTRA_MOBILE, mobile);
        intent.putExtra(MqttConstants.MQTT_EXTRA_NETWORK_CHANGED, netWorkTypeChanged);

        context.startService(intent);
    }

    /**
     * 停止MQTT，不是停止服务，只是断开连接
     * 
     * @param context 上下文
     */
    public static void stop(Context context) {
        Intent intent = new Intent(MqttConstants.MQTT_ACTION_DISCONNECT_MQTT_SERVER);

        context.sendBroadcast(intent);
    }

    /**
     * 订阅
     * 
     * @param context 上下文
     * @param topic 主题
     * @param qos qos
     * @param message 消息
     */
    public static void publish(Context context, String topic, String qos, String message) {
        Intent intent = new Intent(MqttConstants.MQTT_ACTION_PUBLISH);
        intent.putExtra(MqttConstants.MQTT_EXTRA_TOPIC, topic);
        intent.putExtra(MqttConstants.MQTT_EXTRA_MESSAGE, message);
        intent.putExtra(MqttConstants.MQTT_EXTRA_QOS, qos);

        context.sendBroadcast(intent);
    }
}
