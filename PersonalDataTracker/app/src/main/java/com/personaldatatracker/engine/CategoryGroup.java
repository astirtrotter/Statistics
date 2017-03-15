package com.personaldatatracker.engine;

import android.content.Context;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.personaldatatracker.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryGroup {
    public static DataTrackerDB dataTrackerDB = null;

    private List<Category> categoryList = null;

    public CategoryGroup(Context context) {
        dataTrackerDB = new DataTrackerDB(context);
//        dataTrackerDB.clearDataBase();
        categoryList = dataTrackerDB.loadData();
    }

    public int getSize() {
        return categoryList.size();
    }

    public Category getCategory(int index) {
        if (index < 0 || index >= getSize())
            return null;

        return categoryList.get(index);
    }

    public void addCategory(Category category) {
        categoryList.add(category);

        category.addCategory();
    }

    public void removeAllCategories() {
        for (int size = getSize() - 1; size >= 0; size--)
            removeCategory(size);
    }

    public void removeCategory(int index) {
        removeCategory(getCategory(index));
    }

    public void removeCategory(Category category) {
        assert category != null;

        category.removeCategory();
        categoryList.remove(category);
    }

    public void updateCategory(String oldCategoryName, String newCategoryName, String newCategoryUnit) {

        for (Category category: categoryList) {
            if (category.getCategoryName().equals(oldCategoryName)) {
                category.updateCategory(newCategoryName, newCategoryUnit);
                break;
            }
        }
    }

    public boolean isExistCategory(String categoryName) {
        for (int i = 0; i < getSize(); i++)
            if (getCategory(i).getCategoryName().equals(categoryName))
                return true;

        return false;
    }

    public boolean exportCSV(File file) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file), ',', '"', '\n');

            for (int i = 0; i < getSize(); i ++)
                getCategory(i).exportCSV(writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean importCSV(File file) {
        String next[];
        List<String[]> list = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        List<Date> dates = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            while(true) {
                next = reader.readNext();
                if(next != null) {
                    list.add(next);

                    // check syntax
                    if (next.length != 3)
                        return false;
                    if (!next[0].isEmpty()) {
                        // category Name
                        // duplicated
                        if (categoryNames.contains(next[0]))
                            return false;

                        categoryNames.add(next[0]);
                        dates.clear();
                    } else {
                        // not belonging category
                        if (categoryNames.isEmpty())
                            return false;

                        // check date format
                        Date date = Utils.convertStringToDate(next[1]);
                        if (date == null)
                            return false;
                        // duplicated
                        if (dates.contains(date))
                            return false;
                        dates.add(date);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        removeAllCategories();
        Category lastCategory = null;
        for (String[] elements : list) {
            if (!elements[0].isEmpty()) {
                lastCategory = new Category();
                lastCategory.setCategoryName(elements[0]);
                lastCategory.setCategoryUnit(elements[2]);
                addCategory(lastCategory);
            } else {
                Date date = Utils.convertStringToDate(elements[1]);
                Float value = Float.parseFloat(elements[2]);

                assert lastCategory != null;
                lastCategory.addContentItem(date, value);
            }
        }

        return true;
    }
}
