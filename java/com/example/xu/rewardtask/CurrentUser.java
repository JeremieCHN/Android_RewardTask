package com.example.xu.rewardtask;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * 这个类是个单例类
 * 用来保存当前登录的用户的基本信息，这样就不用每次都去读取文件内容了
 * Created by xu国宝 on 2016/12/1.
 */

public class CurrentUser {

    public static String IP = "172.18.70.38:8080";

    private String TAG = "CurrentUser";

    private static CurrentUser currentUser = null;

    private boolean LoginState = false;

    private String userName = null;
    private String description = null;
    private String password = null;
    private int money = 0;

    private String headPath = null;
    private String city = null;

    private CurrentUser() {
    }

    public static CurrentUser getInstance() {
        if (currentUser == null)
            currentUser = new CurrentUser();
        return currentUser;
    }

    public void UserLogin(JSONObject jsonObject) {
        // {"Status":"Success","UserName":"wujy","Description":"this is a test data","Money":10,"Password":"122521"}

        try {
            LoginState = true;
            userName = jsonObject.getString("UserName");
            description = jsonObject.getString("Description");
            password = jsonObject.getString("Password");
            money = jsonObject.getInt("Money");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void UserRegister(String userName_, String password_) {
        LoginState = true;
        userName = userName_;
        password = password_;
    }

    public void UserLogout(Context context) {
        LoginState = false;
        userName = null;
        description = null;
        password = null;
        money = 0;
        headPath = null;

        File file = new File(context.getFilesDir() + "/avatar/Avatar.png");
        if (file.exists())
            file.delete();

    }

    public void readFromFile(Context context) {
        SharedPreferences sp = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);

        userName = sp.getString("UserName", null);
        description = sp.getString("Description", null);
        password = sp.getString("Password", null);
        money = sp.getInt("Money", 0);

        city = sp.getString("City", null);

        if (city == null)
            city = "全国";

        LoginState = (userName != null);

        File head = new File(context.getFilesDir() + "/avatar/Avatar.png");
        if (head.exists())
            headPath = context.getFilesDir() + "/avatar/Avatar.png";
    }

    public void writeToFile(Context context) {
        SharedPreferences sp = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("UserName", userName);
        editor.putString("Description", description);
        editor.putString("Password", password);
        editor.putInt("Money", money);

        editor.putString("City", city);

        editor.apply();
    }

    public void clearFile(Context context) {
        SharedPreferences sp = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.clear().apply();
    }

    public Bitmap getHeadBitmap(Context context) throws IOException {
        return getHeadFromFile(context);
    }

    public Bitmap getHeadFromFile(Context context) throws IOException {
        File file = new File(context.getFilesDir() + "/avatar/Avatar.png");
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(headPath);

            float x, y;
            x = bm.getWidth();
            y = bm.getHeight();
            if (x > y)
                x = y;

            return resize(bm, x, x);
        } else {
            return null;
        }

    }

    Bitmap resize(Bitmap b, float x, float y) {
        int w = b.getWidth();
        int h = b.getHeight();
        float sx = (float) x / w;
        float sy = (float) y / h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(b, 0, 0, w, h, matrix, true);
    }

    public String getUserName() {
        return userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String Description_) {
        description = Description_;
    }

    public String getHeadPath() {
        return headPath;
    }

    public void setHeadPath(String headPath_) {
        headPath = headPath_;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int i) {
        money = i;
    }

    public boolean isLogin() {
        return LoginState;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city_) {
        city = city_;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password_) {
        password = password_;
    }
}
