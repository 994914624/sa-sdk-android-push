package com.sensorsdata.android.push;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.sensorsdata.android.push.yzk.JsonViewerAdapter;
import com.yuyh.jsonviewer.library.JsonRecyclerView;

import org.json.JSONObject;

/**
 * 使用一个 Activity 来做 push intent 的处理跳转
 * <p>
 * uri ---> yang://www.test.com/path?custom=aaa
 * <p>
 * intentUri ---> intent://www.test.com/path?custom=aaa#Intent;scheme=yang;launchFlags=0x10000000;component=com.sensorsdata.android.push/.HandlePushActivity;end
 * <p>
 * intent://www.test.com/path?custom=aaa#Intent;scheme=yang;launchFlags=0x10000000;component=com.sensorsdata.android.push/.HandlePushActivity;S.sf_data={"aa":"bb"};end
 */
public class HandlePushActivity extends AppCompatActivity {

    private static final String TAG = "HandlePushActivity";
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCustom;
    private JsonRecyclerView rcSfData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Log.i(TAG, "onCreate");
        initActionBar();
        iniView();
        handlePushIntent();
    }

    private void iniView() {
        tvTitle = findViewById(R.id.push_title);
        tvContent = findViewById(R.id.push_content);
        tvCustom = findViewById(R.id.push_custom);
        rcSfData = findViewById(R.id.push_sf_data);
    }

    private void initActionBar() {
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    /**
     * 处理推送消息的 Intent
     */
    private void handlePushIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);
            Log.i(TAG, "intentUri：" + intentUri);

            // 推送 title content
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            // TODO 拿到 sf_data 埋点打开推送消息事件
            String sf_data = intent.getStringExtra("sf_data");
            Log.i(TAG, String.format("title： %s。content：%s。sf_data： %s。", title, content, sf_data));
            updateUI(title, content, sf_data);
        }
    }

    /**
     * 更新数据
     */
    private void updateUI(String title, String content, String sf_data) {
        tvTitle.setText(Html.fromHtml("推送标题：" + "<font color=\"#3AB54A\"><b>" + title + "</b></font>"));
        tvContent.setText(Html.fromHtml("推送内容：" + "<font color=\"#3AB54A\"><b>" + content + "</b></font>"));
        if (!TextUtils.isEmpty(sf_data)) {
            try {
                JSONObject Json = new JSONObject(sf_data).optJSONObject("customized");
                if (Json != null) {
                    tvCustom.setText(Html.fromHtml("自定义字段：" + "<font color=\"#3AB54A\"><b>" + Json.toString() + "</b></font>"));
                }
                //rcSfData.bindJson(sf_data);
                rcSfData.setAdapter(new JsonViewerAdapter(sf_data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
