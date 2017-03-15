package com.personaldatatracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.personaldatatracker.engine.Category;

public class ContentListViewAdapter extends BaseAdapter {
    private Context context;
    private Category category;
    private LayoutInflater inflater;

    public ContentListViewAdapter(Context context, Category category) {
        this.context = context;
        this.category = category;
    }

    @Override
    public int getCount() {
        return category.getSize();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        TextView textNo = (TextView) convertView.findViewById(R.id.list_no);
        TextView textDate = (TextView) convertView.findViewById(R.id.list_date);
        TextView textVal = (TextView) convertView.findViewById(R.id.list_val);

        textNo.setText(String.valueOf(position + 1));
        textDate.setText(Utils.convertDateToString(category.getDate(position)));
        textVal.setText(Utils.convertValueToString(category.getValue(position), category.getCategoryUnit()));

        return convertView;
    }
}
