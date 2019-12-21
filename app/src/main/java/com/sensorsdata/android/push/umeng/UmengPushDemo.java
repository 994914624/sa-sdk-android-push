package com.sensorsdata.android.push.umeng;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.UmengNotifyClickActivity;
import com.umeng.message.entity.UMessage;

import org.android.agoo.common.AgooConstants;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by yzk on 2019-11-25
 *
 * 友盟推送，神策 SF 推送消息埋点示例及说明。标注 "TO DO" 的为开发者要做的或者要注意的。
 *
 * 具体说明（开发者要做的）：
 * 一、埋点 "App 打开推送消息"
 *   1.1 需要在 launchApp、openUrl、openActivity、dealWithCustomAction 这 4 个接口中，调用 trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
 *   1.2 如果使用了厂商通道需要在处理厂商通道的 Activity 的 onMessage 接口中处理消息，调用 trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
 *
 * 二、上报 "推送 ID"
 *   2.1 在 IUmengRegisterCallback 的 onSuccess 接口中，调用 SensorsDataAPI.sharedInstance().profilePushId("umeng_id", token);
 *   2.2 在调用神策 SDK login 接口之后。调用 SensorsDataAPI.sharedInstance().profilePushId("umeng_id",PushAgent.getInstance(this).getRegistrationId());
 *
 * 三、处理神策 SF 推送的 "打开 URL 消息"、"自定义消息"，做页面跳转
 *   3.1 需要在友盟 UmengNotificationClickHandler 的 launchApp 接口中，调用 handleSFCustomMessage(uMessage.extra); 同时在 handleSFCustomMessage 方法内加上自己的业务跳转逻辑。
 *   3.2 如果使用了厂商通道需要在处理厂商通道的 Activity 的 onMessage 接口中处理消息，调用 handleSFCustomMessage(uMessage.extra); 同时在 handleSFCustomMessage 方法内加上自己的业务跳转逻辑。
 *
 * 具体接入过程中，如果有什么疑问，请及时在微信群里提出来！！！
 */

public class UmengPushDemo {

    /**
     * 初始化 umeng push sdk
     */
    public static void init(Context context) {
        if (context == null) return;
        UMConfigure.init(context, "XXX", "XXX", UMConfigure.DEVICE_TYPE_PHONE, "XXX");
        //HuaWeiRegister.register(context);// 友盟-华为通道
        //MiPushRegistar.register( this,"xx","xx");// 友盟-小米通道
        //……

        // 推送 ID 的处理
        PushAgent.getInstance(context).register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String token) {
                // TODO 上报友盟 "推送 ID"
                SensorsDataAPI.sharedInstance().profilePushId("umeng_id", token);
                // TODO 注意在调用神策 SDK login 接口后，也需要调用 profilePushId 接口上报友盟 "推送 ID"
                //SensorsDataAPI.sharedInstance().profilePushId("umeng_id",PushAgent.getInstance(this).getRegistrationId());
            }

            @Override
            public void onFailure(String s, String s1) {}
        });

        // 点击的处理
        PushAgent.getInstance(context).setNotificationClickHandler(new UmengNotificationClickHandler() {

            @Override
            public void launchApp(Context context, UMessage uMessage) {
                super.launchApp(context, uMessage);
                // TODO 埋点 App 打开推送消息 事件
                trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
                // TODO 处理自定义消息（神策 SF 发的推送消息必须在 launchApp 接口中处理）
                handleSFPushMessage(uMessage.extra);
            }

            @Override
            public void openUrl(Context context, UMessage uMessage) {
                super.openUrl(context, uMessage);
                // TODO 埋点 App 打开推送消息 事件
                trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage uMessage) {
                super.dealWithCustomAction(context, uMessage);
                // TODO 埋点 App 打开推送消息 事件
                trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
            }

            @Override
            public void openActivity(Context context, UMessage uMessage) {
                super.openActivity(context, uMessage);
                // TODO 埋点 App 打开推送消息 事件
                trackAppOpenNotification(uMessage.extra, uMessage.title, uMessage.text);
            }
        });
    }


    // ---------------------------------------- 下边是处理厂商通道消息示例代码 ----------------------------------------

    /**
     * 友盟厂商通道消息的 Activity 。
     * <p>
     * 该 Activity 需继承自 UmengNotifyClickActivity，同时实现父类的 onMessage 方法，
     * 对该方法的 intent 参数进一步解析即可，该方法异步调用，不阻塞主线程。
     * 并设置 launchMode="singleTask" 和 exported="true"
     */
    public class HandlePushActivity extends UmengNotifyClickActivity {

        /**
         * 处理友盟厂商通道消息
         */
        @Override
        public void onMessage(Intent intent) {
            super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
            if (intent != null) {
                // TODO 如果你们使用了友盟的厂商通道消息，需要在 onMessage 处理厂商通道消息！！！
                String messageBody = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
                Log.i("TODO", "厂商通道消息：" + messageBody);
                if (!TextUtils.isEmpty(messageBody)) {
                    try {
                        JSONObject push = new JSONObject(messageBody);
                        JSONObject extra = push.optJSONObject("extra");
                        String extraStr = null;
                        if (extra != null) {
                            extraStr = extra.toString();
                            // TODO 处理自定义消息
                            handleSFPushMessage(extraStr);
                        }
                        // 推送标题
                        String title = push.optJSONObject("body").optString("title");
                        // 推送内容
                        String content = push.optJSONObject("body").optString("text");
                        // TODO 埋点 App 打开推送消息 事件
                        trackAppOpenNotification(extraStr, title, content);
                        Log.i("TODO", String.format("title： %s。content：%s。sf_data： %s。", title, content, extraStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    // ---------------------------------------- 下边是神策 SF 推送埋点示例代码 ----------------------------------------

    /**
     * 埋点 App 打开推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param notificationExtras  推送消息的 extras（参数类型只能传 String 或 Map<String,String>）
     * @param notificationTitle   推送消息的标题
     * @param notificationContent 推送消息的内容
     */
    public static void trackAppOpenNotification(Object notificationExtras, String notificationTitle, String notificationContent) {
        try {
            JSONObject properties = new JSONObject();
            // 推送消息的标题
            properties.put("$sf_msg_title", notificationTitle);
            // 推送消息的内容
            properties.put("$sf_msg_content", notificationContent);
            try {
                String sfData = null;
                if (notificationExtras != null) {
                    if (notificationExtras instanceof String) {
                        sfData = new JSONObject((String) notificationExtras).optString("sf_data");
                    } else if (notificationExtras instanceof Map) {
                        sfData = new JSONObject((Map) notificationExtras).optString("sf_data");
                    }
                }
                if (!TextUtils.isEmpty(sfData)) {
                    JSONObject sfJson = new JSONObject(sfData);
                    // 推送消息中 SF 的内容
                    properties.put("$sf_msg_id", sfJson.optString("sf_msg_id", null));
                    properties.put("$sf_plan_id", sfJson.optString("sf_plan_id", null));
                    if (!"null".equals(sfJson.optString("sf_audience_id", null))) {
                        properties.put("$sf_audience_id", sfJson.optString("sf_audience_id", null));
                    }
                    properties.put("$sf_link_url", sfJson.optString("sf_link_url", null));
                    properties.put("$sf_plan_name", sfJson.optString("sf_plan_name", null));
                    properties.put("$sf_plan_strategy_id", sfJson.optString("sf_plan_strategy_id", null));
                    JSONObject customized = sfJson.optJSONObject("customized");
                    if (customized != null) {
                        Iterator<String> iterator = customized.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            properties.put(key, customized.opt(key));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 埋点 "App 打开推送消息" 事件
            SensorsDataAPI.sharedInstance().track("AppOpenNotification", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理神策 SF 自定义消息，做页面跳转。
     * TODO 此方法只是帮忙解析出神策 SF 推送消息中的 URL/自定义字段，具体的业务跳转逻辑需要开发者加上！！！
     *
     * @param notificationExtras 推送消息的 extra
     */
    public static void handleSFPushMessage(Object notificationExtras) {
        try {
            String sfData = null;
            if (notificationExtras != null) {
                if (notificationExtras instanceof String) {
                    sfData = new JSONObject((String) notificationExtras).optString("sf_data");
                } else if (notificationExtras instanceof Map) {
                    sfData = new JSONObject((Map) notificationExtras).optString("sf_data");
                }
            }
            if (!TextUtils.isEmpty(sfData)) {
                JSONObject sfJson = new JSONObject(sfData);
                if ("CUSTOMIZED".equals(sfJson.optString("sf_landing_type"))) {
                    JSONObject custom = sfJson.optJSONObject("customized");
                    if (custom != null) {
                        // TODO 处理自定义消息中的字段，做自页面跳转等
                        Log.i("TODO", String.format("-- 请处理自定义字段，做自页面跳转等 --: %s", custom.toString()));
                    }
                } else if ("LINK".equals(sfJson.optString("sf_landing_type"))) {
                    String url = sfJson.optString("sf_link_url");
                    if(!TextUtils.isEmpty(url)){
                        // TODO 处理打开 URL 的消息
                        Log.i("TODO","-- 请处理 URL --: " + url);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
