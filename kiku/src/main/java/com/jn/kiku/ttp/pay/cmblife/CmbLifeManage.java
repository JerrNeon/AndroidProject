package com.jn.kiku.ttp.pay.cmblife;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.jn.kiku.common.api.ILogToastView;
import com.jn.kiku.utils.LogUtils;
import com.jn.kiku.utils.ToastUtil;

import java.util.Map;

/**
 * @version V2.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (招行分期支付管理)
 * @create by: chenwei
 * @date 2018/4/3 16:43
 */
public class CmbLifeManage implements ILogToastView, ICmblifeListener {

    private static final String payRequestCode = "CmbLifePay";//发起支付请求code
    private static final String payResultCode = "respCode";//支付结果code
    private static final String paySuccessResultCode = "1000";//支付成功结果code
    private static final String payCancelResultCode = "2000";//支付取消结果code

    private Context mContext = null;
    private static CmbLifeManage instance = null;
    private CmbLifePayResultListener mCmbLifePayResultListener = null;

    private CmbLifeManage(Context context) {
        mContext = context;
    }

    public static synchronized CmbLifeManage getInstance(Context context) {
        if (instance == null)
            instance = new CmbLifeManage(context.getApplicationContext());
        return instance;
    }

    /**
     * 支付
     *
     * @param context  上下文
     * @param protocol 支付参数(掌上生活cmblife协议)
     */
    public void pay(Context context, String protocol, Class<?> callBackActivity, CmbLifePayResultListener listener) {
        boolean isInstall = CmblifeSDK.isInstall(context);
        if (isInstall) {
            mCmbLifePayResultListener = listener;
            CmblifeSDK.startCmblife(mContext, protocol, callBackActivity, payRequestCode);
        } else {
            showAppNotInstallToast();
        }
    }

    @Override
    public void onCmblifeCallBack(String requestCode, Map<String, String> resultMap) {
        if (payRequestCode.equals(requestCode)) {
            onCmblifeCallBack(resultMap);
        }
    }

    /**
     * 支付结果回调
     *
     * @param resultMap
     */
    public void onCmblifeCallBack(Map<String, String> resultMap) {
        String respCode = resultMap.get(payResultCode);
        if (paySuccessResultCode.equals(respCode)) {
            ToastUtil.showToast(mContext, "支付成功");
            if (mCmbLifePayResultListener != null)
                mCmbLifePayResultListener.onSuccess();
        } else if (payCancelResultCode.equals(respCode)) {
            ToastUtil.showToast(mContext, "取消支付");
        } else {
            ToastUtil.showToast(mContext, "支付失败");
            if (mCmbLifePayResultListener != null)
                mCmbLifePayResultListener.onFailure();
        }
    }

    /**
     * 处理回调
     *
     * @param intent
     */
    public void handleCallBack(Intent intent) {
        try {
            CmblifeSDK.handleCallBack(this, intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (mCmbLifePayResultListener != null)
                mCmbLifePayResultListener.onFailure();
            ToastUtil.showToast(mContext, "支付失败");
        }
    }


    @Override
    public void logI(String message) {
        LogUtils.i(String.format(messageFormat, getClass().getSimpleName(), message));
    }

    @Override
    public void logE(String message) {
        LogUtils.e(String.format(messageFormat, getClass().getSimpleName(), message));
    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showToast(String message, int duration) {

    }

    /**
     * 显示掌上生活App未安装的提示
     */
    private void showAppNotInstallToast() {
        Toast.makeText(mContext, "您还未安装掌上生活,请安装掌上生活客户端", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy() {
        if (mCmbLifePayResultListener != null)
            mCmbLifePayResultListener = null;
    }

    /**
     * 招行支付结果
     */
    public interface CmbLifePayResultListener {
        void onSuccess();

        void onFailure();
    }
}
