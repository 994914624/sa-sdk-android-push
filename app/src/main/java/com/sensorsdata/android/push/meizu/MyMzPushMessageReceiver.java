package com.sensorsdata.android.push.meizu;


import android.content.Context;
import android.util.Log;

import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.handler.MzPushMessage;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;

/**
 * Created by yzk on 2019-12-19
 */

public class MyMzPushMessageReceiver extends MzPushMessageReceiver {

    /**
     *
     * 当用户点击通知栏消息后会在此方法回调
     */
    @Override
    public void onNotificationClicked(Context context, MzPushMessage mzPushMessage) {
        super.onNotificationClicked(context, mzPushMessage);
        if(mzPushMessage == null)return;
        Log.e("魅族","onNotificationClicked："+mzPushMessage.toString());
    }

    @Override
    public void onNotifyMessageArrived(Context context, String s) {
        super.onNotifyMessageArrived(context, s);
        Log.e("魅族","onNotificationClicked："+s);
    }

    @Override
    public void onRegisterStatus(Context context, RegisterStatus registerStatus) {
        if(registerStatus == null)return;
        Log.e("魅族","推送 ID："+registerStatus.getPushId());
    }

    @Override
    public void onUnRegisterStatus(Context context, UnRegisterStatus unRegisterStatus) {

    }

    @Override
    public void onPushStatus(Context context, PushSwitchStatus pushSwitchStatus) {

    }

    @Override
    public void onSubTagsStatus(Context context, SubTagsStatus subTagsStatus) {

    }

    @Override
    public void onSubAliasStatus(Context context, SubAliasStatus subAliasStatus) {

    }
}
