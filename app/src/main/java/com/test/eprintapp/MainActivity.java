package com.test.eprintapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int duration = Integer.parseInt(sharedPref.getString("how_many_papers", "1"));
        String url = "";
        switch(duration){
            case 0: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=2&title=1"; break;
            case 1: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=7&title=1"; break;
            case 2: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=31&title=1"; break;
            case 3: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=183&title=1"; break;
            case 4: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=365&title=1"; break;
            default: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=7&title=1"; break;
        }

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl(url);

        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getAllPendingJobs().size() == 0){
            int res = jobScheduler.schedule(new JobInfo.Builder(1,
                    new ComponentName(this, MyJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(86400000)
                    .setPersisted(true)
                    //.setRequiresDeviceIdle(true)//didn't seem to be easily satisfiable in testing
                    .build());
        }
    }



    /** Called when the user taps the Send button */
    public void prefButton(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void refButton(View view) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int duration = Integer.parseInt(sharedPref.getString("how_many_papers", "1"));
        //int duration = 1; //if(sharedPref.contains("how_many_papers")) duration = 3;
        String url = "";
        switch(duration){
            case 0: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=2&title=1"; break;
            case 1: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=7&title=1"; break;
            case 2: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=31&title=1"; break;
            case 3: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=183&title=1"; break;
            case 4: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=365&title=1"; break;
            default: url = "https://eprint.iacr.org/eprint-bin/search.pl?last=7&title=1"; break;
        }

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl(url);
    }
}
