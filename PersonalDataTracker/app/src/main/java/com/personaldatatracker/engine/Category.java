package com.personaldatatracker.engine;

import com.opencsv.CSVWriter;
import com.personaldatatracker.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Category {

    private String categoryName;
    private String categoryUnit;
    private List<Date> axisX = new ArrayList<>();
    private List<Float> axisY = new ArrayList<>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryUnit() {
        return categoryUnit;
    }

    public void setCategoryUnit(String categoryUnit) {
        this.categoryUnit = categoryUnit;
    }

    public int getSize() {
        return axisX.size();
    }

    public Date getDate(int index) {
        if (index < 0 || index >= getSize())
            return null;

        return axisX.get(index);
    }

    public void setAxisX(List<Date> axisX) {
        this.axisX = axisX;
    }

    public void setAxisY(List<Float> axisY) {
        this.axisY = axisY;
    }

    public Float getValue(int index) {
        if (index < 0 || index >= getSize())
            return null;

        return axisY.get(index);
    }

    public Float getValueByDate(@NotNull Date date) {
        int index = axisX.indexOf(date);

        return getValue(index);
    }

    public void updateCategory(String newCategoryName, String newCategoryUnit) {
        CategoryGroup.dataTrackerDB.updateCategory(categoryName, newCategoryName, newCategoryUnit);

        categoryName = newCategoryName;
        categoryUnit = newCategoryUnit;
    }

    public void addCategory() {
        CategoryGroup.dataTrackerDB.addCategory(categoryName, categoryUnit);
    }

    public void removeCategory() {
        CategoryGroup.dataTrackerDB.removeCategory(categoryName);
    }

    public void removeContentItem(int position) {
        if (position < 0 || position >= getSize())
            return;

        CategoryGroup.dataTrackerDB.removeCategoryItem(categoryName, getDate(position));

        axisX.remove(position);
        axisY.remove(position);
    }

    private int isExistContentItem(Date date) {
        int year = Utils.getParamFromDate(date, Utils.Param.YEAR);
        int month = Utils.getParamFromDate(date, Utils.Param.MONTH);
        int day = Utils.getParamFromDate(date, Utils.Param.DAY);

        for (int i = 0; i < getSize(); i++) {
            Date date1 = getDate(i);

            int year1 = Utils.getParamFromDate(date1, Utils.Param.YEAR);
            int month1 = Utils.getParamFromDate(date1, Utils.Param.MONTH);
            int day1 = Utils.getParamFromDate(date1, Utils.Param.DAY);

            if (year == year1 && month == month1 && day == day1)
                return i;
        }

        return -1;
    }

    public boolean updateContent(int position, Date newDate, Float newValue) {
        int index = isExistContentItem(newDate);
        if (index != -1 && index != position)
            return false;

        // reorder & update
        CategoryGroup.dataTrackerDB.updateCategoryItem(categoryName, getDate(position), newDate, newValue);

        removeContentItem(position);
        addContentItem(newDate, newValue);

        return true;
    }

    public boolean addContentItem(Date date, Float value) {
        if (isExistContentItem(date) != -1)
            return false;

        // find position & add
        int index;
        for (index = 0; index < getSize(); index++) {
            if (getDate(index).getTime() > date.getTime())
                break;
        }

        CategoryGroup.dataTrackerDB.addContentItem(categoryName, date, value);

        axisX.add(index, date);
        axisY.add(index, value);

        return true;
    }

    private Float maxValue;
    private Float minValue;
    private Float avgValue;
    private Date lastDate;
    private Date firstDate;
    private Float sumValue;
    private Float[] list_of_avg_10_day;

    public void calcAdvancedStat() {
        sumValue = maxValue = minValue = avgValue = 0F;
        firstDate = getDate(0);
        lastDate = getDate(getSize() - 1);

        int count = getSize();
        if (count > 0)
            list_of_avg_10_day = new Float[count];
        float tmp = 0;

        for (int i = 0; i < count; i++) {
            Float value = getValue(i);

            sumValue += value;

            if (maxValue < value)
                maxValue = value;

            if (minValue == 0F || minValue > value)
                minValue = value;

            if (i >= 10)
                tmp -= getValue(i - 10);
            tmp += getValue(i);
            list_of_avg_10_day[i] = tmp / Math.min(i + 1, 10);
        }

        if (count > 0)
            avgValue = sumValue / getSize();
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getSumValue() {
        return sumValue;
    }

    public Float getAvgValue() {
        return avgValue;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public Date getFirstDate() {
        return firstDate;
    }

    public Float getAvg10Value(int index) {
        if (getValue(index) == null)
            return null;

        return list_of_avg_10_day[index];
    }

    public void exportCSV(CSVWriter writer) {
        writer.writeNext(new String[]{categoryName, "date", categoryUnit});
        for (int i = 0; i < getSize(); i ++) {
            String contents[] = {"",
                    Utils.convertDateToString(getDate(i)),
                    String.valueOf(getValue(i))};
            writer.writeNext(contents);
        }
    }
}
