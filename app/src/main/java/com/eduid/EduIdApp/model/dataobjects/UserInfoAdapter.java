package com.eduid.EduIdApp.model.dataobjects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eduid.EduIdApp.R;

import java.util.ArrayList;

public class UserInfoAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<InfoEntry> mDataSource;

    public UserInfoAdapter(Context context, ArrayList<InfoEntry> items){
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        //TODO: USER INFO DATA
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View entryView = mInflater.inflate(R.layout.user_info_entry, parent, false);

        TextView title = entryView.findViewById(R.id.titleText);
        TextView content = entryView.findViewById(R.id.contentText);

        InfoEntry entry = (InfoEntry) getItem(position);

        title.setText(entry.getTitleText());
        content.setText(entry.getContentText());

        return entryView;
    }
}
