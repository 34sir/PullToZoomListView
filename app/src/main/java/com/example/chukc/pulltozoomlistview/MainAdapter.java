package com.example.chukc.pulltozoomlistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chukc on 2016/5/13.
 */
public class MainAdapter extends BaseAdapter {
    private List<String> mList = new ArrayList<String>();
    private ViewHolder holder = null;
    private LayoutInflater inflater;
    public MainAdapter(Context context, List<String> mList){
        this.mList=mList;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.list_item, null);
        holder.text= (TextView) convertView.findViewById(R.id.text);
        holder.text.setText(mList.get(position)+position);
        return convertView;
    }

    public final class ViewHolder {
        public TextView text;
    }
}
