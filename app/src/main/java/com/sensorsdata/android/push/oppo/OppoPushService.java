package com.sensorsdata.android.push.oppo;

import android.content.Context;
import android.util.Log;

import com.heytap.mcssdk.PushService;
import com.heytap.mcssdk.mode.AppMessage;
import com.heytap.mcssdk.mode.CommandMessage;
import com.heytap.mcssdk.mode.SptDataMessage;

/**
 * Created by yzk on 2019-12-19
 */

public class OppoPushService extends PushService {

    @Override
    public void processMessage(Context context, SptDataMessage sptDataMessage) {
        super.processMessage(context, sptDataMessage);
        if(sptDataMessage == null)return;
        Log.i("OPPO","sptDataMessage :" + sptDataMessage.toString());
    }

    @Override
    public void processMessage(Context context, CommandMessage commandMessage) {
        super.processMessage(context, commandMessage);
        Log.i("OPPO","commandMessage :" + commandMessage.toString());
    }

    @Override
    public void processMessage(Context context, AppMessage appMessage) {
        super.processMessage(context, appMessage);
        if(appMessage == null)return;
        Log.i("OPPO","appMessage :" + appMessage.toString());
    }
}
