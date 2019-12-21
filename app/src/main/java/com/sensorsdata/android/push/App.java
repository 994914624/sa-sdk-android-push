package com.sensorsdata.android.push;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.heytap.mcssdk.callback.PushCallback;
import com.heytap.mcssdk.mode.SubscribeResult;
import com.igexin.sdk.PushManager;
import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.android.push.getui.GeTuiService;
import com.sensorsdata.android.push.getui.RequiredPushService;
import com.sensorsdata.android.push.umeng.UmengHelper;
import com.sensorsdata.android.push.yzk.ToolBox;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.xiaomi.mipush.sdk.MiPushClient;


import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class App extends Application {
    /**
     * Sensors Analytics 采集数据的地址
     */
    private final static String SA_SERVER_URL = "https://sdktest.datasink.sensorsdata.cn/sa?project=yangzhankun&token=21f2e56df73988c7";

    @Override
    public void onCreate() {
        super.onCreate();
        SFLogger.setIsDebug(true);
        initUmengPush();
        initSensorsDataAPI();
        initJPush();
        initGetTuiPush();
        initXiaoMi();
        initXinGe();
        initOppo();
        initMeizu();
        ToolBox.getHuaWeiPushToken(this);
    }

    /**
     * 魅族有2种推送：①Flyme 推送、②集成推送
     * 魅族（需要魅族的 "推送服务"）
     * 此处是：Flyme 推送 http://push.meizu.com/
     *
     * 集成推送 http://mzups.meizu.com/
     */
    private void initMeizu() {
        com.meizu.cloud.pushsdk.PushManager.register(this, "126314", "373ebb77df37432babadc35f5d41ecb9");
    }

    /**
     * oppo
     */
    private void initOppo() {
        try {
            if (com.heytap.mcssdk.PushManager.isSupportPush(this.getApplicationContext())) {
                com.heytap.mcssdk.PushManager.getInstance().register(this.getApplicationContext(), "f0112b72c1cb4a88b563601d2668623e", "a93031e4428a4b23a1c106ed0b293ac5", new PushCallback() {
                    @Override
                    public void onRegister(int i, String s) {
                        Log.i("OPPO","推送 ID："+s);
                    }

                    @Override
                    public void onUnRegister(int i) {

                    }

                    @Override
                    public void onSetPushTime(int i, String s) {

                    }

                    @Override
                    public void onGetPushStatus(int i, int i1) {

                    }

                    @Override
                    public void onGetNotificationStatus(int i, int i1) {

                    }

                    @Override
                    public void onGetAliases(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onSetAliases(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onUnsetAliases(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onSetUserAccounts(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onUnsetUserAccounts(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onGetUserAccounts(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onSetTags(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onUnsetTags(int i, List<SubscribeResult> list) {

                    }

                    @Override
                    public void onGetTags(int i, List<SubscribeResult> list) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 信鸽
     */
    private void initXinGe() {
        XGPushConfig.enableDebug(this, true);
        // 多进程初始化，这里会回调多次，发现注释掉 registerPush ，receiver 中也不回调了。。。
        XGPushManager.registerPush(this, new XGIOperateCallback() {

            @Override
            public void onSuccess(Object token, int i) {
                // token在设备卸载重装的时候有可能会变
                Log.d("信鸽", "注册成功，设备token为：" + token);
            }

            @Override
            public void onFail(Object o, int i, String s) {
                Log.d("信鸽", "注册失败：" + i + "  ," + s);
            }
        });
    }

    /**
     * 初始化极光推送 SDK
     */
    private void initJPush() {
        JPushInterface.init(this);
    }

    /**
     * RequiredPushService 为第三方自定义推送服务,
     * 个推开发集成文档上建议应用开发者在 Activity 或 Service 类中调用个推 SDK 的初始化方法，
     * 确保SDK在各种情况下都能正常运行。一般情况下可以在主 Activity 的 onCreate()或者 onResume() 方法中调用，
     * 也可以在多个主要界面 Activity 的 onCreate() 或 onResume() 方法中调用。反复调用 SDK 初始化并不会有什么副作用。
     */
    private void initGetTuiPush() {
        // RequiredPushService 为第三方自定义推送服务
        PushManager.getInstance().initialize(getApplicationContext(), RequiredPushService.class);
        // GeTuiService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(getApplicationContext(), GeTuiService.class);
    }

    /**
     * 初始化 Sensors Analytics SDK
     */
    private void initSensorsDataAPI() {
        SAConfigOptions configOptions = new SAConfigOptions("https://sdktest.datasink.sensorsdata.cn/sa?project=yangzhankun&token=21f2e56df73988c7");
        // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
        configOptions.setAutoTrackEventType(SensorsAnalyticsAutoTrackEventType.APP_START |
                SensorsAnalyticsAutoTrackEventType.APP_END |
                SensorsAnalyticsAutoTrackEventType.APP_VIEW_SCREEN |
                SensorsAnalyticsAutoTrackEventType.APP_CLICK);
        // 打开 crash 信息采集
        configOptions.enableTrackAppCrash();
        // 测试使用，打印日志便于观察。
        configOptions.enableLog(true);
        //传入 SAConfigOptions 对象，初始化神策 SDK
        SensorsDataAPI.startWithConfigOptions(this, configOptions);
        // 点击图
        SensorsDataAPI.sharedInstance().enableHeatMap();
    }

    /**
     * 初始化友盟推送
     */
    private void initUmengPush() {
        /*
         * 初始化友盟推送
         * 在此处调用基础组件包提供的初始化函数 相应信息可在应用管理 -> 应用信息 中找到 http://message.umeng.com/list/apps
         * 参数一：当前上下文context；
         * 参数二：应用申请的Appkey（需替换）；
         * 参数三：渠道名称；
         * 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
         * 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
         */
        UMConfigure.init(this, "5d2700453fc1959fac0001a9", "Umeng",
                UMConfigure.DEVICE_TYPE_PHONE, "85bc49e4d9d95716c025261d412fa8f3");
        //UMConfigure.setLogEnabled(true);
        //获取消息推送代理示例
        PushAgent pushAgent = PushAgent.getInstance(this);
        // 注册通知处理自定义回调
        UmengHelper.registerNotificationCallback(pushAgent);
        // 注册 Message 消息自定义处理回调
        UmengHelper.registerMessageCallback(pushAgent);
        //注册推送服务，每次调用register方法都会回调该接口
        pushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(final String deviceToken) {
                /*
                 * 注册成功会返回deviceToken deviceToken是推送消息的唯一标志。
                 * deviceToken是【友盟+】消息推送生成的用于标识设备的id，长度为44位，不能定制和修改。
                 * 同一台设备上不同应用对应的deviceToken不一样。获取deviceToken的值后，可进行消息推送测试！
                 */
                SFLogger.d("Umeng", "注册成功：deviceToken：-------->  " + deviceToken);
                SFUtils.profilePushId(getApplicationContext(), SFConstant.PUSH_ID_UMENG, deviceToken);
                SFUtils.savePushId(getApplicationContext(), SFConstant.PUSH_ID_UMENG, deviceToken);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SFUtils.sendBroadcast(getApplicationContext(), SFConstant.PUSH_ID_UMENG, deviceToken);
                    }
                }).start();
            }

            @Override
            public void onFailure(String s, String s1) {
                SFLogger.d("Umeng", "注册失败：-------->  " + "s:" + s + ",s1:" + s1);
                SFUtils.sendBroadcast(getApplicationContext(), SFConstant.PUSH_ID_UMENG, "注册失败");
            }
        });
    }

    // user your appid the key.
    private static final String APP_ID = "2882303761518098384";
    // user your appid the key.
    private static final String APP_KEY = "5181809853384";

    private void initXiaoMi() {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit()) {
            SFLogger.d("Xiaomi", "registerPush");
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        } else {
            SFLogger.d("Xiaomi", "not registerPush");
        }
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = null;
        if (am != null) {
            processInfos = am.getRunningAppProcesses();
            String mainProcessName = getPackageName();
            int myPid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void reInitPush(Context ctx) {
        MiPushClient.registerPush(ctx.getApplicationContext(), APP_ID, APP_KEY);
    }
}
