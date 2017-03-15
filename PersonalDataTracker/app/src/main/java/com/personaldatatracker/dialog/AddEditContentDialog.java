package com.personaldatatracker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.personaldatatracker.R;
import com.personaldatatracker.Utils;

import java.util.Date;

public class AddEditContentDialog extends Dialog implements View.OnClickListener {

    private AddEditListener listener;
    private Date date;
    private Float value;

    public AddEditContentDialog(Context context, AddEditListener listener) {
        this(context, listener, new Date(), 0F);
    }

    public AddEditContentDialog(Context context, AddEditListener listener, Date initDate, Float initValue) {
        super(context);
        setCancelable(false);

        this.listener = listener;
        this.date = initDate;
        this.value = initValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_addedit_content);

        findViewById(R.id.content_ok).setOnClickListener(this);
        findViewById(R.id.content_cancel).setOnClickListener(this);

        DatePicker datePicker = (DatePicker) findViewById(R.id.content_date);
        final EditText editValue = (EditText) findViewById(R.id.content_value);

        datePicker.init(Utils.getParamFromDate(date, Utils.Param.YEAR),
                        Utils.getParamFromDate(date, Utils.Param.MONTH) - 1,
                        Utils.getParamFromDate(date, Utils.Param.DAY),
                        null);
        editValue.setText(String.format("%d", value.intValue()));

        editValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

                    String valueString = editValue.getText().toString();
                    if (valueString.equals("0"))
                        editValue.selectAll();
                    else
                        editValue.setSelection(valueString.length());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.content_ok) {
            String valueStr = ((EditText) findViewById(R.id.content_value)).getText().toString().trim();
            if (valueStr.isEmpty()) {
                Toast.makeText(getContext(), "Invalid input.", Toast.LENGTH_SHORT).show();
                return;
            }

            value = Float.parseFloat(valueStr);
            DatePicker datePicker = (DatePicker) findViewById(R.id.content_date);
            date = Utils.convertStringToDate(String.format("%04d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));

            if (listener != null) {
                listener.onConfirm(date, value);
            }
        }
        dismiss();
    }

    public interface AddEditListener {
        void onConfirm(Date date, Float value);
    }

}
