package com.sensorsdata.android.push.getui;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFLogger;
import com.sensorsdata.android.push.SFUtils;
import com.sensorsdata.android.push.yzk.ToolBox;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class GeTuiService extends GTIntentService {
    private static final String TAG = "GeTuiService";
    private boolean isNotificationClick = false;
    private String title;
    private String content;
    @Override
    public void onReceiveServicePid(Context context, int i) {
        Log.i(TAG, "onReceiveServicePid:" + i);
    }

    @Override
    public void onReceiveClientId(Context context, String clientId) {
        Log.i(TAG, "onReceiveClientId:" + clientId);
        /*
         * 接收 ClientId，ClientId 是个推业务层中的对外用户标识，用于标识客户端身份，由第三方客户端获取并保存到第三方服务端，是个推 SDK 的唯一识别号,简称 CID。
         * 在这里需要调用神策的 profilePushId 存储该 ID。
         */
        SFUtils.profilePushId(context, SFConstant.PUSH_ID_GETUI, clientId);
        /*
         * 将收到的信息发送到页面显示，测试使用
         */
        SFUtils.sendBroadcast(context, SFConstant.PUSH_ID_GETUI, clientId);
        SFUtils.savePushId(context, SFConstant.PUSH_ID_GETUI, clientId);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        if(context ==null || gtTransmitMessage ==null)return;
        Log.i(TAG, "onNotificationMessageArrived");

        // 构造一个本地通知
        ToolBox.sendNotification(context);
        /*
         * 透传消息的处理，此处仅仅演示了 sf_data 推送的相关字段，注意，如果你有原有的逻辑也有相关处理
         * 的逻辑，你需要做一定的兼容处理。
         */
        String appid = gtTransmitMessage.getAppid();
        String taskid = gtTransmitMessage.getTaskId();
        String messageid = gtTransmitMessage.getMessageId();
        byte[] payload = gtTransmitMessage.getPayload();
        String pkg = gtTransmitMessage.getPkgName();
        String cid = gtTransmitMessage.getClientId();
        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        SFLogger.d(TAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));
        SFLogger.d(TAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid +
                "\nmessageid = " + messageid + "\npayload = " + new String(payload) +
                "\npkg = " + pkg + "\ncid = " + cid);

        /*
         * 透传的消息在 payload 中，目前调试来看，onNotificationMessageClicked 触发后才走的当前回调
         */
        String sfData = new String(payload);
        if (isNotificationClick) {
            isNotificationClick = false;
            SFUtils.trackAppOpenNotification(context, sfData, messageid, title, content);
        }
        SFUtils.handleSFConfig(context, sfData);
        /*
         * 将收到的信息发送到页面显示，测试使用
         */
        SFUtils.sendBroadcast(context, SFConstant.PUSH_CONTENT, sfData);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        Log.i(TAG, "onReceiveOnlineState:" + b);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.i(TAG, "onReceiveCommandResult:"
                + "\nappId = " +gtCmdMessage.getAppid()
                + "\nClientId = " + gtCmdMessage.getClientId()
                + "\npackageName = " + gtCmdMessage.getPkgName()
                + "\naction = " + gtCmdMessage.getAction());
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.i(TAG, "onNotificationMessageArrived:"
                + "\nmessageId = " + gtNotificationMessage.getMessageId()
                + "\ntaskId = " + gtNotificationMessage.getTaskId()
                + "\ncontent = " + gtNotificationMessage.getContent()
                + "\ntitle = " + gtNotificationMessage.getTitle());
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        Log.i(TAG, "onNotificationMessageClicked:"
                + "\nmessageId = " + gtNotificationMessage.getMessageId()
                + "\ntaskId = " + gtNotificationMessage.getTaskId()
                + "\ncontent = " + gtNotificationMessage.getContent()
                + "\ntitle = " + gtNotificationMessage.getTitle());
        title = gtNotificationMessage.getTitle();
        content = gtNotificationMessage.getContent();
        isNotificationClick = true;
    }
}
