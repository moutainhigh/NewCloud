package com.bokecc.ccsskt.example.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.bokecc.ccsskt.example.activity.DirectionActivity;
import com.bokecc.ccsskt.example.activity.ValidateActivity;
import com.bokecc.ccsskt.example.entity.RoomDes;
import com.bokecc.sskt.CCInteractSession;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static com.bokecc.ccsskt.example.CCApplication.mAreaCode;
import static com.bokecc.ccsskt.example.global.Config.mNickName;
import static com.bokecc.ccsskt.example.global.Config.mPwd;
import static com.bokecc.ccsskt.example.global.Config.mRole;
import static com.bokecc.ccsskt.example.global.Config.mRoomDes;
import static com.bokecc.ccsskt.example.global.Config.mRoomId;
import static com.bokecc.ccsskt.example.global.Config.mUserId;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class ParseMsgUtil {

    private ParseMsgUtil() {
    }

    protected CCInteractSession mInteractSession = CCInteractSession.getInstance();

    public static void parseUrl(final Context context, String name, String pwd, String url, final ParseCallBack callBack) {
        try {
            if (callBack != null) {
                callBack.onStart();
            }
            if (TextUtils.isEmpty(url)) {
                throw new NullPointerException("课堂链接错误");
            }
            Log.i(TAG, url);
            mNickName = name;
            mPwd = pwd;
            String arr[] = url.split("\\?|&");
            if (arr[1].split("=")[0].equals("userid")) {
                mUserId = arr[1].split("=")[1];
                mRoomId = arr[2].split("=")[1];
            } else {
                mRoomId = arr[1].split("=")[1];
                mUserId = arr[2].split("=")[1];
            }
            String hosts[] = (arr[0].substring("https://".length(), arr[0].length())).split("/");
            mRole = hosts[hosts.length - 1];
            mRole = mRole.substring(0, mRole.length());
            CCInteractSession.getInstance().getRoomMsg(mRoomId, new CCInteractSession.AtlasCallBack<String>() {
                @Override
                public void onSuccess(String des) {
                    try {
                        mRoomDes = parseDes(des);
                        switch (mRole) {
                            case "presenter":
                                if (callBack != null) {
                                    callBack.onSuccess();
                                }
                                DirectionActivity.startSelf(context, CCInteractSession.PRESENTER, mUserId, mRoomId, mRoomDes);
//                                ValidateActivity.startSelf(context, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.PRESENTER, false);
                                break;
                            case "talker":
                                if (callBack != null) {
                                    callBack.onSuccess();
                                }
                                DirectionActivity.startSelf(context, CCInteractSession.TALKER, mUserId, mRoomId, mRoomDes);
                                break;
                            default:
                                if (callBack != null) {
                                    callBack.onFailure("请使用直播客户端启动");
                                }
                                break;
                        }
                    } catch (Exception e) {
                        if (callBack != null) {
                            callBack.onFailure(e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(final String err) {
                    if (callBack != null) {
                        callBack.onFailure(err);
                    }
                }
            });
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onFailure("课堂链接错误");
            }
        }
    }

    public static void parseUrl(final Context context, String url, final ParseCallBack callBack) {
        try {
            if (callBack != null) {
                callBack.onStart();
            }
            if (TextUtils.isEmpty(url)) {
                throw new NullPointerException("课堂链接错误");
            }
            Log.i(TAG, url);
            String arr[] = url.split("\\?|&");
            if (arr[1].split("=")[0].equals("userid")) {
                mUserId = arr[1].split("=")[1];
                mRoomId = arr[2].split("=")[1];
            } else {
                mRoomId = arr[1].split("=")[1];
                mUserId = arr[2].split("=")[1];
            }
            String hosts[] = (arr[0].substring("https://".length(), arr[0].length())).split("/");
            mRole = hosts[hosts.length - 1];
            mRole = mRole.substring(0, mRole.length());
            CCInteractSession.getInstance().getRoomMsg(mRoomId, new CCInteractSession.AtlasCallBack<String>() {
                @Override
                public void onSuccess(String des) {
                    try {
                        mRoomDes = parseDes(des);
                        switch (mRole) {
                            case "presenter":
                                if (callBack != null) {
                                    callBack.onSuccess();
                                }
                                DirectionActivity.startSelf(context, CCInteractSession.PRESENTER, mUserId, mRoomId, mRoomDes);
//                                ValidateActivity.startSelf(context, mRoomDes.getName(), mRoomDes.getDesc(), mRoomId, mUserId, CCInteractSession.PRESENTER, false);
                                break;
                            case "talker":
                                if (callBack != null) {
                                    callBack.onSuccess();
                                }
                                DirectionActivity.startSelf(context, CCInteractSession.TALKER, mUserId, mRoomId, mRoomDes);
                                break;
                            default:
                                if (callBack != null) {
                                    callBack.onFailure("请使用直播客户端启动");
                                }
                                break;
                        }
                    } catch (Exception e) {
                        if (callBack != null) {
                            callBack.onFailure(e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(final String err) {
                    if (callBack != null) {
                        callBack.onFailure(err);
                    }
                }
            });
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onFailure("课堂链接错误");
            }
        }
    }



    private static RoomDes parseDes(String des) throws JSONException {
        JSONObject obj = new JSONObject(des);
        int videoMode = obj.getInt("video_mode");
        int platform = obj.getInt("platform");
        String followid = obj.getString("is_follow");
        String desc = obj.getString("desc");
        String userid = obj.getString("userid");
        int classType = obj.getInt("classtype");
        int roomType = obj.getInt("room_type");
        int presenterBitrate = obj.getInt("publisher_bitrate");
        int templateType = obj.getInt("templatetype");
        String name = obj.getString("name");
        int maxUsers = obj.getInt("max_users");
        int talkerBitrate = obj.getInt("talker_bitrate");
        int maxStreams = obj.getInt("max_streams");
        int authType = obj.getInt("authtype");
        RoomDes roomDes = new RoomDes();
        roomDes.setVideoMode(videoMode);
        roomDes.setPlatform(platform);
        roomDes.setFollowid(followid);
        roomDes.setDesc(desc);
        roomDes.setUserid(userid);
        roomDes.setClassType(classType);
        roomDes.setRoomType(roomType);
        roomDes.setPresenterBitrate(presenterBitrate);
        roomDes.setTemplateType(templateType);
        roomDes.setName(name);
        roomDes.setMaxUsers(maxUsers);
        roomDes.setTalkerBitrate(talkerBitrate);
        roomDes.setMaxStreams(maxStreams);
        roomDes.setAuthtype(authType);
        return roomDes;
    }

    public interface ParseCallBack {
        void onStart();

        void onSuccess();

        void onFailure(String err);
    }

}
