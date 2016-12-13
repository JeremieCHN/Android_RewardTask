package com.example.xu.rewardtask;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class AreaMissionListActivity extends AppCompatActivity {

    private String TAG = "AreaMissionListActivity";
    private String city;
    private String type;
    private String order;
    private getMissionListAsyncTask getMissionListAsyncTask_;
    private boolean isRefreshing = false;

    private SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_mission_list);

        type = getIntent().getStringExtra("TypeName");
        order = null;
        city = CurrentUser.getInstance().getCity();
        getMissionListAsyncTask_ = new getMissionListAsyncTask();

        initButton();
        initSpinner();

        initSwipe();

        initOrder();
        bindAdapter();
        getMissionListAsyncTask_.execute();
    }

    void initButton() {
        Button button = (Button) findViewById(R.id.AreaMissionList_Release_NewMission_Button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentUser.getInstance().isLogin()) {
                    Intent intent = new Intent(AreaMissionListActivity.this, ReleaseNewMissionActivity.class);
                    intent.putExtra("City", city);
                    intent.putExtra("Type", type);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);
                } else {
                    Toast.makeText(AreaMissionListActivity.this, "您尚未登录，请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String[] cities;
    void initSpinner() {

        Spinner spinner = (Spinner) findViewById(R.id.AreaMissionList_Position_Spinner);

        cities = getResources().getStringArray(R.array.CityList);

        SpinnerAdapter adapter = new ArrayAdapter<String>(
                AreaMissionListActivity.this,
                R.layout.activity_area_mission_list_spinner_item,
                R.id.AreaMissionList_SpinnerText,
                cities);

        spinner.setAdapter(adapter);

        for (int i = 0; i < cities.length; i++) {
            if (cities[i].equals(city))
                spinner.setSelection(i);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = cities[position];
                if (!city.equals(CurrentUser.getInstance().getCity())) {
                    CurrentUser.getInstance().setCity(city);
                    if (isRefreshing) {
                        getMissionListAsyncTask_.cancel(true);
                    }
                    getMissionListAsyncTask_ = new getMissionListAsyncTask();
                    getMissionListAsyncTask_.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                city = CurrentUser.getInstance().getCity();
            }
        });
    }

    void initSwipe() {
        swipe = (SwipeRefreshLayout) findViewById(R.id.AreaMissionList_Swipe);
        swipe.setRefreshing(true);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    getMissionListAsyncTask_ = new getMissionListAsyncTask();
                    getMissionListAsyncTask_.execute();
                }
            }
        });
    }

    void initOrder() {
        Button timeOrder = (Button) findViewById(R.id.AreaMissionList_TimeOrder);
        Button moneyOrder = (Button) findViewById(R.id.AreaMissionList_MoneyOrder);

        timeList = (ListView) findViewById(R.id.AreaMissionList_MissionListByTime);
        moneyList = (ListView) findViewById(R.id.AreaMissionList_MissionListByMoney);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int windowWidth = displayMetrics.widthPixels;

        final View orderFlagAbove = findViewById(R.id.AreaMissionList_OrderFlagAbove);
        final View orderFlagBelow = findViewById(R.id.AreaMissionList_OrderFlagBelow);

        orderFlagAbove.getLayoutParams().width = windowWidth / 2;
        orderFlagBelow.getLayoutParams().width = windowWidth / 2;

        ((RelativeLayout.LayoutParams) orderFlagAbove.getLayoutParams()).setMarginStart(0);
        ((RelativeLayout.LayoutParams) orderFlagBelow.getLayoutParams()).setMarginStart(windowWidth / 2);

        timeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != null && order.equals("Money")) {
                    order = "Time";

                    timeList.setVisibility(View.VISIBLE);
                    moneyList.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) orderFlagAbove.getLayoutParams();
                    params.setMarginStart(0);
                    orderFlagAbove.setLayoutParams(params);

                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) orderFlagBelow.getLayoutParams();
                    params1.setMarginStart(windowWidth / 2);
                    orderFlagBelow.setLayoutParams(params1);
                }
            }
        });

        moneyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (order != null && order.equals("Time")) {
                    order = "Money";

                    Log.i(TAG, "Money");

                    moneyList.setVisibility(View.VISIBLE);
                    timeList.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) orderFlagAbove.getLayoutParams();
                    params.setMarginStart(windowWidth / 2);
                    orderFlagAbove.setLayoutParams(params);

                    RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) orderFlagBelow.getLayoutParams();
                    params1.setMarginStart(0);
                    orderFlagBelow.setLayoutParams(params1);

                }
            }
        });
    }

    // 绑定数据
    private MissionAdapter timeAdapter = null;
    private MissionAdapter moneyAdapter = null;

    private ListView timeList = null;
    private ListView moneyList = null;

    private List<MissionListItem> missionList_data = null;
    private List<MissionListItem> timeList_data = null;
    private List<MissionListItem> moneyList_data = null;

    private void bindAdapter() {
        if (missionList_data == null)
            missionList_data = new LinkedList<>();

        if (timeList_data == null)
            timeList_data = new LinkedList<>();

        if (moneyList_data == null)
            moneyList_data = new LinkedList<>();

        timeAdapter = new MissionAdapter(AreaMissionListActivity.this, timeList_data);
        timeList.setAdapter(timeAdapter);
        timeList.setOnItemClickListener(new itemClick(timeList_data));

        moneyAdapter = new MissionAdapter(AreaMissionListActivity.this, moneyList_data);
        moneyList.setAdapter(moneyAdapter);
        moneyList.setOnItemClickListener(new itemClick(moneyList_data));
    }

    private class itemClick implements AdapterView.OnItemClickListener {
        private List<MissionListItem> l;

        itemClick(List<MissionListItem> l_) {
            l = l_;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MissionListItem item = l.get(position);
            Intent intent = new Intent(AreaMissionListActivity.this, MissionDetailActivity.class);
            intent.putExtra("MissionName", item.missionName);
            intent.putExtra("UserName", item.userName);
            intent.putExtra("Date", item.date.toString());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide2left);
        }
    }

    private class getMissionListAsyncTask extends AsyncTask<Void, Void, String> {
        TextView tip = (TextView) findViewById(R.id.AreaMissionList_Tip);
        private HttpURLConnection httpURLConnection = null;

        @Override
        protected String doInBackground(Void... params) {

            Log.i(TAG, city);
            if (isCancelled()) return null;

            String cityParams = city;
            if (city.equals("全国"))
                cityParams = "all";


            // TODO 去掉因为debug加上去的东西
            try {
                URL url = new URL("http://" + CurrentUser.IP + "/AndroidServer/menuServlet?city=" +
                        URLEncoder.encode(cityParams,"UTF-8") + "&type=" +
                        URLEncoder.encode(type, "UTF-8"));

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setReadTimeout(4000);
                httpURLConnection.setReadTimeout(4000);
                httpURLConnection.connect();

                if (isCancelled()) return null;
                Log.i(TAG, Integer.toString(httpURLConnection.getResponseCode()));

                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                if (jsonArray.optJSONObject(0).getString("Status").equals("Empty"))
                    return "ListIsNull";

                // 解析获取到的列表
                for (int i = 0; i < jsonArray.length(); i++) {
                    missionList_data.clear();

                    JSONObject jsonObject = jsonArray.optJSONObject(i);

                    MissionListItem missionListItem = new MissionListItem(jsonObject);

                    missionList_data.add(missionListItem);
                }

                Log.i(TAG, timeList_data.toString());
                timeList_data.clear();
                moneyList_data.clear();
                Log.i(TAG, timeList_data.toString());

                for (MissionListItem mli : missionList_data) {
                    timeList_data.add(mli);
                    moneyList_data.add(mli);
                }

                Log.i(TAG, timeList_data.toString());
                Collections.sort(timeList_data, new Comparator<MissionListItem>() {
                    @Override
                    public int compare(MissionListItem o1, MissionListItem o2) {
                        if (o1.date.after(o2.date))
                            return 1;
                        else return 0;
                    }
                });

                Collections.sort(moneyList_data, new Comparator<MissionListItem>() {
                    @Override
                    public int compare(MissionListItem o1, MissionListItem o2) {
                        if (o1.date.after(o2.date))
                            return 1;
                        else return 0;
                    }
                });

                return "Success";
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
                return "Fail";
            } catch (JSONException e) {
                e.printStackTrace();
                return "Fail";
            } catch (ParseException e) {
                e.printStackTrace();
                return "Fail";
            } finally {
                if (httpURLConnection != null)
                    httpURLConnection.disconnect();
            }
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "即将开始请求");
            tip.setText(R.string.AreaMissionList_GettingList);
            swipe.setRefreshing(true);
            isRefreshing = true;
        }

        @Override
        protected void onPostExecute(String result) {
            if (swipe.isRefreshing())
                swipe.setRefreshing(false);

            if (result.equals("InternetGG") || result.equals("Fail")) {
                tip.setText("获取列表失败，下拉重试");
            } else if (result.equals("ListIsNull")) {
                tip.setText("此地区下暂无任务，请浏览其他地区或类别");
            } else {
                tip.setVisibility(View.GONE);

                timeAdapter.notifyDataSetChanged();
                moneyAdapter.notifyDataSetChanged();

                timeList.setVisibility(View.VISIBLE);
                order = "Time";
            }
            isRefreshing = false;
        }

        @Override
        protected void onCancelled(String s) {
            Log.i(TAG, "取消任务");
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        if (isRefreshing && getMissionListAsyncTask_ != null)
            getMissionListAsyncTask_.cancel(true);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide2right);
    }
    
    @Override
    public void onRestart() {
        super.onRestart();
        getMissionListAsyncTask_ = new getMissionListAsyncTask();
        getMissionListAsyncTask_.execute();
    }
}
