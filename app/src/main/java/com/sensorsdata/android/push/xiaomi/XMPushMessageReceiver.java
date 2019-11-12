package com.sensorsdata.android.push.xiaomi;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sensorsdata.android.push.R;
import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFLogger;
import com.sensorsdata.android.push.SFUtils;
import com.sensorsdata.android.push.yzk.ToolBox;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class XMPushMessageReceiver extends PushMessageReceiver {
    private static final String TAG = "XMPushMessageReceiver";
    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    /*
     * onReceivePassThroughMessage 方法用来接收服务器向客户端发送的透传消息
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        if (context == null || message == null) return;
        SFLogger.d(TAG, "onReceivePassThroughMessage is called. " + message.toString());
        String log = context.getString(R.string.recv_passthrough_message, message.getContent());
        SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_CONTENT, log);
    }

    /**
     * onNotificationMessageClicked 方法用来接收服务器向客户端发送的通知消息，这个回调方法会在用户手动点击通知后触发。
     */
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        if (context == null || message == null) return;
        SFLogger.d(TAG, "onNotificationMessageClicked is called. " + message.toString());
        String log = context.getString(R.string.click_notification_message, message.getContent());
        SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_CONTENT, log);
        // 埋点 "App 打开推送消息" 事件
        ToolBox.trackAppOpenNotification(message.getExtra(), message.getTitle(), message.getDescription());
    }

    /**
     * onNotificationMessageArrived 方法用来接收服务器向客户端发送的通知消息，
     * 这个回调方法是在通知消息到达客户端时触发。另外应用在前台时不弹出通知的通知消息到达客户端也会触发这个回调函数。
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        SFLogger.d(TAG, "onNotificationMessageArrived is called. " + message.toString());
        String log = context.getString(R.string.arrive_notification_message, message.getContent());
        SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_TYPE_XMPUSH, log);
    }

    /**
     * onCommandResult 方法用来接收客户端向服务器发送命令后的响应结果
     */
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        SFLogger.d(TAG, "onCommandResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.set_alias_success, mAlias);
            } else {
                log = context.getString(R.string.set_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.unset_alias_success, mAlias);
            } else {
                log = context.getString(R.string.unset_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.set_account_success, mAccount);
            } else {
                log = context.getString(R.string.set_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.unset_account_success, mAccount);
            } else {
                log = context.getString(R.string.unset_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.subscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.unsubscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
                log = context.getString(R.string.set_accept_time_success, mStartTime, mEndTime);
            } else {
                log = context.getString(R.string.set_accept_time_fail, message.getReason());
            }
        } else {
            log = message.getReason();
        }
        SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_TYPE_XMPUSH, log);
    }

    /**
     * onReceiveRegisterResult 方法用来接收客户端向服务器发送注册命令后的响应结果
     */
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        SFLogger.d(TAG, "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = "android_" + cmdArg1;
                log = context.getString(R.string.register_success) + ":xiaomiID=" + mRegId;
                SFUtils.profilePushId(context, SFConstant.PUSH_ID_XMPUSH, mRegId);
                /**
                 * 以下为测试代码使用，用于展示
                 */
                SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_ID_XMPUSH, mRegId);
                SFUtils.savePushId(context, SFConstant.PUSH_ID_XMPUSH, mRegId);
                return;
            }
//            else {
//                log = context.getString(R.string.register_fail);
//                SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_ID_XMPUSH, log);
//                return;
//            }
        } else {
            log = message.getReason();
        }

       // SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_TYPE_XMPUSH, log);
    }

    @Override
    public void onRequirePermissions(Context context, String[] permissions) {
        super.onRequirePermissions(context, permissions);
        SFLogger.d(TAG, "onRequirePermissions is called. need permission" + arrayToString(permissions));
        try {
            if (Build.VERSION.SDK_INT >= 23 && context.getApplicationInfo().targetSdkVersion >= 23) {
                Intent intent = new Intent();
                intent.putExtra("permissions", permissions);
                intent.setComponent(new ComponentName(context.getPackageName(), PermissionActivity.class.getCanonicalName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                context.startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressLint("SimpleDateFormat")
    private static String getSimpleDate() {
        return new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date());
    }

    public String arrayToString(String[] strings) {
        String result = " ";
        for (String str : strings) {
            result = result + str + " ";
        }
        return result;
    }
}
