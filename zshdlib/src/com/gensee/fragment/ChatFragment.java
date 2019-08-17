package com.gensee.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gensee.entity.UserInfo;
import com.gensee.player.Player;
import com.gensee.rtmpresourcelib.R;
import com.gensee.view.GSImplChatView;

@SuppressLint("ValidFragment")
public class ChatFragment extends Fragment {

    private Player mPlayer;
    private GSImplChatView mGSImplChatView;
    private UserInfo mUserInfo;

    public ChatFragment(Player player) {

        this.mPlayer = player;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View main = inflater.inflate(R.layout.imchat, null);
        mGSImplChatView = (GSImplChatView) main.findViewById(R.id.impchatview);
        //	mGSImplChatView.setChatTarget(mUserInfo);
        mPlayer.setGSChatView(mGSImplChatView);
        return main;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
    }

    public void setChatPerson(UserInfo userInfo) {

        mUserInfo = userInfo;
        if (mGSImplChatView != null) {
            // mGSImplChatView.setChatTarget(userInfo);
            mPlayer.setGSChatView(mGSImplChatView);
        }
    }

}
