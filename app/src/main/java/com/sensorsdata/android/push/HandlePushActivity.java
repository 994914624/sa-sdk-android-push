package com.sensorsdata.android.push;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.umeng.message.PushAgent;

/**
 * 使用一个 Activity 来做 push intent 的处理跳转
 * <p>
 * uri ---> yang://www.test.com/path?custom=aaa
 * <p>
 * intentUri ---> intent://www.test.com/path?custom=aaa#Intent;scheme=yang;launchFlags=0x10000000;component=com.sensorsdata.android.push/.HandlePushActivity;end
 */
public class HandlePushActivity extends AppCompatActivity {
    private static final String TAG = "华为 HandlePushActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Log.i(TAG, "onCreate");
        handlePushIntent();
    }

    private void handlePushIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                Log.i(TAG, "uri：" + uri);
                String custom = uri.getQueryParameter("custom");
                Log.i(TAG, "" + custom);
                // TODO 自定义跳转，埋点打开推送消息事件
            }

            String intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);
            Log.i(TAG, "intentUri：" + intentUri);
        }
    }
}
