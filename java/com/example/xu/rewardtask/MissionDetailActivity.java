package com.example.xu.rewardtask;

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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MissionDetailActivity extends AppCompatActivity {
    // TODO 待测试

    private String TAG = "MissionDetailActivity";
    private String userName;
    private String missionName;
    private TextView userNameTimeTV;
    private TextView tip;
    private ListView commentLV;
    private EditText commentEdit;
    private TextView IsCompletedTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);

        initView();
        new getMissionDetailAsyncTask().execute();
    }

    void initView() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("UserName");
        missionName = intent.getStringExtra("MissionName");

        userNameTimeTV = (TextView) findViewById(R.id.MissionDetail_UserTime);
        tip = (TextView) findViewById(R.id.MissionDetail_Tip);
        IsCompletedTip = (TextView) findViewById(R.id.MissionDetail_IsCompleted);

        commentEdit = (EditText) findViewById(R.id.MissionDetail_CommentEdit);
        commentEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);
        commentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    new sendComment().execute(
                            new Comment(userName, missionName,
                                    commentEdit.getText().toString(),
                                    CurrentUser.getInstance().getUserName(),
                                    new Date(System.currentTimeMillis())));
                    return true;
                }
                return false;
            }
        });

        ((TextView) findViewById(R.id.MissionDetail_MissionName)).setText(missionName);
        userNameTimeTV.setText(userName);

        if (intent.getStringExtra("Date") != null) {
            Date date = new Date(intent.getStringExtra("Date"));
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm");
            userNameTimeTV.setText(userName + "发布于" + dateFormat.format(date));
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
                final Comment item = CommentList_Data.get(position);
                if (item.getUsername().equals(userName)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MissionDetailActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.activity_mission_detail_dialog, null);

                    Button adoptButton = (Button) dialogView.findViewById(R.id.MissionDetail_AdoptComment);
                    adoptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO 采纳操作，通知服务器
                            item.setAdopt();
                            adapter.notifyDataSetChanged();
                        }
                    });

                    Button replyButton = (Button) dialogView.findViewById(R.id.MissionDetail_ReplyComment);
                    replyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    builder.create().show();
                } else {
                    commentEdit.setText("回复 " + item.getUsername() + ": ");
                    commentEdit.requestFocus();
                }

                // TODO 评论点击事件
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
            DateFormat format = new SimpleDateFormat("yy-MM-dd hh-mm");
            viewHolder.commentDate.setText(format.format(list.get(i).getDate()));

            // TODO 采纳变色

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
                URL url = new URL("http://" + CurrentUser.IP + "/missionServlet?mission=" + missionName + "&publisher=" + userName);

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
                    JSONObject jsonObject = new JSONObject(s);

                    Date date = new Date(jsonObject.getString("Date"));
                    DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm");
                    userNameTimeTV.setText(userName + "发布于" + dateFormat.format(date));

                    TextView contentTV = (TextView) findViewById(R.id.MissionDetail_Content);
                    contentTV.setText(jsonObject.getString("Content"));

                    TextView moneyTV = (TextView) findViewById(R.id.MissionDetail_Money);
                    moneyTV.setText(jsonObject.getString("Gold"));

                    if (jsonObject.getString("IsComplete").equals(Mission.COMPLETED)) {
                        IsCompletedTip.setText("此任务的赏金已经被领取");
                    } else {
                        IsCompletedTip.setText("此任务的赏金尚未被领取");
                    }
                } catch (JSONException e) {
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
                URL url = new URL("http://" + CurrentUser.IP + "/getComment?mission=" + missionName + "&publisher=" + userName);

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

                JSONArray array = new JSONArray(buffer.toString());
                String statue = array.optJSONObject(0).getString("Statue");
                if (statue.equals("Success")) {

                    if (array.optJSONObject(0).getString("Commentor").equals(""))
                        return "NoComment";

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        CommentList_Data.add(new Comment(jsonObject));
                    }

                    adapter.notifyDataSetChanged();

                    return "Success";
                } else {
                    return "Fail";
                }
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
                Toast.makeText(MissionDetailActivity.this, "评论获取失败，请稍后再试", Toast.LENGTH_SHORT).show();
            } else if (s.equals("InternetGG")) {
                Toast.makeText(MissionDetailActivity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
            } else {
                tip.setVisibility(View.GONE);
                commentLV.setVisibility(View.VISIBLE);
            }
        }
    }

    class AdoptComment extends AsyncTask<Comment, Void, Comment> {

        @Override
        protected Comment doInBackground(Comment... params) {
            return params[0];
        }

        @Override
        protected void onPostExecute(Comment c) {
            c.setAdopt();
            adapter.notifyDataSetChanged();
        }
    }

    class sendComment extends AsyncTask<Comment, Void, String> {

        @Override
        protected String doInBackground(Comment... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, keyEvent);
        }
    }
}
