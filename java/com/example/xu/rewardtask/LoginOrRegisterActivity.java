package com.example.xu.rewardtask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class LoginOrRegisterActivity extends AppCompatActivity {
    private String TAG = "LoginOrRegisterActivity";
    private String state = "Login";
    private EditText userName;
    private EditText password;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_rigister);

        userName = (EditText) findViewById(R.id.LOR_UserName);
        password = (EditText) findViewById(R.id.LOR_Password);

        initChoosePart();
        initUserNameTip();
        initSoftKey();
        initButton();
    }

    private TextView loginTV;
    private TextView registerTV;

    void initChoosePart() {
        loginTV = (TextView) findViewById(R.id.LOR_ChooseLogin);
        registerTV = (TextView) findViewById(R.id.LOR_ChooseRegister);

        final float normalSize = loginTV.getTextSize();
        final float focusSize = normalSize * 1.5f;

        if (state.equals("Login"))
            loginTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, focusSize);
        else
            registerTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, focusSize);

        View choosePart = findViewById(R.id.LOR_ChooseLOR);

        choosePart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) findViewById(R.id.LOR_Button);
                if (state.equals("Register")) {
                    registerTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, normalSize);
                    loginTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, focusSize);

                    button.setText(getResources().getString(R.string.LRActivity_Login));

                    state = "Login";
                } else {
                    registerTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, focusSize);
                    loginTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, normalSize);

                    button.setText(getResources().getString(R.string.LRActivity_Register));

                    state = "Register";
                }
            }
        });
    }

    void initSoftKey() {
        userName.setPrivateImeOptions("登陆");
        userName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                button.callOnClick();
                return true;
            }
        });
        password.setPrivateImeOptions("登陆");
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                button.callOnClick();
                return true;
            }
        });
    }

    void initUserNameTip() {
        userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (state.equals("Login")) {
                    userName.setTextColor(getResources().getColor(R.color.colorBlack));
                    return;
                }

                if (!hasFocus)
                    new CheckNameAsyncTask().execute();
                else
                    userName.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
    }

    class CheckNameAsyncTask extends AsyncTask<Void, Void, String> {

        private String nameStr;

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/registerServlet?username=" + nameStr + "&operation=check");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(5000);
                connection.connect();

                StringBuffer buffer = new StringBuffer();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();

                return buffer.toString();

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

        @Override
        protected void onPreExecute() {
            nameStr = userName.getText().toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null)
                return;

            if (result.equals("InternetGG")) {
                Toast.makeText(LoginOrRegisterActivity.this, "网络或服务器异常，请稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                if (!jsonObject.getString("Status").equals("Valid username")) {
                    userName.setTextColor(getResources().getColor(R.color.colorRed));
                    Toast.makeText(LoginOrRegisterActivity.this, "该用户名已被注册！", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private AlertDialog dialog = null;

    void initButton() {
        button = (Button) findViewById(R.id.LOR_Button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userName.getText().toString().equals(""))
                    Toast.makeText(LoginOrRegisterActivity.this, getResources().getString(R.string.LRActivity_UserNameNull), Toast.LENGTH_SHORT).show();
                else if (password.getText().toString().equals(""))
                    Toast.makeText(LoginOrRegisterActivity.this, getResources().getString(R.string.LRActivity_PasswordNull), Toast.LENGTH_SHORT).show();
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginOrRegisterActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

                    dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(LoginOrRegisterActivity.this, R.anim.roteting));
                    ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在" + (state.equals("Login") ? "登录" : "注册") + "...");

                    builder.setView(dialogView);
                    dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
                                return true;
                            else
                                return false;

                        }
                    });

                    dialog.show();
                    if (state.equals("Login"))
                        new LoginAsyncTask().execute();
                    else if (state.equals("Register")) {
                        if (userName.getCurrentTextColor() != getResources().getColor(R.color.colorRed))
                            new RegisterAsyncTask().execute();
                        else
                            Toast.makeText(LoginOrRegisterActivity.this, "该用户名已被注册！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    class LoginAsyncTask extends AsyncTask<Void, Void, String> {

        private String nameStr;
        private String passwordStr;

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/loginServlet?username=" + nameStr + "&password=" + passwordStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(5000);
                connection.connect();

                StringBuilder builder = new StringBuilder();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                Log.i(TAG, builder.toString());

                return builder.toString();
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
                if (dialog != null)
                    dialog.cancel();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            nameStr = userName.getText().toString();
            passwordStr = password.getText().toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null)
                return;

            if (result.equals("InternetGG")) {
                Toast.makeText(LoginOrRegisterActivity.this, "网络或服务器异常，请稍后再试", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("Status").equals("Please register your account first"))
                    Toast.makeText(LoginOrRegisterActivity.this, "未找到此用户，请先注册！", Toast.LENGTH_SHORT).show();
                else if (jsonObject.getString("Status").equals("Wrong password"))
                    Toast.makeText(LoginOrRegisterActivity.this, "用户名密码不匹配，请检查用户名和密码！", Toast.LENGTH_SHORT).show();
                else if (jsonObject.getString("Status").equals("Success")) {
                    Toast.makeText(LoginOrRegisterActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginOrRegisterActivity.this, MainActivity.class);
                    intent.putExtra("LastActivity", "LOR");
                    intent.putExtra("Statue", "Login");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);

                    CurrentUser.getInstance().UserLogin(jsonObject);
                    CurrentUser.getInstance().writeToFile(LoginOrRegisterActivity.this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (dialog != null)
                dialog.cancel();
        }
    }

    class RegisterAsyncTask extends AsyncTask<Void, Void, String> {

        private String nameStr;
        private String passwordStr;

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/registerServlet?username=" + nameStr + "&password=" + passwordStr + "&operation=register");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(5000);
                connection.connect();

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
                if (dialog != null)
                    dialog.cancel();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            nameStr = userName.getText().toString();
            passwordStr = password.getText().toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog != null)
                dialog.cancel();

            if (result == null)
                return;

            if (result.equals("InternetGG")) {
                Toast.makeText(LoginOrRegisterActivity.this, "网络或服务器异常，请稍后再试", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.getString("Status").equals("Username has been registered"))
                    Toast.makeText(LoginOrRegisterActivity.this, "此用户已被注册，请更换！", Toast.LENGTH_SHORT).show();
                else if (jsonObject.getString("Status").equals("Success")) {
                    Toast.makeText(LoginOrRegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginOrRegisterActivity.this, MainActivity.class);
                    intent.putExtra("LastActivity", "LOR");
                    intent.putExtra("Statue", "Register");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);

                    CurrentUser.getInstance().UserRegister(nameStr, passwordStr);
                    CurrentUser.getInstance().writeToFile(LoginOrRegisterActivity.this);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class getHeadFromServer extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection;

            try {
                URL url = new URL("http://" + CurrentUser.IP + "/getImage");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);

    }

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(LoginOrRegisterActivity.this, MainActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/
}
