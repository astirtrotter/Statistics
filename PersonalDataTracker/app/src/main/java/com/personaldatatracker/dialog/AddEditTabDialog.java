package com.personaldatatracker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.personaldatatracker.R;

public class AddEditTabDialog extends Dialog implements View.OnClickListener {

    private AddTabListener listener;
    private String categoryName;
    private String categoryUnit;

    public AddEditTabDialog(Context context, AddTabListener listener) {
        this(context, listener, "", "");
    }

    public AddEditTabDialog(Context context, AddTabListener listener, String categoryName, String categoryUnit) {
        super(context);
        setCancelable(false);

        this.listener = listener;
        this.categoryName = categoryName;
        this.categoryUnit = categoryUnit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_addedit_tab);

        findViewById(R.id.addedit_tab_ok).setOnClickListener(this);
        findViewById(R.id.addedit_tab_cancel).setOnClickListener(this);

        EditText categoryName = (EditText) findViewById(R.id.addedit_tab_title);
        EditText categoryUnit = (EditText) findViewById(R.id.addedit_tab_unit);

        categoryName.setText(this.categoryName);
        categoryUnit.setText(this.categoryUnit);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addedit_tab_ok) {
            String title = ((EditText) findViewById(R.id.addedit_tab_title)).getText().toString().trim();
            String unit = ((EditText) findViewById(R.id.addedit_tab_unit)).getText().toString().trim();
            if (title.isEmpty() || unit.isEmpty()) {
                Toast.makeText(getContext(), "Invalid input.", Toast.LENGTH_SHORT).show();
                return;
            }

            listener.onConfirm(title, unit);
        }

        dismiss();
    }

    public interface AddTabListener {
        void onConfirm(String title, String unit);
    }
}
