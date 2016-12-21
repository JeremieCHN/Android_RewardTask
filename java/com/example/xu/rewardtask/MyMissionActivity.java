package com.example.xu.rewardtask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MyMissionActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> arrayList;
    private findMyMissions myMissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_mission);


        listView = (ListView) findViewById(R.id.MyMission_ListView);
        myMissions = new findMyMissions();
        myMissions.execute();

        arrayList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_my_mission_list_item, R.id.MyMission_MissionName, arrayList);
        listView.setAdapter(adapter);
    }

    public class findMyMissions extends AsyncTask<Void, Void, String> {
        private String userName;
        private String TAG = "findMyMissionsActivity";

        @Override
        protected String doInBackground(Void... params) {
            if (isCancelled()) return null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/ownMission?username=" + URLEncoder.encode(userName, "UTF-8"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(5000);
                connection.connect();

                if (isCancelled()) return null;

                StringBuffer buffer = new StringBuffer();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();

                Log.i(TAG, buffer.toString());

                return buffer.toString();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (ConnectException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        private AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            userName = CurrentUser.getInstance().getUserName();

            AlertDialog.Builder builder = new AlertDialog.Builder(MyMissionActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

            dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(MyMissionActivity.this, R.anim.roteting));
            ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在获取列表...");

            builder.setView(dialogView);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        finish();
                        return true;
                    } else
                        return false;
                }
            });

            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog != null)
                dialog.cancel();

            if (result == null)
                return;

            if (result.equals("InternetGG")) {
                Toast.makeText(MyMissionActivity.this, "网络或服务器异常，请稍后再试", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    arrayList.add(jsonObject.getString("Missionname"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (myMissions != null)
            myMissions.cancel(true);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
    }
}
