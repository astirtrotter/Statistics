package com.personaldatatracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.github.developerpaul123.filepickerlibrary.enums.MimeType;
import com.github.developerpaul123.filepickerlibrary.enums.Request;
import com.github.developerpaul123.filepickerlibrary.enums.Scope;
import com.personaldatatracker.dialog.StatDialog;
import com.personaldatatracker.engine.Category;
import com.personaldatatracker.dialog.AddEditContentDialog;
import com.personaldatatracker.dialog.AddEditTabDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ComboLineColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;

public class MainActivity extends AppCompatActivity /*implements FileDialog.OnFileSelectedListener*/ {

    private static final int OPEN_FILE_RESULT_CODE = 1000;
    private static final int SAVE_FILE_RESULT_CODE = 1001;

    LinearLayout mTabs;
    HorizontalScrollView scrollView;
    public Button mSelButton = null;           //having selected Category as a tag
    public boolean isGlobalAvgLineVisible = true;
    public boolean is10DaysAvgLineVisible = true;

    boolean isContentTableView;
    SwipeMenuListView listView;
    LinearLayout graphViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabs = (LinearLayout) findViewById(R.id.tabs);
        assert mTabs != null;
        scrollView = (HorizontalScrollView) mTabs.getParent();

        int size = GlobalConstants.categoryGroup.getSize();
        for (int i = 0; i < size; i ++) {
            Category category = GlobalConstants.categoryGroup.getCategory(i);
            Button button = addCategoryButton(category);

            if (mSelButton == null)
                mSelButton = button;
        }

        listView = (SwipeMenuListView) findViewById(R.id.tableView);
        configureListView();

        graphViews = (LinearLayout) findViewById(R.id.graphViews);

        isContentTableView = true;
        showContentView();
    }

    private void configureListView() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem editItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                editItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0xC9, 0xCE)));
                // set item width
                editItem.setWidth(Utils.dp2px(MainActivity.this, 90));
                // set a icon
                editItem.setIcon(R.drawable.ic_edit);
                // add to menu
                menu.addMenuItem(editItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(Utils.dp2px(MainActivity.this, 90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                if (index == 0) {
                    // edit
                    editContent(position);
                } else if (index == 1) {
                    // delete
                    Category category = (Category) mSelButton.getTag();
                    category.removeContentItem(position);

                    // update content by adapter
                    showContentView();
                }
                return false;
            }
        });

        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editContent(position);
            }
        });
    }

    private void editContent(final int position) {
        final Category category = (Category) mSelButton.getTag();
        Date date = category.getDate(position);
        Float value = category.getValue(position);

        AddEditContentDialog dialog = new AddEditContentDialog(MainActivity.this, new AddEditContentDialog.AddEditListener() {
            @Override
            public void onConfirm(Date date, Float value) {
                if (!category.updateContent(position, date, value))
                    Toast.makeText(getApplicationContext(), "Failed to update content.", Toast.LENGTH_SHORT).show();
                else {
                    // update content by adapter
                    showContentView();
                }

            }
        }, date, value);

        dialog.show();
    }

    public void onAddTab(View view) {
        AddEditTabDialog addEditTabDialog = new AddEditTabDialog(this, new AddEditTabDialog.AddTabListener() {
            @Override
            public void onConfirm(String title, String unit) {
                if (GlobalConstants.categoryGroup.isExistCategory(title)) {
                    Toast.makeText(getApplicationContext(), "Already exist!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Category category = new Category();
                category.setCategoryName(title);
                category.setCategoryUnit(unit);

                GlobalConstants.categoryGroup.addCategory(category);
                mSelButton = addCategoryButton(category);
                scrollView.fullScroll(View.FOCUS_RIGHT);

                showContentView();
            }
        });

        addEditTabDialog.show();
    }

    public void onDelTab(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        GlobalConstants.categoryGroup.removeCategory((Category) mSelButton.getTag());
                        mTabs.removeView(mSelButton);

                        if (GlobalConstants.categoryGroup.getSize() > 0) {
                            mSelButton = (Button) mTabs.getChildAt(0);
                            scrollView.fullScroll(View.FOCUS_LEFT);
                        } else
                            mSelButton = null;

                        showContentView();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private Button addCategoryButton(final Category category) {
        final Button button = new Button(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText(category.getCategoryName());
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.background_unsel);
        button.setTag(category);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelButton.equals(button)) {
                    AddEditTabDialog dialog = new AddEditTabDialog(MainActivity.this, new AddEditTabDialog.AddTabListener() {
                        @Override
                        public void onConfirm(String title, String unit) {
                            GlobalConstants.categoryGroup.updateCategory(category.getCategoryName(), title, unit);
                            button.setText(title);
                            showContentView();
                        }
                    }, category.getCategoryName(), category.getCategoryUnit());
                    dialog.show();
                } else {
                    mSelButton = button;
                    showContentView();
                }
            }
        });

        mTabs.addView(button);

        return button;
    }

    public void onContentTypeChanged(View view) {
        RadioButton tableRadioButton = (RadioButton) findViewById(R.id.content_table);
        assert tableRadioButton != null;
        isContentTableView = tableRadioButton.isChecked();

        showContentView();
    }

    public void showContentView() {
        if (mSelButton == null) {
            findViewById(R.id.content).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.content).setVisibility(View.VISIBLE);

        for (int i = 0; i < mTabs.getChildCount(); i++) {
            Button button = (Button) mTabs.getChildAt(i);
            button.setBackgroundResource(R.drawable.background_unsel);
            button.setAlpha(0.3f);
        }

        mSelButton.setBackgroundResource(R.drawable.background_sel);
        mSelButton.setAlpha(1);

        Category selCategory = (Category) mSelButton.getTag();

        listView.setVisibility(isContentTableView ? View.VISIBLE : View.GONE);
        graphViews.setVisibility(isContentTableView ? View.GONE : View.VISIBLE);

        selCategory.calcAdvancedStat();

        if (isContentTableView) {
            ContentListViewAdapter adapter = new ContentListViewAdapter(this, selCategory);
            listView.setAdapter(adapter);
        } else {
            int count = selCategory.getSize();
            if (count > 0) {
                selCategory.calcAdvancedStat();

                ComboLineColumnChartView chart;
                PreviewColumnChartView previewChart;
                ComboLineColumnChartData data;
                /**
                 * Deep copy of data.
                 */
                ColumnChartData previewData;

                chart = (ComboLineColumnChartView) findViewById(R.id.chart);
                previewChart = (PreviewColumnChartView) findViewById(R.id.chart_preview);

                List<Column> columns = new ArrayList<>();
                List<Column> columnsForScale = new ArrayList<>();
                List<SubcolumnValue> values;
                List<AxisValue> axisValuesTop = new ArrayList<>();
                List<AxisValue> axisValuesBottom = new ArrayList<>();
                List<Line> lines = new ArrayList<>();
                List<PointValue> pointValues = new ArrayList<>();
                for (int i = 0; i < count; ++i) {

                    values = new ArrayList<>();
                    Float value = selCategory.getValue(i);
                    values.add(new SubcolumnValue(value, value >= selCategory.getAvgValue() ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_VIOLET));
                    axisValuesTop.add(new AxisValue(i).setLabel(Utils.convertDateToString(selCategory.getDate(i), "MMM d")));
                    axisValuesBottom.add(new AxisValue(i).setLabel(Utils.convertDateToString(selCategory.getDate(i), "yyyy-MM")));

                    columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
                    pointValues.add(new PointValue(i, selCategory.getAvg10Value(i)));
                }

                Line line;
                // 10 days avg lines
                if (is10DaysAvgLineVisible) {
                    line = new Line(pointValues);
                    line.setColor(ChartUtils.COLOR_BLUE / 2);
                    line.setCubic(true);
                    line.setHasLabelsOnlyForSelected(true);
                    line.setHasLines(true);
                    line.setHasPoints(true);
                    lines.add(line);
                }

                // global avg line
                if (isGlobalAvgLineVisible) {
                    pointValues = new ArrayList<>();
                    pointValues.add(new PointValue(0, selCategory.getAvgValue()));
                    pointValues.add(new PointValue(count - 1, selCategory.getAvgValue()));
                    line = new Line(pointValues);
                    line.setColor(ChartUtils.COLOR_BLUE);
                    line.setHasPoints(false);
                    line.setStrokeWidth(1);
                    lines.add(line);
                }

                ColumnChartData columnChartData = new ColumnChartData(columns);
                LineChartData lineChartData = new LineChartData(lines);

                data = new ComboLineColumnChartData(columnChartData, lineChartData);
                data.setAxisXBottom(new Axis(axisValuesTop));
                data.setAxisYLeft(new Axis().setHasLines(true).setName(selCategory.getCategoryUnit()));
                columnChartData.setAxisXBottom(new Axis(axisValuesBottom));
                columnChartData.setAxisYLeft(data.getAxisYLeft());

                // prepare preview data, is better to use separate deep copy for preview chart.
                // set color to grey to make preview area more visible.
                previewData = new ColumnChartData(columnChartData);
                for (Column column : previewData.getColumns()) {
                    for (SubcolumnValue value : column.getValues()) {
                        value.setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
                    }
                }

                assert chart != null;
                chart.setComboLineColumnChartData(data);
                chart.setValueSelectionEnabled(true);
                chart.setOnValueTouchListener(new ValueTouchListener());
                // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
                // zoom/scroll is unnecessary.
                chart.setZoomEnabled(false);
                chart.setScrollEnabled(false);

                assert previewChart != null;
                previewChart.setColumnChartData(previewData);
                previewChart.setViewportChangeListener(new ViewportListener());

                Viewport tempViewport = new Viewport(chart.getMaximumViewport());
                float dx = tempViewport.width() / 4;
                tempViewport.inset(dx, 0);
                previewChart.setCurrentViewport(tempViewport);
                previewChart.setZoomType(ZoomType.HORIZONTAL);
                previewChart.setPreviewColor(ChartUtils.COLOR_ORANGE);
            } else
                graphViews.setVisibility(View.GONE);
        }

    }

    public void onAddContent(View view) {
        AddEditContentDialog dialog = new AddEditContentDialog(this, new AddEditContentDialog.AddEditListener() {
            @Override
            public void onConfirm(Date date, Float value) {
                Category category = (Category) mSelButton.getTag();

                if (category.addContentItem(date, value)) {
                    showContentView();
                } else {
                    Toast.makeText(getApplicationContext(), "Alreay exist!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    public void onStat(View view) {
        Category selCategory = (Category) mSelButton.getTag();

        if (selCategory.getSize() == 0) {
            Toast.makeText(this, "No data to collect.", Toast.LENGTH_SHORT).show();
            return;
        }

        StatDialog statDialog = new StatDialog(this);

        statDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.menu_item_restore &&
                item.getItemId() != R.id.menu_item_motivate &&
                (mSelButton == null ||
                ((Category) mSelButton.getTag()).getSize() == 0)) {
            Toast.makeText(this, "No data!", Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (isContentTableView)
                    Toast.makeText(this, "Switch to graph view mode and try again.", Toast.LENGTH_SHORT).show();
                else {
                    Bitmap bitmap = getBitmapFromView(graphViews);
                    Uri bmpUri;
                    try {
                        // Use methods on Context to access package-specific directories on external storage.
                        // This way, you don't need to request external read/write permission.
                        // See https://youtu.be/5xVh-7ywKpE?t=25m25s
                        File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();
                        bmpUri = Uri.fromFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return true;
                    }

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);

                    Intent.createChooser(shareIntent, "share");
                    startActivity(shareIntent);
                }
                return true;

            case R.id.menu_item_backup:
                Intent filePickerToBackup = new Intent(this, FilePickerActivity.class);
                filePickerToBackup.putExtra(FilePickerActivity.SCOPE, Scope.ALL);
                filePickerToBackup.putExtra(FilePickerActivity.REQUEST, Request.DIRECTORY);
                filePickerToBackup.putExtra(FilePickerActivity.INTENT_EXTRA_COLOR_ID, android.R.color.holo_blue_dark);
//                filePickerToBackup.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
                startActivityForResult(filePickerToBackup, SAVE_FILE_RESULT_CODE);

                return true;

            case R.id.menu_item_restore:
                Intent filePickerToRestore = new Intent(this, FilePickerActivity.class);
                filePickerToRestore.putExtra(FilePickerActivity.SCOPE, Scope.ALL);
                filePickerToRestore.putExtra(FilePickerActivity.REQUEST, Request.FILE);
                filePickerToRestore.putExtra(FilePickerActivity.INTENT_EXTRA_COLOR_ID, android.R.color.holo_green_dark);
                filePickerToRestore.putExtra(FilePickerActivity.MIME_TYPE, MimeType.TXT);

//                filePickerToRestore.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
                startActivityForResult(filePickerToRestore, OPEN_FILE_RESULT_CODE);
                return true;

            case R.id.menu_item_motivate:
                startActivity(new Intent(MainActivity.this, MotivationActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);

        return returnedBitmap;
    }

    public void backup(String filePathName) {
        Toast.makeText(this, filePathName, Toast.LENGTH_SHORT).show();
    }

    public void restore(String filePathName) {
        Toast.makeText(this, filePathName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == SAVE_FILE_RESULT_CODE) && (resultCode == RESULT_OK)) {
            String dirPath = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            String fileName = getResources().getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".txt";
            File file = new File(dirPath, fileName);
            try {
                file.createNewFile();
                if (GlobalConstants.categoryGroup.exportCSV(file))
                    Toast.makeText(this, "Successfully exported.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Failed to export. Try again!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if ((requestCode == OPEN_FILE_RESULT_CODE) && (resultCode == RESULT_OK)) {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:

                            String filePathName = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
                            File file = new File(filePathName);

                            if (GlobalConstants.categoryGroup.importCSV(file)) {
                                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                                finish();
                                Toast.makeText(getApplicationContext(), "Successfully imported. App is being restarted.", Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Invalid backup file. Try again!", Toast.LENGTH_SHORT).show();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Current data will be erased.\nDo you really want to continue?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart because usually viewport changes
            // happens to often.
            ComboLineColumnChartView chart;
            chart = (ComboLineColumnChartView) findViewById(R.id.chart);
            assert chart != null;
            chart.setCurrentViewport(newViewport);
        }
    }

    private class ValueTouchListener implements ComboLineColumnChartOnValueSelectListener {

        @Override
        public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            Category selCategory = (Category) mSelButton.getTag();
            String msg = String.format("Value(%s) : %.02f %s", Utils.convertDateToString(selCategory.getDate(columnIndex)), value.getValue(), selCategory.getCategoryUnit());
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Category selCategory = (Category) mSelButton.getTag();
            String msg = String.format("Avg of last 10 days(%s) : %.02f %s", Utils.convertDateToString(selCategory.getDate(pointIndex)), value.getY(), selCategory.getCategoryUnit());
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }
    }
}
