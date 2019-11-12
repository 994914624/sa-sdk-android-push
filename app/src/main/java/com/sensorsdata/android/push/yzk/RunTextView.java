package com.sensorsdata.android.push.yzk;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by yzk on 2019-11-06
 */

public class RunTextView extends AppCompatTextView {

    public RunTextView(Context context) {
        super(context);
    }

    public RunTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RunTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
