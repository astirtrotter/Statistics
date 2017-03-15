package com.personaldatatracker;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

public class MotivationActivity extends AppCompatActivity {

    int gifNum;
    GifImageButton gib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No action bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        gib = new GifImageButton(this);
        gib.setScaleType(ImageView.ScaleType.FIT_CENTER);
        setContentView(gib);

        final int gifCount = 3;
        gifNum = new Random(System.currentTimeMillis()).nextInt(gifCount) + 1;

        displayGif();
    }

    private void displayGif() {
        try {
            GifDrawable drawable = new GifDrawable( getAssets(), String.format("gif/motivation%d.gif", gifNum));
            drawable.setLoopCount(0);
            gib.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(this, "Failed to load gif file. Try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
