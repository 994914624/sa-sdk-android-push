package com.sensorsdata.android.push.xinge;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sensorsdata.android.push.yzk.ToolBox;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONObject;

/**
 * Created by yzk on 2019-09-16
 */

public class XinGeBroadcastReceiver extends XGPushBaseReceiver {

    private static final String TAG = "信鸽";

    @Override
    public void onRegisterResult(Context context, int code, XGPushRegisterResult message) {
        if (context == null || message == null) {
            return;
        }

        if (code == XGPushBaseReceiver.SUCCESS) {
            // 在这里拿token
            String token = message.getToken();
            Log.e(TAG, "信鸽 token:" + token);
        }
    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    /**
     * 透传消息
     */
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        if (context == null || message == null) {
            return;
        }
        String text = message.toString();
        // 获取自定义key-value
        String customContent = message.getCustomContent();
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                // key1为前台配置的key
                if (!obj.isNull("key")) {
                    String value = obj.getString("key");
                    Log.d(TAG, "get custom value:" + value);
                }
                // ...
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "++++++++++++++++透传消息:"+text);
    }

    /**
     * 通知点击回调 actionType=1为该消息被清除，actionType=0为该消息被点击。
     * 此处不能做点击消息跳转，详细方法请参照官网的Android常见问题文档
     * https://xg.qq.com/docs/android_access/android_faq.html
     */
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult message) {

        if (context == null || message == null) {
            return;
        }
        Log.e(TAG, String.format("+++++++++++++++ 通知被点击。Title：%s。Content：%s。%s", message.getTitle(), message.getContent(), message.getCustomContent()));

        // 处理通知
        try {
            String sf_data="";
            if(!TextUtils.isEmpty(message.getCustomContent())){
                sf_data = new JSONObject().optString("sf_data");
            }
            ToolBox.handlePush(message.getTitle(),message.getContent(),sf_data,context);
        } catch (Exception e) {
            e.printStackTrace();
        }



        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        String text = "";
        if (message.getActionType() == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
            // 通知在通知栏被点击啦。。。。。
            // APP自己处理点击的相关动作
            // 这个动作可以在activity的onResume也能监听，请看第3点相关内容
            text = "通知被打开 :" + message;
        } else if (message.getActionType() == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
            // 通知被清除啦。。。。
            // APP自己处理通知被清除后的相关动作
            text = "通知被清除 :" + message;
        }
        Log.d(TAG, "通知:" + text);
        Toast.makeText(context, "广播接收到通知被点击:" + message.toString(),
                Toast.LENGTH_SHORT).show();
        // 获取自定义key-value
        String customContent = message.getCustomContent();
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                // key1为前台配置的key
                if (!obj.isNull("key")) {
                    String value = obj.getString("key");
                    Log.d(TAG, "get custom value:" + value);
                }
                // ...
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult notifiShowedRlt) {
        if (context == null || notifiShowedRlt == null) {
            return;
        }
         Intent intent = new Intent();
        //通知展示
        Log.i(TAG, "+++++++++++++++++++++++++++++展示通知的回调");
    }


}
