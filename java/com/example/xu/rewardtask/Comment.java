package com.example.xu.rewardtask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comment {
    static String ADOPTED = "true";

    private String missionUsername, missionName, comment, username, isAdopted;
    private Date date;

    public Comment(String missionUsername_, String missionName_, String comment_, String username_, Date date_) {
        this.username = username_;
        this.missionName = missionName_;
        this.comment = comment_;
        this.missionUsername = missionUsername_;
        this.date = date_;
        isAdopted = "false";
    }

    public Comment(JSONObject jsonObject) throws JSONException, ParseException {
        missionUsername = jsonObject.getString("Username");
        missionName = jsonObject.getString("Missionname");
        comment = jsonObject.getString("Comment");
        username = jsonObject.getString("Commentor");
        isAdopted = jsonObject.getString("IsAdopt");

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = format.parse(jsonObject.getString("Date").substring(0, 19));
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

    public boolean isAdopt() {
        return isAdopted.equals(ADOPTED);
    }

    public void setAdopt() {
        isAdopted = ADOPTED;
    }

    public String toPostParams() {
        return "username=" + missionUsername
                + "&mission=" + missionName
                + "&commentor=" + username
                + "&comment=" + comment;
    }
}
