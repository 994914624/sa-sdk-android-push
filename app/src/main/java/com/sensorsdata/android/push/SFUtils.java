package com.sensorsdata.android.push;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class SFUtils {
    // 打开 App
    private static final String LAND_TYPE_OPEN_APP = "OPEN_APP";
    // 打开 Link
    private static final String LAND_TYPE_LINK = "LINK";
    // 自定义
    private static final String LAND_TYPE_CUSTOMIZED = "CUSTOMIZED";

    /**
     * 解析处理 SF 配置的操作，此处仅仅是模拟一些关键字段的解析的展示，具体的业务实现还要开发者根据
     * 自己的业务需求去实现。
     *
     * @param context Context
     * @param sfData 配置
     */
    public static void handleSFConfig(Context context, String sfData) {
        try {
            if (!TextUtils.isEmpty(sfData)) {
                JSONObject jsonObject = new JSONObject(sfData);
                // 解析推送过来的类型，有三种 OPEN_APP、LINK、CUSTOMIZED
                String type = jsonObject.optString("sf_landing_type");
                if (LAND_TYPE_OPEN_APP.equals(type)) {//打开 App
                    openApp(context);
                } else if (LAND_TYPE_LINK.equals(type)) {//打开链接
                    // 剥离出 link_data 字段
                    String url = jsonObject.optString("sf_link_url");
                    if (!TextUtils.isEmpty(url)) {
                        openLINK(context, url);
                    }
                } else if (LAND_TYPE_CUSTOMIZED.equals(type)) {//自定义的内容
                    // 剥离提取出自定义参数
                    JSONObject customized = (JSONObject) jsonObject.opt("customized");
                    //openCustomized(context, customized);
                }
            }
        } catch (Exception ex) {
            //ignore
        }
    }

    /**
     * 解析 SF 配置
     *
     * @param extra 配置
     * @return 配置值
     */
    public static String readSFConfig(String extra) {
        try {
            if (!TextUtils.isEmpty(extra)) {
                JSONObject json = new JSONObject(extra);
                if (json.has(SFConstant.SF_DATA)) {
                    return json.optString(SFConstant.SF_DATA);
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return "";
    }

    /**
     * 存储 PushId,设备推送 ID
     *
     * @param context Context
     * @param type 类别
     * @param registerId PushId
     */
    public static void profilePushId(Context context, String type, String registerId) {
        try {
            SFLogger.d("SFUtils", "profilePushId:" + type + "---pushId=" + registerId);
            SensorsDataAPI.sharedInstance(context).profilePushId(type, registerId);
        } catch (Exception ex) {
            //ignore
        }
    }

    /**
     * 处理通知打开操作，需要保证上传事件的字段是一致的。
     * 事件名：AppOpenNotification
     * msg_id	消息 ID	字符串
     * msg_title	消息标题	字符串
     * msg_content	消息内容	字符串
     * push_type	消息类型	字符串
     * sf_link_url	定向跳转页面	字符串
     * sf_plan_name	计划名称	字符串
     * sf_audience_entry_time	计划受众的进入时间	日期时间
     * sf_audience_id	计划受众 id	字符串
     * sf_plan_id	计划 id	数字
     *
     * @param context Context
     * @param sfData SF 配置
     * @param msg_id 三方平台的通知 ID，暂时无用
     * @param notificationTitle 通知标题
     * @param notificationContent 通知内容
     */
    public static void trackAppOpenNotification(Context context, String sfData, String msg_id, String notificationTitle, String notificationContent) {
        try {
            JSONObject jsonObject = null;
            if (!TextUtils.isEmpty(sfData)) {
                jsonObject = new JSONObject(sfData);
            }
            JSONObject properties = new JSONObject();
            // 获取消息标题，并保存在事件属性 msg_title 中
            properties.put("$sf_msg_title", notificationTitle);
            // 获取消息 ID，并保存在事件属性 msg_id 中
            properties.put("$sf_msg_content", notificationContent);
            if (jsonObject != null) {
                properties.put("$sf_msg_id", jsonObject.optString("sf_msg_id"));
                properties.put("$sf_plan_id", jsonObject.optString("sf_plan_id"));
                if(!"null".equals(jsonObject.optString("sf_audience_id"))){
                    properties.put("$sf_audience_id", jsonObject.optString("sf_audience_id"));
                }
                properties.put("$sf_link_url", jsonObject.optString("sf_link_url"));
                properties.put("$sf_plan_name", jsonObject.optString("sf_plan_name"));
                properties.put("$sf_plan_strategy_id", jsonObject.optString("sf_plan_strategy_id"));
            }
            // 使用神策分析追踪 "App 消息推送成功" 事件
            SensorsDataAPI.sharedInstance(context).track("AppOpenNotification", properties);
        } catch (Exception ex) {
            //ignore
        }
    }

    /**
     * 打开 App
     *
     * @param context Context
     */
    private static void openApp(Context context) {
        if (!isAppForeground(context)) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    /**
     * 打开链接
     *
     * @param context Context
     * @param url Url
     */
    private static void openLINK(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 自定义操作
     *
     * @param context Context
     * @param params 自定义的参数
     */
    private static void openCustomized(Context context, JSONObject params) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context.getPackageName(),
                "com.sensorsdata.android.push.SettingActivity"));
        if (params != null) {
            Iterator<String> it = params.keys();
            while (it.hasNext()) {
                String key = it.next();
                intent.putExtra(key, params.optString(key));
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 判断 App 是否在前台，仅供参考测试使用
     *
     * @param context Context
     * @return true, 在前台 false，在后台
     */
    private static boolean isAppForeground(Context context) {
        try {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList =
                    null;
            if (activityManager != null) {
                runningAppProcessInfoList = activityManager.getRunningAppProcesses();
            }
            if (runningAppProcessInfoList == null) {
                return false;
            }

            for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
                if (processInfo.processName.equals(context.getPackageName())
                        && (processInfo.importance ==
                        ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            //ignore
        }
        return false;
    }

    /**
     * 发送广播展示数据
     *
     * @param context Context
     * @param type 哪个推送类型
     * @param message 数据
     */
    public static void sendBroadcast(Context context, String type, String message) {
        SFLogger.d("SFUtils", "sendBroadcast:" + type + "---message=" + message);
        Intent intent = new Intent(context.getPackageName());
        intent.putExtra("type", type);
        intent.putExtra("message", message);
        context.sendStickyBroadcast(intent);
    }

    /**
     * 保存 PushId
     *
     * @param context Context
     * @param type 类型，Umeng、GeTui、Jpush
     * @param pushId PushId
     */
    public static void savePushId(Context context, String type, String pushId) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        sharedPreferences.edit().putString("PushId:" + type, pushId).apply();
        SFLogger.d("SFUtils", "savePushId:" + type + "---pushId=" + pushId);
    }

    /**
     * 获取 PushId
     *
     * @param context Context
     * @param type 类型，Umeng、GeTui、Jpush
     * @return pushId
     */
    public static String getPushId(Context context, String type) {
        String pushId = getSharedPreferences(context).getString("PushId:" + type, "");
        SFLogger.d("SFUtils", "getPushId:" + type + "---pushId=" + pushId);
        return pushId;
    }


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }
}
