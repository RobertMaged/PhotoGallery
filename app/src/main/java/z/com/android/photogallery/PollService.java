package z.com.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    private static final String TAG = "PollService";
    //set Interval to 1 min
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);

    public static final String ACTION_SHOW_NOTIFICATION = "z.com.android.photogallery.SHOW_NOTIFICATION";
    //make our broadcast private for the app only
    public static final String PREM_PRIVATE = "z.com.android.photogallery.PRIVATE";

    public static Intent newIntent(Context context){
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn){

        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        //write to sharedPref. Alarm state (isOn)
        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent intent = PollService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        return pendingIntent != null;
    }

    Handler mHandler;
    public PollService() {
        super(TAG);
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null){
            items = new FlickrFetcher().fetchRecentPhotos();
        } else {
            items = new FlickrFetcher().searchPhotos(query);
        }

        if (items.size() == 0){
            return;
        }

        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)){
            Log.i(TAG, "Got an old result: " + resultId);
            mHandler.post( ()-> Toast.makeText(this, "Alarm is on but Got an old result", Toast.LENGTH_LONG).show());
        } else {
            Log.i(TAG, "Got a new result: " + resultId);
            mHandler.post( ()-> Toast.makeText(this, "Alarm is on, Got a new result", Toast.LENGTH_LONG).show());
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("info", "test", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("description");

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }
                 NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "info")
                        .setTicker(getResources().getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(getResources().getString(R.string.new_pictures_title))
                        .setContentText(getResources().getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true);


                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(0,notification.build());

            //now send broadcat every time new search result are available
            //the receiver who only use our(PREM_PRIVATE) permission can receive it
            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PREM_PRIVATE);
        }

        QueryPreferences.setLastResultId(this, resultId);
    }


    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //check if is it available to use network (if background data is disabled)
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        Log.d(TAG, "isNetworkAvailableAndConnected() returned: " + isNetworkConnected);

        return isNetworkConnected;
    }
}
