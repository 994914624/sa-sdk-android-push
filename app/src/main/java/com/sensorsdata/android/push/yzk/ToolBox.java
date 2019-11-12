package com.sensorsdata.android.push.yzk;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.huawei.hms.aaid.HmsInstanceId;
import com.igexin.sdk.PushManager;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.android.push.MainActivity;
import com.sensorsdata.android.push.R;
import com.umeng.message.PushAgent;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by yzk on 2019-10-30
 */

public class ToolBox {

    public static String HUAWEI_PUSH_ID = "";

    /**
     * 权限
     */
    public static void requestPermission(Activity context) {
        // 申请 READ_PHONE_STATE 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                int code = (int) (Math.random()*1000+11);
                ActivityCompat.requestPermissions(context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_PHONE_STATE"}, code);
            }
            if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED) {
                int code = (int) (Math.random()*1000+111);
                ActivityCompat.requestPermissions(context, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_PHONE_STATE"}, code);
            }
        }
    }

    /**
     * 扫码
     */
    public static void scanBar(Activity activity) {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setPrompt("扫描二维码");
        integrator.initiateScan();
    }

    /**
     * 扫码结果
     */
    public static void handleScanResult(int requestCode, int resultCode, Intent data, Activity activity, TextView runText) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(activity, "扫码取消", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "扫码成功： " + result.getContents(), Toast.LENGTH_LONG).show();
                    SensorsDataAPI.sharedInstance().track("ScanBarOk");
                    // TODO 开启 debug 模式、点击图、可视化埋点
                    if (!openDebugModeOrHeatMap(result.getContents(), activity)) {
                        // TODO 更新数据接收地址 & 复制到剪切板 & 上报推送 ID
                        updateServerUrl(result.getContents());
                        runText.setText(String.format("当前数据接收地址：%s", getServerUrl()));
                        copy(result.getContents(), activity);
                        profilePushId(activity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启 debug 模式、点击图、可视化埋点
     * <p>
     * 扫码拿到的链接
     * https://sdktest.cloud.sensorsdata.cn/h5/debugmode/index.html?connectType=debugmode&info_id=z5s7953lgi8&protocal=sa9930c860&project=yangzhankun
     * <p>
     * // debug 模式 intent 携带的参数
     * sa9930c860://debugmode?info_id=z5s7953lgi8
     * // 可视化全埋点 intent 携带的参数
     * sa9930c860://visualized?feature_code=sa9930c860mRArSnvbFf&url=https%3A%2F%2Fsdktest.cloud.sensorsdata.cn%2Fapi%2Fheat_map%2Fupload%3Fproject%3Dyangzhankun
     * // 点击图 intent 携带的参数
     * sa9930c860://heatmap?feature_code=sa9930c860mRArSnvbFf&url=https%3A%2F%2Fsdktest.cloud.sensorsdata.cn%2Fapi%2Fheat_map%2Fupload%3Fproject%3Dyangzhankun
     */
    public static boolean openDebugModeOrHeatMap(String result, Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final Uri uri = Uri.parse(result);
            Set<String> keys = uri.getQueryParameterNames();
            // debug 模式
            if (keys.contains("connectType") && "debugmode".equals(uri.getQueryParameter("connectType"))) {
                String intentData = String.format("%s://debugmode?info_id=%s", uri.getQueryParameter("protocal"), uri.getQueryParameter("info_id"));
                intent.setData(Uri.parse(intentData));
                // 打开指定的 App
                activity.startActivity(intent);
                activity.finish();
                return true;
            }
            // 可视化埋点、点击图
            if (keys.contains("feature_code") && keys.contains("protocal")) {
                //拼接 heat map api & scanning api
                String heatMapApi = String.format("%s://%s/api/heat_map/upload?project=%s", uri.getScheme(), uri.getHost(), uri.getQueryParameter("project"));
                final String scanningApi = String.format("%s://%s/api/heat_map/scanning?project=%s", uri.getScheme(), uri.getHost(), uri.getQueryParameter("project"));
                // heatmap or visualized
                String host = uri.getQueryParameter("connectType") == null ? "heatmap" : "visualized";
                String intentData = String.format("%s://%s?feature_code=%s&url=%s", uri.getQueryParameter("protocal"), host, uri.getQueryParameter("feature_code"), URLEncoder.encode(heatMapApi, "UTF-8"));
                intent.setData(Uri.parse(intentData));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 发出 scanning 请求
                        submitPostData(scanningApi, uri.getQueryParameter("feature_code"));
                    }
                }).start();
                // 打开指定的 App
                activity.startActivity(intent);
                activity.finish();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制内容到剪切板
     */
    public static boolean copy(String copyStr, Context context) {
        try {
            // 获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 更换数据接收地址
     */
    public static void updateServerUrl(String serverUrl){
        if(!TextUtils.isEmpty(serverUrl)){
            SensorsDataAPI.sharedInstance().setServerUrl(serverUrl);
        }
    }

    /**
     * 上报 推送 ID
     */
    public static void profilePushId(Context context) {
        try {
            JSONObject properties = new JSONObject();
            String jgId = JPushInterface.getRegistrationID(context);
            String xmId = MiPushClient.getRegId(context);
            String gtId = PushManager.getInstance().getClientid(context);
            String umId = PushAgent.getInstance(context).getRegistrationId();
            if (!TextUtils.isEmpty(jgId)) {
                // 极光 "推送 ID"
                properties.put("jiguang_id", jgId);
            }
            if (!TextUtils.isEmpty(xmId)) {
                // 小米 "推送 ID"
                properties.put("xiaomi_id", String.format("android_%s", xmId));
            }
            if (!TextUtils.isEmpty(gtId)) {
                // 个推 "推送 ID"
                properties.put("getui_id", gtId);
            }
            if (!TextUtils.isEmpty(umId)) {
                // 友盟 "推送 ID"
                properties.put("umeng_id", umId);
            }
            if (!TextUtils.isEmpty(HUAWEI_PUSH_ID)) {
                // 华为 "推送 ID"
                properties.put("huawei_id", HUAWEI_PUSH_ID);
            }
            SensorsDataAPI.sharedInstance().profileSet(properties);
            SensorsDataAPI.sharedInstance().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 埋点 App 打开推送消息
     * <p>
     * 事件名：AppOpenNotification
     *
     * @param notificationExtras  推送消息的 extras（参数类型只能传 String 或 Map<String,String> ）
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
                if (notificationExtras instanceof String) {
                    sfData = new JSONObject((String) notificationExtras).optString("sf_data");
                } else if (notificationExtras instanceof Map){
                    sfData = new JSONObject((Map) notificationExtras).optString("sf_data");
                }
                if (!TextUtils.isEmpty(sfData)) {
                    JSONObject sfJson = new JSONObject(sfData);
                    // 推送消息中 SF 的内容
                    properties.put("$sf_msg_id", sfJson.optString("sf_msg_id", null));
                    properties.put("$sf_plan_id", sfJson.optString("sf_plan_id", null));
                    if(!"null".equals(sfJson.optString("sf_audience_id", null))){
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

    /*
     * 反射调用 getServerUrl
     */
    public static String getServerUrl() {
        try {
            Class<?> clazz = Class.forName("com.sensorsdata.analytics.android.sdk.SensorsDataAPI");
            java.lang.reflect.Method sharedInstance = clazz.getMethod("sharedInstance");
            java.lang.reflect.Method getServerUrl = clazz.getDeclaredMethod("getServerUrl");
            getServerUrl.setAccessible(true);
            Object sdkInstance = sharedInstance.invoke(null);
            return (String)getServerUrl.invoke(sdkInstance);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 个推发送的是透传的消息
     *  展示一个本地通知
     */
    public static void sendNotification(Context context) {
        String channelID = "100";
        // 获取 NotificationManager 实例
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyManager == null) return;
        // 获取 PendingIntent
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // 实例化NotificationCompat.Builde并设置相关属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,channelID)
                // 设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知标题
                .setContentTitle("来了一条个推透传消息")
                // 点击通知后自动清除
                .setAutoCancel(true)
                // 设置通知内容
                .setContentText("xxx 内容")
                .setContentIntent(mainPendingIntent);

        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel(channelID, "channel_name", android.app.NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("我是一个通知通道");
            notifyManager.createNotificationChannel(channel);
        }
        // 通过 builder.build() 方法生成 Notification 对象,并发送通知,id=1
        notifyManager.notify(1, builder.build());
    }

    /**
     * 华为 push
     */
    public static void getHuaWeiPushToken(final Context context) {
        if(context ==null)return;
        final HmsInstanceId hmsInstanceId  = HmsInstanceId.getInstance(context);
        new Thread() {
            @Override
            public void run() {
                try {
                    String token =  hmsInstanceId.getToken("com.sensorsdata.android.push", "HCM");
                    if (!TextUtils.isEmpty(token)) {
                        // getToken
                        HUAWEI_PUSH_ID = token;
                        Log.i("华为推送 token: ",token);
                        SensorsDataAPI.sharedInstance().profileSet(new JSONObject().put("huawei_id",token));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * post 请求
     *
     */
    public static String submitPostData(String strUrlPath, String params) {
        try {
            byte[] data = params.getBytes("UTF-8");
            URL url = new URL(strUrlPath);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            // 设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            // 设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpURLConnection.getInputStream();
                // 处理服务器的响应结果
                String resultData = null;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] data2 = new byte[1024];
                int len;
                try {
                    while ((len = inputStream.read(data2)) != -1) {
                        byteArrayOutputStream.write(data2, 0, len);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                resultData = new String(byteArrayOutputStream.toByteArray());
                return resultData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-1";
    }
}
