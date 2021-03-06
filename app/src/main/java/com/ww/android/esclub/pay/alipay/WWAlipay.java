package com.ww.android.esclub.pay.alipay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alipay.sdk.app.PayTask;

import java.util.Map;

/**
 * 支付宝支付
 */
public class WWAlipay {
    private static final int SDK_PAY_FLAG = 1;
    private AlipayListener mListener;
    private Activity mActy;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        mListener.onPaySuccess(resultInfo);
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        mListener.onPayFail(resultStatus, resultInfo);
                    }
                    break;
                }

                default:
                    break;
            }
        }
    };


    private WWAlipay(Activity acty, AlipayListener listener) {
        this.mActy = acty;
        this.mListener = listener;
    }

    public static WWAlipay cretateAlipay(Activity acty, AlipayListener listener) {
        return new WWAlipay(acty, listener);
    }

    /**
     * 支付宝支付业务
     */
    public void pay(final String orderInfo) {
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(mActy);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public static interface AlipayListener {
        void onPaySuccess(String info);

        void onPayFail(String status, String errInfo);
    }
}
