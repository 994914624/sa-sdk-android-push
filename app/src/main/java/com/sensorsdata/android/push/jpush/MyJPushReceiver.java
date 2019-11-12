package com.sensorsdata.android.push.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFLogger;
import com.sensorsdata.android.push.SFUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

public class MyJPushReceiver extends BroadcastReceiver {
    private static final String TAG = "MyJPushReceiver";
    private static String sfData;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            SFLogger.d(TAG, "[MyJPushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
            /**
             * 此处仅仅演示了 sf_data 推送的相关字段，注意，如果你有原有的逻辑也有相关处理的逻辑，你需要做一定的兼容处理。
             */
            readBundleConfig(bundle);
            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
                SFLogger.d(TAG, "[MyJPushReceiver] 接收 Registration Id : " + regId);
                /*
                 * 注册成功后需要保存 registerId
                 */
                SFUtils.profilePushId(context, SFConstant.PUSH_ID_JPUSH, regId);
                /*
                 * 将收到的信息发送到页面显示，测试使用
                 */
                SFUtils.sendBroadcast(context, SFConstant.PUSH_ID_JPUSH, regId);
                SFUtils.savePushId(context, SFConstant.PUSH_ID_JPUSH, regId);
            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
                SFLogger.d(TAG, "[MyJPushReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
                processCustomMessage(context, bundle);
            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                SFLogger.d(TAG, "[MyJPushReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                SFLogger.d(TAG, "[MyJPushReceiver] 接收到推送下来的通知的 ID: " + notifactionId);
                // 处理 Extra 字段
            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
                /**
                 * 如果开发者在 AndroidManifest.xml 里未配置此 receiver action，那么，
                 * SDK 会默认打开应用程序的主 Activity，相当于用户点击桌面图标的效果。
                 *
                 * 如果开发者在 AndroidManifest.xml 里配置了此 receiver action，那么，
                 * 当用户点击通知时，SDK 不会做动作。开发者应该在自己写的 BroadcastReceiver 类里处理，比如打开某 Activity 。
                 */
                SFLogger.d(TAG, "[MyJPushReceiver] 用户点击打开了通知");
                /*
                 * 用户点击通知，需要处理两个操作：
                 * 1. 发送 AppOpenNotification 事件;
                 * 2. 执行点击后的操作;
                 */
                int messageId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
                String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                SFUtils.trackAppOpenNotification(context, sfData, String.valueOf(messageId), title, content);
                SFUtils.handleSFConfig(context, sfData);
                /*
                 * 将收到的信息发送到页面显示，测试使用
                 */
                SFUtils.sendBroadcast(context, SFConstant.PUSH_TYPE_JPUSH, sfData);
            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
                SFLogger.d(TAG, "[MyJPushReceiver] 用户收到到 RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

            } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
                SFLogger.d(TAG, "[MyJPushReceiver]" + intent.getAction() + " connected state change to " + connected);
                if (connected) {
                    /*
                     * 将收到的信息发送到页面显示，测试使用
                     */
                    SFUtils.sendBroadcast(context, SFConstant.PUSH_ID_JPUSH, JPushInterface.getRegistrationID(context));
                    SFUtils.savePushId(context, SFConstant.PUSH_ID_JPUSH, JPushInterface.getRegistrationID(context));
                }
            } else {
                SFLogger.d(TAG, "[MyJPushReceiver] Unhandled intent - " + intent.getAction());
            }
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * 处理接收的消息
     *
     * @param context Context
     * @param bundle Bundle
     */
    private void processCustomMessage(Context context, Bundle bundle) {

    }

    private static void readBundleConfig(Bundle bundle) {
        try {
            Set<String> stringSet = bundle.keySet();
            if (stringSet != null && stringSet.contains(JPushInterface.EXTRA_EXTRA)) {
                String value = bundle.getString(JPushInterface.EXTRA_EXTRA);
                sfData = SFUtils.readSFConfig(value);
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
                    SFLogger.d(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    SFLogger.d(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.get(key));
            }
        }
        return sb.toString();
    }
}
