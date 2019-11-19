package com.sensorsdata.android.push.umeng;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFLogger;
import com.sensorsdata.android.push.SFUtils;
import com.sensorsdata.android.push.yzk.ToolBox;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.json.JSONObject;

import java.util.Map;

public class UmengHelper {
    private static final String TAG = "UmengHelper";

    /**
     * 自定义通知栏打开动作,自定义行为的数据放在 UMessage.custom 字段。
     * 在【友盟+】后台或通过 API 发送消息时，在“后续动作”中的“自定义行为”中输入相应的值或代码即可实现。
     * 若开发者需要处理自定义行为，则可以重写方法 dealWithCustomAction()。其中自定义行为的内容，存放在 UMessage.custom 中。
     * 请在自定义 Application 类中添加以下代码.
     *
     * @param pushAgent PushAgent
     */
    public static void registerNotificationCallback(PushAgent pushAgent) {
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {

            @Override
            public void launchApp(Context context, UMessage msg) {
                super.launchApp(context, msg);
                SFLogger.d(TAG, "launchApp："+msg.custom +"||||"+ msg.extra);
                try {
                    String sf_data ="";
                    if(msg.extra !=null){
                        sf_data = new JSONObject(msg.extra).optString("sf_data");
                    }
                    ToolBox.handlePush(msg.title,msg.text,sf_data,context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleUmengMessage(context, msg);
            }
            @Override
            public void openUrl(Context context, UMessage msg) {
                super.openUrl(context, msg);
                SFLogger.d(TAG, "openUrl："+msg.custom +"||||"+  msg.extra);
                try {
                    String sf_data ="";
                    if(msg.extra !=null){
                        sf_data = new JSONObject(msg.extra).optString("sf_data");
                    }
                    ToolBox.handlePush(msg.title,msg.text,sf_data,context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleUmengMessage(context, msg);
            }
            @Override
            public void openActivity(Context context, UMessage msg) {
                super.openActivity(context, msg);
                SFLogger.d(TAG, "openActivity："+msg.custom +"||||"+  msg.extra);
                try {
                    String sf_data ="";
                    if(msg.extra !=null){
                        sf_data = new JSONObject(msg.extra).optString("sf_data");
                    }
                    ToolBox.handlePush(msg.title,msg.text,sf_data,context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handleUmengMessage(context, msg);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                super.dealWithCustomAction(context, msg);
                SFLogger.d(TAG, "dealWithCustomAction："+msg.custom +"||||"+  msg.extra);
                if(context ==null || msg ==null)return;
//                try{
//                    //handleUmengMessage(context, msg);
//                    String pushContent = String.format("消息标题：%s \n\n消息内容：%s \n\nmsg.extra：%s \n\nmsg.custom：%s", msg.title, msg.text, msg.extra.toString(), msg.custom);
//                    SFUtils.sendBroadcast(context.getApplicationContext(), SFConstant.PUSH_CONTENT, pushContent);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                //TODO 埋点 App 打开推送消息 事件
//                ToolBox.trackAppOpenNotification(msg.extra, msg.title, msg.text);

                try {
                    String sf_data ="";
                    if(msg.extra !=null){
                         sf_data = new JSONObject(msg.extra).optString("sf_data");
                    }
                    ToolBox.handlePush(msg.title,msg.text,sf_data,context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        pushAgent.setNotificationClickHandler(notificationClickHandler);
    }

    /**
     * 自定义消息的内容存放在 UMessage.custom 字段里。拦截自定义参数根据友盟官网介绍在 getNotification 中拦截。
     * 参照友盟官方文档：https://developer.umeng.com/docs/66632/detail/98583#h2--10
     *
     * @param pushAgent PushAgent
     */
    public static void registerMessageCallback(PushAgent pushAgent) {
        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        SFLogger.d(TAG, "UMessage = " + msg.custom);
                        handleUmengMessage(context, msg);
                    }
                });
            }
        };

        pushAgent.setMessageHandler(messageHandler);
    }

    /**
     * 该方法是解析处理友盟推送的相关参数，此处仅仅演示了 sf_data 推送的相关字段，注意，如果你有原有的逻辑也有相关处理
     * 的逻辑，你需要做一定的兼容处理。
     * @param context Context
     * @param uMessage UMessage
     */
    private static void handleUmengMessage(Context context, UMessage uMessage) {
        try {
            if (uMessage == null) return;
            if (!TextUtils.isEmpty(uMessage.custom)) {
                SFUtils.sendBroadcast(context,  SFConstant.PUSH_TYPE_UMENG,  "自定义参数---" + uMessage.custom);
            }
            if (uMessage.extra != null) {
                for (Map.Entry entry : uMessage.extra.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    /*
                     * 解析出 sf_data 字段，然后做自定义处理
                     */
                    if (SFConstant.SF_DATA.equals(key)) {
                        // 为了测试收到的内容效果，发送广播给页面展示使用，仅测试使用。
                        SFUtils.sendBroadcast(context, SFConstant.PUSH_TYPE_UMENG, key + ":" + value);
                        // 解析 sf 中的字段，然后做一些自定义操作，本例中是模拟，开发者需要根据自己的实际需求实现。
                        SFUtils.handleSFConfig(context, value.toString());
                        /*
                         * 当点击通知打开 App 时，需要上报一个 "AppOpenNotification" 事件，此处假定当前是从 launchApp 调用过来的
                         */
                        SFUtils.trackAppOpenNotification(context, value.toString(), uMessage.msg_id, uMessage.title, uMessage.text);
                    }
                }
            }
        } catch (Exception ex) {
            SALog.printStackTrace(ex);
        }
    }
}
