package com.sensorsdata.android.push.huawei;

import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.android.push.SFConstant;
import com.sensorsdata.android.push.SFUtils;
import com.sensorsdata.android.push.yzk.ToolBox;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yzk on 2019-11-09
 *
 * 需要 "华为移动服务" 3.0.1.303
 */

public class HmsPushService extends HmsMessageService {

    private static final String TAG = "华为 push";

    /**
     * EMUI 10.0 及以上版本的华为设备上，getToken 接口直接返回 token。
     *  如果当次调用失败 PUSH 会自动重试申请，成功后则以 onNewToken 接口返回。
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG,"--onNewToken--> ："+token);
        if(!TextUtils.isEmpty(token)){
            try {
                ToolBox.HUAWEI_PUSH_ID = token;
                SensorsDataAPI.sharedInstance().profileSet(new JSONObject().put("huawei_id",token));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用于接收透传消息
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(TAG,"onMessageReceived：");
        // TODO(developer): Handle HCM messages here.
        // Check if message contains a data payload.
        if (remoteMessage.getData().length() > 0) {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());

        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.i(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        // Also if you intend on generating your own notifications as a result of a received HCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
