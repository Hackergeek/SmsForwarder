package com.idormy.sms.forwarder.sender;

import android.os.Handler;
import android.util.Log;

import com.idormy.sms.forwarder.model.vo.GotifySettingVo;
import com.idormy.sms.forwarder.utils.Define;
import com.idormy.sms.forwarder.utils.LogUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SenderGotifyMsg extends SenderBaseMsg {

    static final String TAG = "SenderGotifyMsg";

    public static void sendMsg(final long logId, final Handler handError, final RetryIntercepter retryInterceptor, GotifySettingVo gotifySettingVo, String title, String message) throws Exception {

        //具体消息内容
        if (message == null || message.isEmpty()) return;

        RequestBody formBody = new FormBody.Builder()
                .add("title", title)
                .add("message", message)
                .add("priority", gotifySettingVo.getPriority())
                .build();

        String requestUrl = gotifySettingVo.getWebServer();
        Log.i(TAG, "requestUrl:" + requestUrl);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //设置重试拦截器
        if (retryInterceptor != null) builder.addInterceptor(retryInterceptor);
        //设置读取超时时间
        OkHttpClient client = builder
                .readTimeout(Define.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Define.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .connectTimeout(Define.REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(requestUrl).post(formBody).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            final String responseStr = Objects.requireNonNull(response.body()).string();
            Log.d(TAG, "Response：" + response.code() + "，" + responseStr);
            Toast(handError, TAG, "发送状态：" + responseStr);

            //TODO:粗略解析是否发送成功
            if (response.isSuccessful()) {
                LogUtil.updateLog(logId, 2, responseStr);
            } else {
                LogUtil.updateLog(logId, 1, responseStr);
            }
        }

    }

}
