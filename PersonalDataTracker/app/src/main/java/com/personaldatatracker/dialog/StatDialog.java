package com.personaldatatracker.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.personaldatatracker.MainActivity;
import com.personaldatatracker.R;
import com.personaldatatracker.Utils;
import com.personaldatatracker.engine.Category;

public class StatDialog extends Dialog {
    MainActivity mainActivity;
    Category selCategory;

    public StatDialog(MainActivity mainActivity) {
        super(mainActivity);

        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_statistics);

        selCategory = (Category) mainActivity.mSelButton.getTag();

        // Count
        TextView textView = (TextView) findViewById(R.id.stat_count);
        textView.setText(String.valueOf(selCategory.getSize()));

        // Sum
        textView = (TextView) findViewById(R.id.stat_sum);
        textView.setText(String.format("%.02f %s", selCategory.getSumValue(), selCategory.getCategoryUnit()));

        // Max
        textView = (TextView) findViewById(R.id.stat_max);
        textView.setText(String.format("%.02f %s", selCategory.getMaxValue(), selCategory.getCategoryUnit()));

        // Min
        textView = (TextView) findViewById(R.id.stat_min);
        textView.setText(String.format("%.02f %s", selCategory.getMinValue(), selCategory.getCategoryUnit()));

        // Avg
        textView = (TextView) findViewById(R.id.stat_avg);
        textView.setText(String.format("%.02f %s", selCategory.getAvgValue(), selCategory.getCategoryUnit()));

        // From
        textView = (TextView) findViewById(R.id.stat_from);
        textView.setText(Utils.convertDateToString(selCategory.getFirstDate()));

        // To
        textView = (TextView) findViewById(R.id.stat_to);
        textView.setText(Utils.convertDateToString(selCategory.getLastDate()));

        // global avg check
        CheckBox checkBox = (CheckBox) findViewById(R.id.stat_check_global_avg);
        checkBox.setChecked(mainActivity.isGlobalAvgLineVisible);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.isGlobalAvgLineVisible = isChecked;
                mainActivity.showContentView();
            }
        });

        // 10 days avg check
        checkBox = (CheckBox) findViewById(R.id.stat_check_10day_avg);
        checkBox.setChecked(mainActivity.is10DaysAvgLineVisible);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivity.is10DaysAvgLineVisible = isChecked;
                mainActivity.showContentView();
            }
        });
    }
}
