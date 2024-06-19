package com.example.term_project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GPS_Service extends Service {

    private static final String CHANNEL_ID = "loca";
    private static final String TAG = "MyLocationService";
    private Timer timer;
    private TimerTask timerTask;

    public Location getLastLocation() {
        return lastLocation;
    }

    private Location lastLocation;

    private LocationManager locationManager;
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    Firebase firebase = new Firebase(mDatabase);
    String userID;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 위치가 변경될 때마다 서버로 위치 전송
            //sendLocationToServer(location);
            lastLocation = location;
            firebase.add_user_location(userID,location);

            firebase.getRoomUsers_loc(userID, new Firebase.RoomUsersLocCallback() {
                @Override
                public void onCallback(Map<String, String> userList) {
                    for(String key: userList.keySet()){
                        Log.d("asd", key+ " : \n" + userList.get(key));
                        Log.d("bb", ""+userList.size());
                    }
                }
            });
        }

    };

    public GPS_Service() {
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return Service.START_STICKY;
        else {
            userID = intent.getStringExtra("userID");
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = null;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_NOT_STICKY;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("TermProject")
                .setContentText("GPS를 통해 위치를 공유중입니다.")
                .setSmallIcon(R.drawable.ic_launcher_background);

        Notification notification = builder.build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, notification);

        startForeground(1, notification);

        //startLoggingLocation();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (timer != null) {
            timer.cancel();
        }
    }

}