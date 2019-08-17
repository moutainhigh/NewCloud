package com.example.pay.ali;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by xzw on 2018-06-29.
 */

public class AliPayUtils {


    // 商户PID
    private String APP_ID = "";
    //收款支付宝用户ID
    private String SELLER_ID = "";
    // 商户私钥，pkcs8格式，长度为2048
    private String RSA_PRIVATE2 = "";

    private static final int SDK_PAY_FLAG = 1;
    public static final int SDK_CHECK_FLAG = 2;

    private WeakReference<Activity> mActivity;

    private OnAlipayListener mListener;

    /**
     * 构造方法
     * 实际开发中，根据不同的项目，设置不同的APP_ID，SELLER_ID，RSA_PRIVATE2
     * 验证签名在客户端完成
     *
     * @param APP_ID
     * @param SELLER_ID
     * @param RSA_PRIVATE2
     * @param activity
     */
    public AliPayUtils(String APP_ID, String SELLER_ID, String RSA_PRIVATE2, Activity activity) {
        this.APP_ID = APP_ID;
        this.SELLER_ID = SELLER_ID;
        this.RSA_PRIVATE2 = RSA_PRIVATE2;
        mActivity = new WeakReference<Activity>(activity);
    }

    /**
     * 构造方法
     * 验证签名已经在服务端完成
     *
     * @param activity
     */
    public AliPayUtils(Activity activity) {
        mActivity = new WeakReference<Activity>(activity);
    }


    /**
     * 请求支付,创建订单信息
     * *** 验证签名在客户端完成
     * 需要保证 APP_ID SELLER_ID RSA_PRIVATE2 正确
     *
     * @param orderTitle  交易标题
     * @param orderNumber 订单号
     * @param totalMoney  订单总金额
     * @param urlCallback 回调地址
     */
    public void requestPay(String orderTitle, String orderNumber, String totalMoney, String urlCallback) {
        if (TextUtils.isEmpty(APP_ID)) return;
        if (TextUtils.isEmpty(SELLER_ID)) return;
        if (TextUtils.isEmpty(RSA_PRIVATE2)) return;

        Map<String, String> params = OrderInfoUtil.buildOrderParamMap(APP_ID, orderTitle, orderNumber, totalMoney, SELLER_ID, urlCallback);

        String orderParam = OrderInfoUtil.buildOrderParam(params);

        String sign = OrderInfoUtil.getSign(params, RSA_PRIVATE2, true);

        final String orderInfo = orderParam + "&" + sign;

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                //使用弱引用,节省内存,使用之前确保activity不为null
                Activity activity = mActivity.get();
                if (activity == null) return;
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 请求支付
     * ***验证签名已经在服务端完成
     *
     * @param orderInfo 服务器返回的经过加签验证的字符串码
     */
    public void requestPayFromServiceSide(final String orderInfo) {
        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                //使用弱引用,确保activity不为null
                Activity activity = mActivity.get();
                if (activity == null) return;
                // 构造PayTask 对象
                PayTask alipay = new PayTask(activity);
                // 调用支付接口，获取支付结果
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //##################################################################
            if (msg.what == SDK_PAY_FLAG) {
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
//                PayResult payResult = new PayResult((String) msg.obj);
                // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                String resultInfo = payResult.getResult();
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {
                    if (mListener != null) mListener.onSuccess();
                } else {
                    // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认（小概率状态）
                    if (TextUtils.equals(resultStatus, "8000")) {
                        if (mListener != null) mListener.onWait();
                    } else if (TextUtils.equals(resultStatus, "6001")) {
                        // 用户主动取消支付
                        if (mListener != null) mListener.onCancel();
                    } else {
                        //其他值就可以判断为支付失败，或者系统返回的错误
                        if (mListener != null) mListener.onFailed();
                    }
                }
            }
        }
    };


    public void setPayListener(OnAlipayListener listener) {
        mListener = listener;
    }

    /**
     * 支付的回调
     */
    public static class OnAlipayListener {
        /**
         * 支付成功
         */
        public void onSuccess() {
        }

        /**
         * 支付取消
         */
        public void onCancel() {
        }

        /**
         * 等待确认
         */
        public void onWait() {
        }

        /**
         * 支付失败
         */
        public void onFailed() {
        }


    }

    /**
     * 判断是否安装的有支付宝软件
     *
     * @param context
     * @return
     */
    public static boolean isExist(Context context) {
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> pkgList = manager.getInstalledPackages(0);
        for (int i = 0; i < pkgList.size(); i++) {
            PackageInfo pI = pkgList.get(i);
            //com.alipay.android.app 支付宝快捷服务
            if (pI.packageName.equalsIgnoreCase("com.eg.android.AlipayGphone"))
                return true;
        }
        return false;
    }
}
