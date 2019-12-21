package com.sensorsdata.android.push;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.igexin.sdk.PushManager;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.android.push.yzk.ToolBox;
import com.tencent.android.tpush.XGPushConfig;
import com.umeng.message.PushAgent;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

/**
 * 在 onCreate 中会 2 次尝试上报推送 ID
 * 在小米初始化成功时，也会尝试上报推送 ID
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_push_content;
    private TextView textView_jpush;
    private TextView textView_getui;
    private TextView textView_umeng;
    private TextView textView_xiaomi;
    private TextView textView_huawei;
    private TextView textView_xinge;
    private TextView textView_oppo;
    private TextView textView_meizu;
    private TextView runText;
    private EditText editText;

    private BroadcastReceiver sfResultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String text = tv_push_content.getText().toString();
            String type = intent.getStringExtra("type");
            String message = intent.getStringExtra("message");
            Log.i("XXX onReceive", "" + type + ": " + message);
            if (type != null) {
                switch (type) {
                    case SFConstant.PUSH_CONTENT:
                        // 推送内容的展示
                        //tv_push_content.setText(message);
                        break;
                    case SFConstant.PUSH_ID_XMPUSH:
                        // 小米推送初始化最慢！！！，第一次没权限会初始化失败。。。
                        refreshDisplay(MainActivity.this);
                        ToolBox.profilePushId(MainActivity.this);
                        break;
                    default:
                        break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToolBox.requestPermission(this);
        PushAgent.getInstance(this).onAppStart();
        initView();
        createNotificationChannel();
        // 注册广播，接收推送展示的数据
        registerReceiver(sfResultReceiver, new IntentFilter(getPackageName()));
        // 上报 推送 ID
        refreshDisplay(this);
        ToolBox.profilePushId(this);
        // 5秒后，再次上报 推送 ID
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshDisplay(MainActivity.this);
                ToolBox.profilePushId(MainActivity.this);
                Toast.makeText(MainActivity.this, "上报推送 ID", Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        oppoRequestNotificationPermission();
    }

    private void oppoRequestNotificationPermission() {
        try {
            if (com.heytap.mcssdk.PushManager.isSupportPush(this.getApplicationContext())) {
                // 请求权限
                com.heytap.mcssdk.PushManager.getInstance().requestNotificationPermission();
//                com.heytap.mcssdk.PushManager.getInstance().openNotificationSettings();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        tv_push_content = findViewById(R.id.tv_test);
        textView_jpush = findViewById(R.id.tv_jpush);
        textView_getui = findViewById(R.id.tv_getui);
        textView_umeng = findViewById(R.id.tv_umeng);
        textView_xiaomi = findViewById(R.id.tv_xiaomi);
        textView_huawei = findViewById(R.id.tv_huawei);
        textView_xinge = findViewById(R.id.tv_xinge);
        textView_oppo = findViewById(R.id.tv_oppo);
        textView_meizu = findViewById(R.id.tv_meizu);
        findViewById(R.id.ll_main_jg).setOnClickListener(this);
        findViewById(R.id.ll_main_gt).setOnClickListener(this);
        findViewById(R.id.ll_main_um).setOnClickListener(this);
        findViewById(R.id.ll_main_xm).setOnClickListener(this);
        findViewById(R.id.ll_main_hw).setOnClickListener(this);
        findViewById(R.id.ll_main_xg).setOnClickListener(this);
        findViewById(R.id.ll_main_oppo).setOnClickListener(this);
        findViewById(R.id.ll_main_meizu).setOnClickListener(this);
        TextView tv_jpush_tip = findViewById(R.id.tv_jpush_tip);
        TextView tv_getui_tip = findViewById(R.id.tv_getui_tip);
        TextView tv_umeng_tip = findViewById(R.id.tv_umeng_tip);
        TextView tv_xiaomi_tip = findViewById(R.id.tv_xiaomi_tip);
        TextView tv_huawei_tip = findViewById(R.id.tv_huawei_tip);
        TextView tv_xinge_tip = findViewById(R.id.tv_xinge_tip);
        TextView tv_oppo_tip = findViewById(R.id.tv_oppo_tip);
        TextView tv_meizu_tip = findViewById(R.id.tv_meizu_tip);
        tv_jpush_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>极光</b>" + "</font>" + " 推送 ID"));
        tv_getui_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>个推</b>" + "</font>" + " 推送 ID"));
        tv_umeng_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>友盟</b>" + "</font>" + " 推送 ID"));
        tv_xiaomi_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>小米</b>" + "</font>" + " 推送 ID"));
        tv_huawei_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>华为</b>" + "</font>" + " 推送 ID"));
        tv_xinge_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>信鸽</b>" + "</font>" + " 推送 ID"));
        tv_oppo_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>OPPO</b>" + "</font>" + " 推送 ID"));
        tv_meizu_tip.setText(Html.fromHtml("点击可复制 " + "<font color=\"#FF4081\">" + "<b>魅族</b>" + "</font>" + " 推送 ID"));
        // run text
        runText = findViewById(R.id.tv_main_run_text);
        runText.setText(String.format("当前数据接收地址：%s", ToolBox.getServerUrl()));
        initInputActionBar();
    }


    /*
     * 带输入框，及扫码的 ActionBar
     */
    private void initInputActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            View view = LayoutInflater.from(this).inflate(R.layout.action_bar_input, null);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            actionBar.setCustomView(view, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            //输入框
            editText = view.findViewById(R.id.edt_action_bar);
            editText.setHint("可更换数据接收地址或扫码填入");
            TextView textView = view.findViewById(R.id.tv_action_bar);
            textView.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {
                    String input = editText.getText() + "";
                    if (!TextUtils.isEmpty(input)) {
                        if (input.contains("https://") || input.contains("http://")) {
                            ToolBox.updateServerUrl(input);
                            runText.setText(String.format("当前数据接收地址：%s", ToolBox.getServerUrl()));
                            ToolBox.profilePushId(MainActivity.this);
                            Toast.makeText(MainActivity.this, "数据接收 URL，设置为：" + input, Toast.LENGTH_SHORT).show();
                            SensorsDataAPI.sharedInstance().track("InputUrlOk");
                        }
                        // 隐藏软键盘
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                        }
                    }
                }
            });
            view.findViewById(R.id.scan_action_bar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 扫码
                    ToolBox.scanBar(MainActivity.this);
                }
            });
        }
    }

    /**
     * 点击复制
     */
    @Override
    public void onClick(View v) {
        String copyPushId = "";
        switch (v.getId()) {
            case R.id.ll_main_jg:
                copyPushId = textView_jpush.getText() + "";
                break;
            case R.id.ll_main_gt:
                copyPushId = textView_getui.getText() + "";
                break;
            case R.id.ll_main_um:
                copyPushId = textView_umeng.getText() + "";
                break;
            case R.id.ll_main_xm:
                copyPushId = textView_xiaomi.getText() + "";
                break;
            case R.id.ll_main_hw:
                copyPushId = textView_huawei.getText() + "";
                break;
            case R.id.ll_main_xg:
                copyPushId = textView_xinge.getText() + "";
                break;
            case R.id.ll_main_oppo:
                copyPushId = textView_oppo.getText() + "";
                break;
            case R.id.ll_main_meizu:
                copyPushId = textView_meizu.getText() + "";
                break;
            default:
                break;
        }
        ToolBox.copy(copyPushId, this);
        Toast.makeText(this, "已复制：" + copyPushId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示推送 ID
     */
    private void refreshDisplay(Context context) {
        setText(textView_jpush, JPushInterface.getRegistrationID(context));
        setText(textView_xiaomi, String.format("android_%s", MiPushClient.getRegId(context)));
        setText(textView_getui, PushManager.getInstance().getClientid(context));
        setText(textView_umeng, PushAgent.getInstance(context).getRegistrationId());
        setText(textView_huawei, ToolBox.HUAWEI_PUSH_ID);
        setText(textView_xinge, XGPushConfig.getToken(this));

        if (com.heytap.mcssdk.PushManager.isSupportPush(this.getApplicationContext())) {
            setText(textView_oppo, com.heytap.mcssdk.PushManager.getInstance().getRegisterID());
        } else {
            setText(textView_oppo,"请使用 OPPO 手机！(或一加/realme)");
        }
        setText(textView_meizu, com.meizu.cloud.pushsdk.PushManager.getPushId(context));
    }

    private void setText(TextView textView, String pushId) {
        if (!TextUtils.isEmpty(pushId) && !"android_null".equals(pushId)) {
            //  "推送 ID"
            textView.setText(pushId);
        } else {
            if (textView.getId() == R.id.tv_huawei) {
                textView.setText("手机内，需要 \"华为移动服务\" 3.0.1.303 以上版本！");
                return;
            } else if (textView.getId() == R.id.tv_meizu) {
                textView.setText("请使用魅族手机!");
                return;
            }
            textView.setText("初始化失败，请杀掉 App 重新打开！！！\n\n如果重新打开后，还显示此文字，请联系 @杨站昆");
        }
    }

    /**
     * 扫码的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ToolBox.handleScanResult(requestCode, resultCode, data, MainActivity.this, runText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sfResultReceiver);
    }

    /**
     * OPPO 推送，开发者必须自己创建通知通道
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "通道001";
            NotificationChannel channel = new NotificationChannel("001", channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("通道001的描述");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
