package com.example.xu.rewardtask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private CurrentUser currentUser;
    private TextView userNameTV;
    private ImageView icon;
    private String HeadImgPath = null; // 当前显示的头像的路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userNameTV = (TextView) findViewById(R.id.MainActivity_UserName);
        icon = (ImageView) findViewById(R.id.MainActivity_Icon);
        currentUser = CurrentUser.getInstance();
        initListView();
        initPersonPart();
        initSettingButton();
    }

    private List<Map<String, Object>> TypeList = null;
    private SimpleAdapter TypeAdapter = null;
    private ListView TypeListView = null;

    private void initListView() {
        if (TypeList == null) {
            TypeList = new LinkedList<>();
            String[] typesNameToShow = getResources().getStringArray(R.array.TypeNameText);
            String[] typesNameToGetIcon = getResources().getStringArray(R.array.TypeNameForCoding);
            for (int i = 0; i < typesNameToShow.length; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("Name", typesNameToShow[i]);
                item.put("Icon", getResources().getIdentifier("type_icon_" + typesNameToGetIcon[i], "mipmap", getPackageName()));
                TypeList.add(item);
            }
        }

        if (TypeAdapter == null) {
            String[] from = {"Icon", "Name"};
            int[] to = {R.id.Main_TypeListItem_Icon, R.id.Main_TypeListItem_Name};
            TypeAdapter = new SimpleAdapter(MainActivity.this, TypeList, R.layout.activity_main_type_list_item, from, to);
        }

        if (TypeListView == null) {
            TypeListView = (ListView) findViewById(R.id.MainActivity_TypeList);
            TypeListView.setAdapter(TypeAdapter);
        }

        TypeListView.setOnItemClickListener(new TypeItemClickListener());
    }

    class TypeItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, Object> item = (HashMap<String, Object>) TypeListView.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, AreaMissionListActivity.class);
            intent.putExtra("TypeName", (String) item.get("Name"));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);
        }
    }


    private void initPersonPart() {
        View personPart = findViewById(R.id.MainActivity_PersonPart);
        personPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser.isLogin())
                    startActivity(new Intent(MainActivity.this, PersonActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, LoginOrRegisterActivity.class));

                overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
            }
        });

        if (currentUser.isLogin()) {
            userNameTV.setText(currentUser.getUserName());
            if (currentUser.getHeadPath() != null) {
                HeadImgPath = currentUser.getHeadPath();
                new getIconAsyncTask().execute();
            }
        }
    }

    private void initSettingButton() {
        ImageButton settingButton = (ImageButton) findViewById(R.id.MainActivity_Setting_Button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentUser.isLogin()) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.stay_half_sec);
                } else {
                    Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginOrRegisterActivity.class));
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
                }
            }
        });
    }

    class getIconAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            if (CurrentUser.getInstance().getHeadPath() != null) {
                try {
                    return CurrentUser.getInstance().getHeadBitmap(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                // TODO 从服务器获取头像
                // 保存到本地的文件中
                // currentUser.setIconPath(本地的路径);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null)
                icon.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onResume() {

        if (CurrentUser.getInstance().getHeadPath() == null || HeadImgPath != null) {
            HeadImgPath = null;
            icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.person_icon_unlogin));
        } else if (CurrentUser.getInstance().getHeadPath() != null) {
            if (HeadImgPath == null || !HeadImgPath.equals(CurrentUser.getInstance().getHeadPath())) {
                try {
                    icon.setImageBitmap(CurrentUser.getInstance().getHeadBitmap(MainActivity.this));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            HeadImgPath = CurrentUser.getInstance().getHeadPath();
        }

        Intent intent = this.getIntent();

        if (intent.getStringExtra("LastActivity") != null && intent.getStringExtra("LastActivity").equals("LOR")) {
            userNameTV.setText(currentUser.getUserName());
            if (intent.getStringExtra("Statue") != null && intent.getStringExtra("Statue").equals("Login"))
                new getIconAsyncTask().execute();
            Log.i(TAG, "LoginSuccess");
        }

        if (intent.getStringExtra("LastActivity") != null && intent.getStringExtra("LastActivity").equals("Setting")) {
            if (intent.getStringExtra("Statue") != null && intent.getStringExtra("Statue").equals("Logout")) {
                userNameTV.setText("请登录");
                icon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.person_icon_unlogin));
            }
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // must store the new intent unless getIntent() will
        // return the old one
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CurrentUser.getInstance().writeToFile(MainActivity.this);
        if (keyCode == KeyEvent.KEYCODE_BACK)
            System.exit(0);
        return super.onKeyDown(keyCode, event);
    }
}
