package com.example.xu.rewardtask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class MissionAdapter extends BaseAdapter {
    private Context context;
    private List<MissionListItem> list;

    public MissionAdapter(Context context, List<MissionListItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;
        else
            return list.size();
    }

    @Override
    public Object getItem(int i) {
        if (list == null)
            return null;
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View convertView;
        ViewHolder viewHolder;
        if (view == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_area_mission_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.gold = (TextView) convertView.findViewById(R.id.AreaMissionListItem_MissionGold);
            viewHolder.date = (TextView) convertView.findViewById(R.id.AreaMissionListItem_MissionTime);
            viewHolder.title = (TextView) convertView.findViewById(R.id.AreaMissionListItem_MissionTitle);
            convertView.setTag(viewHolder);
        } else {
            convertView = view;
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //这里获取控件名字
        viewHolder.title.setText(list.get(i).missionName);
        viewHolder.gold.setText(Integer.toString(list.get(i).money));
        DateFormat format = new SimpleDateFormat("yy-MM-dd");
        viewHolder.date.setText(format.format(list.get(i).date));
        return convertView;
    }

    public class ViewHolder {
        //这里是控件的名字
        public TextView title;
        public TextView gold;
        public TextView date;
    }
}