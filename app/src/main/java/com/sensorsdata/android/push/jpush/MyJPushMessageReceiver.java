/*
 * Created by zhangwei on 2019/05/17.
 * Copyright 2015－2019 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorsdata.android.push.jpush;

import android.content.Context;

import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFLogger;
import com.sensorsdata.android.push.SFUtils;
import com.sensorsdata.android.push.yzk.ToolBox;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 自定义 JPush message 接收器,包括操作 tag/alias 的结果返回(仅仅包含 tag/alias 新接口部分)
 * 如果使用该类同时为了自己老的 BroadcastReceiver 能否收到广播，则必须在回调方法中调用 super 方法。
 */
public class MyJPushMessageReceiver extends JPushMessageReceiver {
    private static final String TAG = "MyJPushMessageReceiver";

    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        SFLogger.d(TAG, "onTagOperatorResult:" + jPushMessage);
        TagAliasOperatorHelper.getInstance().onTagOperatorResult(context, jPushMessage);
        super.onTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        SFLogger.d(TAG, "onCheckTagOperatorResult:" + jPushMessage);
        TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context, jPushMessage);
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        SFLogger.d(TAG, "onAliasOperatorResult:" + jPushMessage);
        TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context, jPushMessage);
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        SFLogger.d(TAG, "onMobileNumberOperatorResult");
        TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context, jPushMessage);
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        SFLogger.d(TAG, "onMessage:" + customMessage);
        super.onMessage(context, customMessage);
    }

    /**
     * 打开极光通知
     */
    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        SFLogger.d(TAG, "onNotifyMessageOpened");
        if(context ==null || message ==null)return;
        super.onNotifyMessageOpened(context, message);
        /**
         * 对于极光 3.0.0 以后的版本，可以在这里处理通知打开的操作，此处仅仅演示了 sf_data 推送的相关字段，注意，如果你有原有的逻辑也有相关处理
         *      * 的逻辑，你需要做一定的兼容处理。
         */
        //SFUtils.trackAppOpenNotification(context, SFUtils.readSFConfig(notificationMessage.notificationExtras), String.valueOf(notificationMessage.notificationId), notificationMessage.notificationTitle, notificationMessage.notificationContent);
        //SFUtils.handleSFConfig(context, SFUtils.readSFConfig(notificationMessage.notificationExtras));
//        SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_CONTENT, message.toString());
//        ToolBox.trackAppOpenNotification(message.notificationExtras, message.notificationTitle, message.notificationContent);
        // 处理通知
        try {
            String sf_data = new JSONObject(message.notificationExtras).optString("sf_data");
            ToolBox.handlePush(message.notificationTitle,message.notificationContent,sf_data,context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        SFLogger.d(TAG, "onNotifyMessageArrived");
        super.onNotifyMessageArrived(context, notificationMessage);
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage notificationMessage) {
        SFLogger.d(TAG, "onNotifyMessageDismiss");
        super.onNotifyMessageDismiss(context, notificationMessage);
    }

    @Override
    public void onRegister(Context context, String s) {
        SFLogger.d(TAG, "onRegister:" + s);
        super.onRegister(context, s);
        /**
         * 对于极光 3.0.0 以后的版本，可以在这里获取 RegisterID
         */
        //SFUtils.profilePushId(context, s);
    }

    @Override
    public void onConnected(Context context, boolean b) {
        SFLogger.d(TAG, "onConnected");
        super.onConnected(context, b);
    }

    @Override
    public void onCommandResult(Context context, CmdMessage cmdMessage) {
        SFLogger.d(TAG, "onCommandResult");
        super.onCommandResult(context, cmdMessage);
    }
}

