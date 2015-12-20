package com.nvapp.video.webrtc;

public class RTCEvent {
    private String name;

    public RTCEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
