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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ReleaseNewMissionActivity extends AppCompatActivity {
    private String TAG = "ReleaseNewMission";
    private EditText missionName;
    private EditText content;
    private EditText reward;
    private String type;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_new_mission);

        missionName = (EditText) findViewById(R.id.ReleaseMission_MissionName);
        content = (EditText) findViewById(R.id.ReleaseMission_MissionContent);
        reward = (EditText) findViewById(R.id.ReleaseMission_RewardEdit);
        missionName.requestFocus();

        Spinner citySpinner = (Spinner) findViewById(R.id.ReleaseMission_City);
        Spinner typeSpinner = (Spinner) findViewById(R.id.ReleaseMission_Type);

        final String[] cities = getResources().getStringArray(R.array.CityList);
        final String[] types = getResources().getStringArray(R.array.TypeNameText);

        SpinnerAdapter cityAdapter = new ArrayAdapter<String>(this, R.layout.activity_area_mission_list_spinner_item, R.id.AreaMissionList_SpinnerText,cities);
        citySpinner.setAdapter(cityAdapter);

        SpinnerAdapter typeAdapter = new ArrayAdapter<String>(this, R.layout.activity_area_mission_list_spinner_item, R.id.AreaMissionList_SpinnerText, types);
        typeSpinner.setAdapter(typeAdapter);

        type = getIntent().getStringExtra("Type");
        city = getIntent().getStringExtra("City");

        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(city))
                citySpinner.setSelection(i);
        }

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(type))
                typeSpinner.setSelection(i);
        }

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = cities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = types[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        reward.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int i = Integer.valueOf(reward.getText().toString());
                    if (i < 0 || i > CurrentUser.getInstance().getMoney())
                        reward.setText(Integer.toString(CurrentUser.getInstance().getMoney()));
                }
            }
        });

        Button button = (Button) findViewById(R.id.ReleaseMission_ReleaseButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (missionName.getText().toString().equals("")) {
                    Toast.makeText(ReleaseNewMissionActivity.this, "请输入任务名", Toast.LENGTH_SHORT).show();
                    missionName.requestFocus();
                } else if (content.getText().toString().equals("")) {
                    Toast.makeText(ReleaseNewMissionActivity.this, "请输入任务详情", Toast.LENGTH_SHORT).show();
                    content.requestFocus();
                } else {
                    new sendMission().execute();
                }
            }
        });
    }

    class sendMission extends AsyncTask<Void, Void, String> {

        private AlertDialog dialog = null;

        @Override
        protected String doInBackground(Void... params) {
            Log.e(TAG, "City:"+city);
            Log.e(TAG, "Type:"+type);
            // TODO 发给服务器
            return null;
        }

        @Override
        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(ReleaseNewMissionActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

            dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(ReleaseNewMissionActivity.this, R.anim.roteting));
            ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在发布...");
            builder.setView(dialogView);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0);
                }
            });
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.cancel();
            if (s != null && s.equals("Success")) {
                Toast.makeText(ReleaseNewMissionActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                onBackPressed();
                startActivity(new Intent(ReleaseNewMissionActivity.this, AreaMissionListActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
            } else
                Toast.makeText(ReleaseNewMissionActivity.this, "发布失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // must store the new intent unless getIntent() will
        // return the old one
    }

    @Override
    public  void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
    }
}
