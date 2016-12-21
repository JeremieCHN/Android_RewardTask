package com.example.xu.rewardtask;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class NoticeService extends IntentService {
    private final int RECEIVE = 0;
    private String TAG = "NoticeService";

    public NoticeService() {
        super("com.example.xu.rewardtask.NoticeService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        sendRequest();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private void sendRequest() {
        new Thread(new Runnable() {
            int time = 5000;
            String username = "";

            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    while (true) {
                        SystemClock.sleep(time);
                        try {
                            File file = new File(getFilesDir() + "/log/log.txt");
                            if (!file.exists()) {
                                time = 10000;
                                Log.i(TAG, "The file does not exist.");
                                continue;
                            }

                            Log.i(TAG, "The file exists.");
                            FileInputStream fin = new FileInputStream(getFilesDir() + "/log/log.txt");
                            InputStreamReader reader = new InputStreamReader(fin, "UTF-8");
                            BufferedReader bufferedReader = new BufferedReader(reader);

                            String temp;
                            username = "";
                            while ((temp = bufferedReader.readLine()) != null) {
                                username += temp;
                            }
                            bufferedReader.close();
                            reader.close();
                            fin.close();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (username.equals("")) {
                            continue;
                        }

                        Log.i(TAG, "Username="+username);
                        URL u = new URL("http://"+ CurrentUser.IP +"/AndroidServer/noticeServlet?username=" + username);
                        connection = (HttpURLConnection)u.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(8000);
                        connection.setConnectTimeout(8000);

                        connection.connect();

                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer response = new StringBuffer();

                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject jsonObject = new JSONObject(response.toString());

                        Log.i(TAG, response.toString());

                        Message message = new Message();
                        message.what = RECEIVE;
                        message.obj = jsonObject;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            try {
                switch (message.what) {
                    case RECEIVE:
                        JSONObject jsonObject = (JSONObject) message.obj;
                        if (jsonObject.getString("Status").equals("Success")) {
                            Intent intent = new Intent("NewComment");
                            Bundle bundle = new Bundle();
                            bundle.putString("Username" ,jsonObject.getString("Username"));
                            bundle.putString("Missionname", jsonObject.getString("Missionname"));
                            bundle.putString("Commentor", jsonObject.getString("Commentor"));
                            bundle.putString("Comment", jsonObject.getString("Comment"));

                            bundle.putString("Date", jsonObject.getString("Date"));
                            bundle.putInt("Icon", R.mipmap.app_icon);
                            intent.putExtras(bundle);
                            sendBroadcast(intent);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
