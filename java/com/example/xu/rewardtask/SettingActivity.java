package com.example.xu.rewardtask;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingActivity extends AppCompatActivity {

    private String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initButton_Icon();
        initButton_Description();
        initButton_Password();
        initButton_Logout();
    }

    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;

    void initButton_Icon() {
        Button icon = (Button) findViewById(R.id.Setting_Icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);

                getAlbum.setType(IMAGE_TYPE);

                startActivityForResult(getAlbum, IMAGE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量
            Log.e(TAG, "ActivityResult resultCode error");
            return;
        }

        //此处的用于判断接收的Activity是不是你想要的那个

        if (requestCode == IMAGE_CODE) {

            //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
            ContentResolver resolver = getContentResolver();

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(resolver, data.getData());

                // 发给服务器
                new sendHeadImg().execute(image);

                // 保存到文件
                File file = new File(getFilesDir() + "/avatar");
                if (!file.exists()) {
                    file.mkdirs();
                }

                File avatar = new File(getFilesDir() + "/avatar/Avatar.png");
                if (avatar.exists()) {
                    avatar.delete();
                }

                OutputStream stream = new FileOutputStream(avatar);
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();

                CurrentUser.getInstance().setHeadPath(getFilesDir() + "/avatar/Avatar.png");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    void initButton_Description() {
        Button description = (Button) findViewById(R.id.Setting_Description);
        description.setOnClickListener(new View.OnClickListener() {
            private AlertDialog dialog = null;
            private EditText editText;
            private Button sure;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_setting_description_dialog, null);

                editText = (EditText) dialogView.findViewById(R.id.Setting_Description_Edit);
                editText.setText(CurrentUser.getInstance().getDescription());
                sure = (Button) dialogView.findViewById(R.id.Setting_Description_Sure);
                sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new changeInfo().execute(CurrentUser.getInstance().getPassword(), editText.getText().toString());
                        dialog.cancel();
                    }
                });

                Button cancel = (Button) dialogView.findViewById(R.id.Setting_Description_Cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                builder.setView(dialogView);
                dialog = builder.create();
                dialog.show();

                editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        sure.callOnClick();
                        return true;
                    }
                });
            }
        });
    }

    void initButton_Password() {
        Button password = (Button) findViewById(R.id.Setting_Password);
        password.setOnClickListener(new View.OnClickListener() {
            private AlertDialog dialog = null;
            private EditText oldPass;
            private EditText newPass;
            private EditText confirmPass;
            private Button sure;

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_setting_password_dialog, null);

                oldPass = (EditText) dialogView.findViewById(R.id.Setting_OldPassword_Edit);
                newPass = (EditText) dialogView.findViewById(R.id.Setting_NewPassword_Edit);
                confirmPass = (EditText) dialogView.findViewById(R.id.Setting_ConfirmPassword_Edit);

                oldPass.setText("");
                newPass.setText("");
                confirmPass.setText("");

                sure = (Button) dialogView.findViewById(R.id.Setting_Password_Sure);
                sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (oldPass.getText().toString().equals("")) {
                            Toast.makeText(SettingActivity.this, "请输入原密码", Toast.LENGTH_SHORT).show();
                            oldPass.requestFocus();
                        } else if (newPass.getText().toString().equals("")) {
                            Toast.makeText(SettingActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();
                            newPass.requestFocus();
                        } else if (confirmPass.getText().toString().equals("")) {
                            Toast.makeText(SettingActivity.this, "请再次输入新密码", Toast.LENGTH_SHORT).show();
                            confirmPass.requestFocus();
                        } else if (!oldPass.getText().toString().equals(CurrentUser.getInstance().getPassword())) {
                            Toast.makeText(SettingActivity.this, "原密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                            oldPass.requestFocus();
                        } else if (!newPass.getText().toString().equals(confirmPass.getText().toString())) {
                            Toast.makeText(SettingActivity.this, "两次密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                            confirmPass.requestFocus();
                        } else {
                            new changeInfo().execute(confirmPass.getText().toString(), CurrentUser.getInstance().getDescription());
                            dialog.cancel();
                        }
                    }
                });

                Button cancel = (Button) dialogView.findViewById(R.id.Setting_Password_Cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                builder.setView(dialogView);
                dialog = builder.create();
                dialog.show();

                confirmPass.setImeOptions(EditorInfo.IME_ACTION_DONE);
                confirmPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        sure.callOnClick();
                        return true;
                    }
                });
            }
        });
    }

    void initButton_Logout() {
        Button logout = (Button) findViewById(R.id.Setting_Logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentUser.getInstance().UserLogout(SettingActivity.this);
                CurrentUser.getInstance().clearFile(SettingActivity.this);

                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                intent.putExtra("LastActivity", "Setting");
                intent.putExtra("Statue", "Logout");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_top, R.anim.slide2bottom);
            }
        });
    }

    class sendHeadImg extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap img = params[0];
            HttpURLConnection connection;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/sendImage");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                connection.setDoInput(true);

                /*ByteArrayOutputStream output = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 100, output);
                byte[] bytes = output.toByteArray();
                String imgStr = new String(bytes, "UTF-8");

                PrintWriter writer = new PrintWriter(connection.getOutputStream());

                writer.write("username=" + CurrentUser.getInstance().getUserName() + "&image=" + imgStr);
                writer.flush();
                writer.close();

                Log.i(TAG, imgStr);*/

                // TODO 直接发图片

                connection.setRequestProperty("Content-Type", "multipart/form-data");


                InputStream is = connection.getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONObject jsonObject = new JSONObject(builder.toString());
                return jsonObject.getString("Status");

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (IOException e) {
                e.printStackTrace();
                return "InternetGG";
            } catch (JSONException e) {
                e.printStackTrace();
                return "InternetGG";
            }
        }

        private AlertDialog dialog = null;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

            dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(SettingActivity.this, R.anim.roteting));
            ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在修改...");
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
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG, s);
            if (!s.equals("Success"))
                Toast.makeText(SettingActivity.this, "照片上传失败", Toast.LENGTH_SHORT).show();
            if (dialog != null)
                dialog.cancel();
        }
    }

    class changeInfo extends AsyncTask<String, Void, String> {

        private String newPassword;
        private String newDescription;

        @Override
        protected String doInBackground(String... params) {
            newPassword = params[0];
            newDescription = params[1];

            HttpURLConnection connection;
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/updateInfo?username=" + CurrentUser.getInstance().getUserName()
                        + "&password=" + newPassword + "&description=" + newDescription);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                StringBuilder builder = new StringBuilder();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONObject jsonObject = new JSONObject(builder.toString());
                if (jsonObject.getString("Status") != null)
                    return jsonObject.getString("Status");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

            dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(SettingActivity.this, R.anim.roteting));
            ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在修改...");
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
            if (s != null && s.equals("Success")) {
                CurrentUser.getInstance().setDescription(newDescription);
                CurrentUser.getInstance().setPassword(newPassword);
                CurrentUser.getInstance().writeToFile(SettingActivity.this);
                Toast.makeText(SettingActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
            }
            if (dialog != null)
                dialog.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide2bottom);
    }
}
