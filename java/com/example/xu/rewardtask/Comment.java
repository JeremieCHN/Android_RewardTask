package com.example.xu.rewardtask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Comment {
    static String ADOPTED = "true";

    private String missionUsername, missionName, comment, username, isAdopted;
    private Date date;

    public Comment(String missionUsername, String missionName, String comment, String username, Date date) {
        this.username = username;
        this.missionName = missionName;
        this.comment = comment;
        this.missionName = missionUsername;
        this.date = date;
        isAdopted = "false";
    }

    public Comment(JSONObject jsonObject) throws JSONException {
        username = jsonObject.getString("Username");
        missionName = jsonObject.getString("Missionname");
        comment = jsonObject.getString("Comment");
        username =  jsonObject.getString("Commentor");
        date = new Date(jsonObject.getString("Date"));
        // TODO 和服务端校对
        isAdopted = "false";
    }

    public String getUsername() {
        return username;
    }

    public String getMissionName() {
        return missionName;
    }

    public String getComment() {
        return comment;
    }

    public String getMissionUsername() {
        return missionUsername;
    }

    public Date getDate() {
        return date;
    }

    public void setAdopt() {
        isAdopted = ADOPTED;
    }
}
