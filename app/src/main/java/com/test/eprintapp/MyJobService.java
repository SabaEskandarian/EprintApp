package com.test.eprintapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import static android.app.job.JobInfo.getMinPeriodMillis;

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        new Thread(new Runnable() {
            public void run() {
                //Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MyJobService.this);
                String[] searchTerms = sharedPref.getString("notification_topics", "").toLowerCase().split(" ");
                boolean notificationsOn = sharedPref.getBoolean("notifications_switch", false);
                boolean citationSearchOn = sharedPref.getBoolean("citation_checkbox", false);
                String[] lastName = sharedPref.getString("last_name", "").split(" ");
                if(notificationsOn){
                    try {
                        Document doc = Jsoup.connect("https://eprint.iacr.org/eprint-bin/search.pl?last=1&title=1").get();
                        List<String> links = doc.select("a[href]").eachText();
                        List<String> titles = doc.select("b").eachText();
                        List<String> authors = doc.select("em").eachText();
                        for(int i = 0; i < titles.size(); i++){
                            for(String s : searchTerms){
                                if(titles.get(i).toLowerCase().contains(s) || authors.get(i).toLowerCase().contains(s)){
                                    // notificationId is a unique int for each notification that you must define
                                    MyJobService.this.notify(MyJobService.this, "New paper: "+links.get(2*i), titles.get(i)+"\n"+authors.get(i), i);
                                }
                            }
                            if(citationSearchOn){
                                String url = "https://eprint.iacr.org/"+links.get(2*i)+".pdf";
                                //search in text and update notification if citation found
                                for(String s : lastName){
                                    if(ReadPdfFile(url, s)){
                                        // notificationId is a unique int for each notification that you must define
                                        MyJobService.this.notify(MyJobService.this, "New paper cites you: "+links.get(2*i), titles.get(i)+"\n"+authors.get(i), i);
                                    }
                                }
                            }
                        }
                        jobFinished(params, false);
                    } catch (IOException e) {
                        MyJobService.this.notify(MyJobService.this, "Exception! ", "something went wrong.",100);
                        e.printStackTrace();
                        jobFinished(params, false);
                    }
                }
            }
        }).start();
        return true;
    }

    //modified from an online forum
    public  boolean ReadPdfFile(String url, String searchText) throws IOException
    {
        PdfReader pdfReader = new PdfReader(url);
        int n = pdfReader.getNumberOfPages();
        for (int page = 1; page <= n; page++)
        {
            TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();

            String currentPageText = PdfTextExtractor.getTextFromPage(pdfReader, page, strategy);
            if (currentPageText.contains(searchText))
            {
                return true;
            }
        }
        pdfReader.close();
        return false;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        return true;
    }

    public void notify(Context context,
                       final String firstLine, String bodyText, int number) {


        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_launcher_background);


        final String ticker = firstLine;
        final String title = firstLine;
        final String text = bodyText;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "eprint channel";
            String description = "for eprint app";
            NotificationChannel channel = new NotificationChannel("default", name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_paper)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(ticker)
                .setChannelId("default")



                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                0))

                // Show expanded text content on devices running Android 4.1 or
                // later.
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(number, builder.build());
    }

}
