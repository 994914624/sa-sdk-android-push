package com.sensorsdata.android.push;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.umeng.message.PushAgent;

import static anet.channel.util.Utils.context;

public class SettingActivity extends AppCompatActivity {
    private final String BOOK_ID = "book_id";
    private final String BOOK_NAME = "book_name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        PushAgent.getInstance(this).onAppStart();
        TextView textView = findViewById(R.id.params);
        Bundle paramsBundle = getIntent().getExtras();
        if (paramsBundle != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (paramsBundle.containsKey(BOOK_ID)) {
                stringBuilder.append(paramsBundle.getString(BOOK_ID)).append("\n");
            }

            if (paramsBundle.containsKey(BOOK_NAME)) {
                stringBuilder.append(paramsBundle.getString(BOOK_NAME)).append("\n");
            }
            textView.setText(stringBuilder.toString());
        }
    }
}
