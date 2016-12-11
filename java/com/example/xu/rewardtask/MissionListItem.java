package com.example.xu.rewardtask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MissionListItem {
    public String userName;
    public String missionName;
    public int money;
    public Date date;

    public MissionListItem(String userName_, String missionName_, int money_, Date date_) {
        userName = userName_;
        missionName = missionName_;
        money = money_;
        date = date_;
    }

    public MissionListItem(JSONObject jsonObject) throws JSONException {
        userName = jsonObject.getString("UserName");
        missionName = jsonObject.getString("MissionName");
        money = Integer.valueOf(jsonObject.getString("Money"));
        date = new Date(jsonObject.getString("Date"));
    }

    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("UserName", userName);
            jsonObject.put("MissionName", missionName);
            jsonObject.put("Money", money);
            jsonObject.put("Date", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
