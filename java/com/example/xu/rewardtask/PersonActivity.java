package com.example.xu.rewardtask;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

public class PersonActivity extends AppCompatActivity {

    private CurrentUser currentUser = CurrentUser.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        initView();
        setClickListen();
    }

    void initView() {
        if (currentUser.isLogin()) {
            ((TextView) findViewById(R.id.Person_UserName)).setText(currentUser.getUserName());

            if (currentUser.getDescription() != null)
                ((TextView) findViewById(R.id.Person_Description)).setText(currentUser.getDescription());

            String moneyStr = "持有金币数：" + String.valueOf(currentUser.getMoney());
            ((TextView) findViewById(R.id.Person_Money)).setText(moneyStr);

            if (currentUser.getHeadPath() != null) {
                try {
                    ImageView view = (ImageView) findViewById(R.id.Person_Icon);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    params.height = displayMetrics.widthPixels;
                    view.setLayoutParams(params);
                    view.setImageBitmap(currentUser.getHeadBitmap(PersonActivity.this));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void setClickListen() {
        View Person_Mission_Release = findViewById(R.id.Person_Mission_Release);
        Person_Mission_Release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 跳转 等雨哥的代码
            }
        });

        View Person_Setting = findViewById(R.id.Person_Setting);
        Person_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PersonActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.stay_half_sec);
            }
        });
    }


    @Override
    public void onStart() {
        initView();

        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);
    }
}
