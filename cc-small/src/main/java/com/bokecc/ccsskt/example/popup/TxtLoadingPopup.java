package com.bokecc.ccsskt.example.popup;

import android.content.Context;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.ccsskt.example.R;
import com.bokecc.ccsskt.example.base.BasePopupWindow;
import com.bokecc.ccsskt.example.base.PopupAnimUtil;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class TxtLoadingPopup extends BasePopupWindow {

    private ImageView mLoadingIcon;
    private TextView mTip;

    public TxtLoadingPopup(Context context) {
        super(context);
    }

    public TxtLoadingPopup(Context context, int width, int height) {
        super(context, width, height);
    }

    @Override
    protected void onViewCreated() {
        mTip = findViewById(R.id.id_loading_tip);
    }

    public void setTipValue(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        mTip.setText(value);
    }

    @Override
    protected int getContentView() {
        return R.layout.txt_loading_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }
}
