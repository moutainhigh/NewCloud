package com.bokecc.ccsskt.example.entity;

import java.io.Serializable;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class RoomDes implements Serializable {
    private int videoMode;
    private int platform;
    private String followid;
    private String desc;
    private String userid;
    private int classType;
    private int roomType;
    private int presenterBitrate;
    private int templateType;
    private String name;
    private int maxUsers;
    private int talkerBitrate;
    private int maxStreams;
    private int authtype;

    public int getVideoMode() {
        return videoMode;
    }

    public void setVideoMode(int videoMode) {
        this.videoMode = videoMode;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getFollowid() {
        return followid;
    }

    public void setFollowid(String followid) {
        this.followid = followid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getPresenterBitrate() {
        return presenterBitrate;
    }

    public void setPresenterBitrate(int presenterBitrate) {
        this.presenterBitrate = presenterBitrate;
    }

    public int getTemplateType() {
        return templateType;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getTalkerBitrate() {
        return talkerBitrate;
    }

    public void setTalkerBitrate(int talkerBitrate) {
        this.talkerBitrate = talkerBitrate;
    }

    public int getMaxStreams() {
        return maxStreams;
    }

    public void setMaxStreams(int maxStreams) {
        this.maxStreams = maxStreams;
    }

    public int getAuthtype() {
        return authtype;
    }

    public void setAuthtype(int authtype) {
        this.authtype = authtype;
    }
}
