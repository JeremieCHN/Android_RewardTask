package com.example.xu.rewardtask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MissionDetailActivity extends AppCompatActivity {
    // TODO 待测试

    private String TAG = "MissionDetailActivity";
    private String missionPublisher;
    private String missionName;
    private TextView PublisherTimeTV;
    private TextView tip;
    private ListView commentLV;
    private EditText commentEdit;
    private TextView IsCompletedTip;
    private TextView moneyTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);

        initView();
        new getMissionDetailAsyncTask().execute();
    }

    void initView() {
        Intent intent = getIntent();
        missionPublisher = intent.getStringExtra("UserName");
        missionName = intent.getStringExtra("MissionName");

        PublisherTimeTV = (TextView) findViewById(R.id.MissionDetail_UserTime);
        tip = (TextView) findViewById(R.id.MissionDetail_Tip);
        IsCompletedTip = (TextView) findViewById(R.id.MissionDetail_IsCompleted);

        commentEdit = (EditText) findViewById(R.id.MissionDetail_CommentEdit);
        commentEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);
        commentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    new sendComment().execute(
                            new Comment(missionPublisher, missionName,
                                    commentEdit.getText().toString(),
                                    CurrentUser.getInstance().getUserName(),
                                    new Date(System.currentTimeMillis())));
                    return true;
                }
                return false;
            }
        });

        // 未登录的话直接显示评论编辑的部分
        if (!CurrentUser.getInstance().isLogin())
            findViewById(R.id.MissionDetail_CommentEditPart).setVisibility(View.GONE);

        Button button = (Button) findViewById(R.id.MissionDetail_ReleaseButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, missionPublisher);
                Log.i(TAG, missionName);
                Log.i(TAG, commentEdit.getText().toString());
                Log.i(TAG, CurrentUser.getInstance().getUserName());

                new sendComment().execute(new Comment(missionPublisher, missionName,
                        commentEdit.getText().toString(),
                        CurrentUser.getInstance().getUserName(),
                        new Date(System.currentTimeMillis())));
            }
        });
        ((TextView) findViewById(R.id.MissionDetail_MissionName)).setText(missionName);
        PublisherTimeTV.setText(missionPublisher);

        if (intent.getStringExtra("Date") != null) {
            Date date = new Date(intent.getStringExtra("Date"));
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
            PublisherTimeTV.setText(missionPublisher + "发布于" + dateFormat.format(date));
        }

        initListView();
    }

    private List<Comment> CommentList_Data;
    private CommentAdapter adapter;

    void initListView() {
        commentLV = (ListView) findViewById(R.id.MissionDetail_Comment);
        commentLV.setVisibility(View.GONE);

        CommentList_Data = new LinkedList<>();
        adapter = new CommentAdapter(CommentList_Data);
        commentLV.setAdapter(adapter);

        commentLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // 未登陆点击不会有任何反应
                if (!CurrentUser.getInstance().isLogin())
                    return;

                final Comment item = CommentList_Data.get(position);
                // TODO 采纳按钮没做完
                commentEdit.setText("回复 " + item.getUsername() + ": ");
                commentEdit.requestFocus();
                /*
                if (item.getMissionUsername().equals(missionPublisher)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MissionDetailActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.activity_mission_detail_dialog, null);
                    builder.setView(dialogView);
                    final AlertDialog dialog = builder.create();

                    Button adoptButton = (Button) dialogView.findViewById(R.id.MissionDetail_AdoptComment);
                    adoptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialog.isShowing())
                                dialog.cancel();

                            //item.setAdopt();
                            //adapter.notifyDataSetChanged();
                            new AdoptComment().execute();
                        }
                    });

                    Button replyButton = (Button) dialogView.findViewById(R.id.MissionDetail_ReplyComment);
                    replyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            commentEdit.setText("回复 " + item.getUsername() + ": ");
                            commentEdit.requestFocus();

                            if (dialog.isShowing())
                                dialog.cancel();
                        }
                    });

                    dialog.show();
                } else {
                    commentEdit.setText("回复 " + item.getUsername() + ": ");
                    commentEdit.requestFocus();
                }
                */
            }
        });
    }

    class CommentAdapter extends BaseAdapter {
        private List<Comment> list = null;

        public CommentAdapter(List<Comment> list_) {
            list = list_;
        }

        @Override
        public int getCount() {
            if (list == null)
                return 0;
            else
                return list.size();
        }

        @Override
        public Object getItem(int position) {
            if (list == null)
                return null;
            else
                return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            View convertView;
            ViewHolder viewHolder;
            if (view == null) {
                convertView = LayoutInflater.from(MissionDetailActivity.this).inflate(R.layout.activity_mission_detail_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.commenter = (TextView) convertView.findViewById(R.id.MissionDetail_Comment_Item_UserName);
                viewHolder.commentDetail = (TextView) convertView.findViewById(R.id.MissionDetail_Comment_Item_Comment);
                viewHolder.commentDate = (TextView) convertView.findViewById(R.id.MissionDetail_Comment_Item_Date);
                convertView.setTag(viewHolder);
            } else {
                convertView = view;
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.commenter.setText(list.get(i).getUsername());
            viewHolder.commentDetail.setText(list.get(i).getComment());
            DateFormat format = new SimpleDateFormat("yy-MM-dd HH-mm");
            viewHolder.commentDate.setText(format.format(list.get(i).getDate()));

            if (list.get(i).isAdopt())
                viewHolder.commentDetail.setTextColor(getResources().getColor(R.color.colorRed));
            return convertView;
        }

        class ViewHolder {
            public TextView commenter;
            public TextView commentDetail;
            public TextView commentDate;
        }
    }

    class getMissionDetailAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/missionServlet?mission=" +
                        URLEncoder.encode(missionName, "UTF-8") + "&publisher=" +
                        URLEncoder.encode(missionPublisher, "UTF-8"));

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                StringBuffer buffer = new StringBuffer();
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(buffer.toString());
                if (jsonObject.getString("Status").equals("Fail")) {
                    return "Fail";
                }

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
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return "Fail";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("Fail")) {
                Toast.makeText(MissionDetailActivity.this, "获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
            } else if (s.equals("InternetGG")) {
                Toast.makeText(MissionDetailActivity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    Log.e(TAG, s);
                    JSONObject jsonObject = new JSONObject(s);

                    String dateStr = jsonObject.getString("Date");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = format.parse(dateStr.substring(0, 19));

                    DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
                    PublisherTimeTV.setText(missionPublisher + "发布于" + dateFormat.format(date));

                    TextView contentTV = (TextView) findViewById(R.id.MissionDetail_Content);
                    contentTV.setText(URLDecoder.decode(jsonObject.getString("Content"), "UTF-8"));

                    moneyTV = (TextView) findViewById(R.id.MissionDetail_Money);
                    moneyTV.setText(jsonObject.getString("Gold"));

                    if (jsonObject.getString("IsComplete").equals(Mission.COMPLETED)) {
                        IsCompletedTip.setText("此任务的赏金已经被领取");
                    } else {
                        IsCompletedTip.setText("此任务的赏金尚未被领取");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                new getCommentAsyncTask().execute();
            }
        }
    }

    class getCommentAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/getComment?mission=" +
                        URLEncoder.encode(missionName, "UTF-8") + "&publisher=" +
                        URLEncoder.encode(missionPublisher, "UTF-8"));

                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(4000);
                connection.setRequestMethod("GET");
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

                JSONArray array = new JSONArray(buffer.toString());
                String statue = array.optJSONObject(0).getString("Status");
                if (statue != null && statue.equals("Success")) {
                    CommentList_Data.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        CommentList_Data.add(new Comment(jsonObject));
                    }
                }
                return statue;
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
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return "Fail";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("Fail")) {
                Toast.makeText(MissionDetailActivity.this, "评论获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
            } else if (s.equals("InternetGG")) {
                Toast.makeText(MissionDetailActivity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
            } else if (s.equals("Empty")) {
                tip.setText("暂无评论");
            } else if (s.equals("Success")) {
                tip.setVisibility(View.GONE);
                commentLV.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        }
    }

    class AdoptComment extends AsyncTask<Integer, Void, Integer> {

        String MissionNameSend;
        String MissionPublisherSend;
        String CommentPublisherSend;
        String DateSend;
        String GoldSend;

        @Override
        protected void onPreExecute() {
            try {
                MissionNameSend = URLEncoder.encode(missionName, "UTF-8");
                MissionPublisherSend = URLEncoder.encode(missionPublisher, "UTF-8");
                GoldSend = moneyTV.getText().toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            // TODO
            HttpURLConnection connection = null;

            try {
                CommentPublisherSend = URLEncoder.encode(CommentList_Data.get(params[0]).getUsername(), "UTF-8");
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateSend = format.format( CommentList_Data.get(params[0]).getDate());


                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/adoptServlet?"
                        + "mission=" + MissionNameSend
                        + "&publisher=" + MissionPublisherSend
                        + "&username=" + CommentPublisherSend
                        + "&date=" + DateSend
                        + "&gold=" + GoldSend);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);
                connection.connect();

                InputStream is = connection.getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                JSONObject jsonObject = new JSONObject(builder.toString());
                if (jsonObject.getString("Status") != null && jsonObject.getString("Status").equals("Success")) {
                    return params[0];
                } else {
                    return null;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(Integer c) {
            if (c != null) {
                CommentList_Data.get(c).setAdopt();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MissionDetailActivity.this, "采纳失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class sendComment extends AsyncTask<Comment, Void, String> {

        @Override
        protected String doInBackground(Comment... params) {
            Log.i(TAG, params[0].toPostParams() + " is ready to send");
            HttpURLConnection connection;

            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/sendComment");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                connection.setDoInput(true);

                Log.i(TAG, params[0].toPostParams());

                connection.getOutputStream().write(params[0].toPostParams().getBytes());
                connection.connect();

                Log.i(TAG, Integer.toString(connection.getResponseCode()));

                if (connection.getResponseCode() == 200) {
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null)
                        builder.append(line);

                    Log.i(TAG, "Response: " + builder.toString());
                    return builder.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MissionDetailActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);

            dialogView.findViewById(R.id.Dialog_Loading).setAnimation(AnimationUtils.loadAnimation(MissionDetailActivity.this, R.anim.roteting));
            ((TextView) dialogView.findViewById(R.id.Dialog_Text)).setText("正在发布评论...");

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
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String statusStr = jsonObject.getString("Status");
                    String dateStr = jsonObject.getString("Date");
                    if (statusStr != null && statusStr.equals("Success")) {
                        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        CommentList_Data.add(new Comment(missionPublisher, missionName, commentEdit.getText().toString(),
                                CurrentUser.getInstance().getUserName(), format.parse(dateStr)));

                        commentEdit.setText("");
                        Toast.makeText(MissionDetailActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        if (dialog != null)
                            dialog.cancel();
                        new getCommentAsyncTask().execute();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(MissionDetailActivity.this, "发布失败，请稍后再试", Toast.LENGTH_SHORT).show();
            if (dialog != null)
                dialog.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
    }
}
