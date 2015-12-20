/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 */
package com.itfsm.mqtt.service;

import java.io.File;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

class MqttConnection implements MqttCallback {

    // Strings for Intents etc..
    private static final String TAG = "MqttConnection";
    // Error status messages
    private static final String NOT_CONNECTED = "not connected";
    // Invocation context
    private static final String INVOCATION_CONTEXT = "LEXLOO";

    // fields for the connection definition
    private String serverURI;

    public String getServerURI() {
        return serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }

    private String clientId;
    private String tenantId;
    private String mobile;
    private MqttClientPersistence persistence = null;
    private MqttConnectOptions connectOptions;

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    public void setConnectOptions(MqttConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    // our client object - instantiated on connect
    private MqttAsyncClient myClient = null;

    // our (parent) service object
    private MqttService service = null;

    private volatile boolean disconnected = true;
    private boolean cleanSession = true;

    // Indicate this connection is connecting or not.
    // This variable uses to avoid reconnect multiple times.
    private volatile boolean isConnecting = false;

    private WakeLock wakelock = null;
    private String wakeLockTag = null;

    /**
     * Constructor - create an MqttConnection to communicate with MQTT server
     * 
     * @param service our "parent" service - we make callbacks to it
     * @param serverURI the URI of the MQTT server to which we will connect
     * @param clientId the name by which we will identify ourselves to the MQTT server
     * @param persistence the persistence class to use to store in-flight message. If null then the default persistence
     *            mechanism is used
     */
    MqttConnection(MqttService service,
                   String serverURI,
                   String clientId,
                   String tenantId,
                   String mobile,
                   MqttClientPersistence persistence) {
        this.serverURI = serverURI.toString();
        this.service = service;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.mobile = mobile;
        this.persistence = persistence;

        StringBuffer buff = new StringBuffer(this.getClass().getCanonicalName());
        buff.append(" ");
        buff.append(clientId);
        buff.append(" ");
        buff.append("on host ");
        buff.append(serverURI);
        wakeLockTag = buff.toString();
    }

    // The major API implementation follows
    /**
     * Connect to the server specified when we were instantiated
     * 
     * @param options timeout, etc
     */
    public void connect(MqttConnectOptions options) {
        connectOptions = options;
        if (options != null) {
            cleanSession = options.isCleanSession();
        }

        try {
            if (persistence == null) {
                // ask Android where we can put files
                File myDir = service.getExternalFilesDir(TAG);

                if (myDir == null) {
                    // No external storage, use internal storage instead.
                    myDir = service.getDir(TAG, Context.MODE_PRIVATE);

                    if (myDir == null) {
                        Log.e(TAG, "Error! No external and internal storage available");
                        return;
                    }
                }

                // use that to setup MQTT client persistence storage
                persistence = new MqttDefaultFilePersistence(myDir.getAbsolutePath());
            }

            IMqttActionListener listener = new MqttConnectionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    doAfterConnectSuccess();

                    innerSubscribe(tenantId, mobile);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    doAfterConnectFail();
                }
            };

            if (myClient != null) {
                if (isConnecting) {
                    return;
                } else if (!disconnected) {
                    doAfterConnectSuccess();
                } else {
                    setConnectingState(true);
                    myClient.connect(connectOptions, INVOCATION_CONTEXT, listener);
                }
            }

            // if myClient is null, then create a new connection
            else {
                myClient = new MqttAsyncClient(serverURI, clientId, persistence, new AlarmPingSender(service));
                myClient.setCallback(this);

                setConnectingState(true);
                myClient.connect(connectOptions, INVOCATION_CONTEXT, listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void innerSubscribe(String tenantId, String mobile) {
        final String[] topics = {"WebRTC/"
                                 + tenantId
                                 + "/"
                                 + mobile,
                                 "IM/" + tenantId + "/" + mobile,
                                 "PUSH/" + tenantId + "/" + mobile};
        subscribe(topics, new int[]{0, 0, 0}, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "subscribe success");
                Intent intent = new Intent(MqttConstants.MQTT_ACTION_CONNECTED_SUCCESS_CALLBACK);

                service.sendBroadcast(intent);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Intent intent = new Intent(MqttConstants.MQTT_ACTION_CONNECTED_FAILURE_CALLBACK);

                service.sendBroadcast(intent);
            }
        });
    }

    private void doAfterConnectSuccess() {
        // since the device's cpu can go to sleep, acquire a wakelock and drop
        // it later.

        acquireWakeLock();
        setConnectingState(false);
        disconnected = false;
        releaseWakeLock();
    }

    private void doAfterConnectFail() {
        System.out.println("连接失败");
        //
        acquireWakeLock();
        disconnected = true;
        setConnectingState(false);
        releaseWakeLock();
    }

    /**
     * Close connection from the server
     * 
     */
    void close() {
        try {
            if (myClient != null) {
                myClient.close();
            }

            // Added by lexloo
            if (this.persistence != null) {
                this.persistence.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnect from the server
     */
    void disconnect() {
        disconnected = true;
        if ((myClient != null) && (myClient.isConnected())) {
            IMqttActionListener listener = new MqttConnectionListener();
            try {
                myClient.disconnect(INVOCATION_CONTEXT, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, NOT_CONNECTED);
        }

        releaseWakeLock();
    }

    /**
     * @return true if we are connected to an MQTT server
     */
    public boolean isConnected() {
        if (myClient != null)
            return myClient.isConnected();
        return false;
    }

    public IMqttDeliveryToken publish(String topic,
                                      byte[] payload,
                                      int qos,
                                      boolean retained,
                                      IMqttActionListener listener) {
        IMqttDeliveryToken sendToken = null;

        if ((myClient != null) && (myClient.isConnected())) {
            try {
                MqttMessage message = new MqttMessage(payload);
                message.setQos(qos);
                message.setRetained(retained);
                sendToken = myClient.publish(topic, payload, qos, retained, INVOCATION_CONTEXT, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, NOT_CONNECTED);
        }

        return sendToken;
    }

    /**
     * Subscribe to one or more topics
     * 
     * @param topic a list of possibly wildcarded topic names
     * @param qos requested quality of service for each topic
     */
    public void subscribe(final String[] topic, final int[] qos, IMqttActionListener listener) {
        if ((myClient != null) && (myClient.isConnected())) {
            try {
                myClient.subscribe(topic, qos, INVOCATION_CONTEXT, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, NOT_CONNECTED);
        }
    }

    /**
     * Unsubscribe from one or more topics
     * 
     * @param topic a list of possibly wildcarded topic names
     */
    void unsubscribe(final String[] topic) {
        if ((myClient != null) && (myClient.isConnected())) {
            IMqttActionListener listener = new MqttConnectionListener();
            try {
                myClient.unsubscribe(topic, INVOCATION_CONTEXT, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, NOT_CONNECTED);
        }
    }

    /**
     * Get tokens for all outstanding deliveries for a client
     * 
     * @return an array (possibly empty) of tokens
     */
    public IMqttDeliveryToken[] getPendingDeliveryTokens() {
        return myClient.getPendingDeliveryTokens();
    }

    // Implement MqttCallback
    /**
     * Callback for connectionLost
     * 
     * @param why the exeception causing the break in communications
     */
    @Override
    public void connectionLost(Throwable why) {
        Log.e(TAG, "失去连接");
        disconnected = true;
        Log.e(TAG, "连接丢失，启动重连");

        Intent intent = new Intent(MqttConstants.MQTT_ACTION_START_MQTT_SERVER);
        this.service.sendBroadcast(intent);
        // try {
        // myClient.disconnect(null, new IMqttActionListener() {
        //
        // @Override
        // public void onSuccess(IMqttToken asyncActionToken) {
        // // No action
        // }
        //
        // @Override
        // public void onFailure(IMqttToken asyncActionToken, Throwable
        // exception) {
        // // No action
        // }
        // });
        // } catch (Exception e) {
        // // ignore it - we've done our best
        // }

        releaseWakeLock();
    }

    /**
     * Callback to indicate a message has been delivered (the exact meaning of "has been delivered" is dependent on the
     * QOS value)
     * 
     * @param messageToken the messge token provided when the message was originally sent
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken messageToken) {
        Log.d(TAG, "发送完成");
    }

    /**
     * Callback when a message is received
     * 
     * @param topic the topic on which the message was received
     * @param message the message itself
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    	Log.d(TAG, "Receive message on topic:" + topic);
    	
        // 广播收到的消息
        Intent intent = null;
        if (topic.startsWith("IM")) {
            intent = new Intent(MqttConstants.MQTT_ACTION_RECEIVE_IM_MESSAGE);
            intent.putExtra(MqttConstants.MQTT_EXTRA_MESSAGE, new String(message.getPayload(), "UTF-8"));
        } else if (topic.startsWith("PUSH")) {
            intent = new Intent(MqttConstants.MQTT_ACTION_RECEIVE_PUSH_MESSAGE);
            intent.putExtra(MqttConstants.MQTT_EXTRA_MESSAGE, new String(message.getPayload(), "UTF-8"));
        } else if (topic.startsWith("WebRTC")) {
            intent = new Intent(MqttConstants.MQTT_ACTION_RECEIVE_WEBRTC_MESSAGE);
            intent.putExtra(MqttConstants.MQTT_EXTRA_MESSAGE, new String(message.getPayload(), "UTF-8"));
        }

        this.service.sendBroadcast(intent);
    }

    /**
     * Acquires a partial wake lock for this client
     */
    private void acquireWakeLock() {
        if (wakelock == null) {
            PowerManager pm = (PowerManager) service.getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
        }
        wakelock.acquire();

    }

    /**
     * Releases the currently held wake lock for this client
     */
    private void releaseWakeLock() {
        if (wakelock != null && wakelock.isHeld()) {
            wakelock.release();
        }
    }

    /**
     * General-purpose IMqttActionListener for the Client context
     * <p>
     * Simply handles the basic success/failure cases for operations which don't return results
     * 
     */
    private class MqttConnectionListener implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {}

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

        }
    }

    /**
     * Receive notification that we are offline<br>
     * if cleanSession is true, we need to regard this as a disconnection
     */
    void offline() {
        Log.d(TAG, String.valueOf(!disconnected && !cleanSession));
        if (!disconnected && !cleanSession) {
            Exception e = new Exception("Android offline");
            connectionLost(e);
        }
    }

    /**
     * Reconnect<br>
     * Only appropriate if cleanSession is false and we were connected. Declare as synchronized to avoid multiple calls
     * to this method to send connect multiple times
     */
    synchronized void reconnect() {
        if (isConnecting) {
            return;
        }

        if (!service.isOnline()) {
            return;
        }

        if (disconnected && !cleanSession) {
            try {

                IMqttActionListener listener = new MqttConnectionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // since the device's cpu can go to sleep, acquire a
                        // wakelock and drop it later.
                        doAfterConnectSuccess();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        doAfterConnectFail();

                    }
                };

                Log.d(TAG, "重新连接...");
                myClient.connect(connectOptions, null, listener);
                setConnectingState(true);
            } catch (MqttException e) {
                setConnectingState(false);
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param isConnecting
     */
    synchronized void setConnectingState(boolean isConnecting) {
        this.isConnecting = isConnecting;
    }

    /**
     * 是否正在连接中
     * 
     * @return 如果正在连接，返回ture
     */
    public boolean isConnecting() {
        return this.isConnecting;
    }
}
