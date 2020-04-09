package com.uy.safeboxv3app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StatusVault extends AppCompatActivity {

    public ProgressDialog dialog;
    public ImageView IV_VaultStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_vault);

        IV_VaultStatus = findViewById(R.id.IV_VaultStatus);

        PhoneStatus();
        RealtimeVaultStatus();
        VibrationStatus();
    }

    public void RealtimeVaultStatus() {
        final Intent intent = new Intent(this, StatusVault.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("lockSensor");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String vaultStatus = (String) dataSnapshot.getValue();

                if(vaultStatus.equals("True")) {
                    //unlock image
                    Log.d("Test", "Unlock");
                    IV_VaultStatus.setImageDrawable(getResources().getDrawable(R.drawable.safeboxstatus_unlock));

                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }

                    // notification
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                        // Configure the notification channel.
                        notificationChannel.setDescription("Channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }


                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);

                    notificationBuilder.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.safetitle)
                            .setTicker("Hearty365")
                            //     .setPriority(Notification.PRIORITY_MAX)
                            .setContentTitle("Vault Status")
                            .setContentText("Vault Unlocked.")
                            .setContentInfo("Info");

                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), StatusVault.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    notificationBuilder.setContentIntent(contentIntent);

                    notificationManager.notify(/*notification id*/1, notificationBuilder.build());
                } else {
                    //lock image
                    Log.d("Test", "Lock");
                    IV_VaultStatus.setImageDrawable(getResources().getDrawable(R.drawable.safeboxstatus));

                    // notification
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String NOTIFICATION_CHANNEL_ID = "my_channel_id_02";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                        // Configure the notification channel.
                        notificationChannel.setDescription("Channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }


                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);

                    notificationBuilder.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.safetitle)
                            .setTicker("Hearty365")
                            //     .setPriority(Notification.PRIORITY_MAX)
                            .setContentTitle("Vault Status")
                            .setContentText("Vault Locked.")
                            .setContentInfo("Info");

                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), StatusVault.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    notificationBuilder.setContentIntent(contentIntent);

                    notificationManager.notify(/*notification id*/2, notificationBuilder.build());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
    }

    public void PhoneStatus() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("phoneButton");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String vaultStatus = (String) dataSnapshot.getValue();

                if(vaultStatus.equals("True")) {
                    dialog = new ProgressDialog(StatusVault.this);
                    dialog.setMessage("Unlocking. Please wait...");
                    dialog.show();
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                } else {
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
    }

    public void VibrationStatus() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("vibrationSensor");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = (String) dataSnapshot.getValue();

                if(status.equals("True")) {
                    // notification
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    String NOTIFICATION_CHANNEL_ID = "my_channel_id_03";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

                        // Configure the notification channel.
                        notificationChannel.setDescription("Channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.setLightColor(Color.RED);
                        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }


                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);

                    notificationBuilder.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.safetitle)
                            .setTicker("Hearty365")
                            //     .setPriority(Notification.PRIORITY_MAX)
                            .setContentTitle("Vault Status")
                            .setContentText("Vibration Detected.")
                            .setContentInfo("Info");

                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                            new Intent(getApplicationContext(), StatusVault.class), PendingIntent.FLAG_UPDATE_CURRENT);

                    notificationBuilder.setContentIntent(contentIntent);

                    notificationManager.notify(/*notification id*/3, notificationBuilder.build());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
    }

    public void UserStatus() {
        // detect Device table exists
        // if exists, count if 3 show toast 2 users required
        // if not insert, get mac address
    }

    public void unlockpage(View v){
        final Intent intent = new Intent(this, UnlockVault.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("lockSensor");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String vaultStatus = (String) dataSnapshot.getValue();

                if(vaultStatus.equals("True")) {
                    Toast.makeText(getApplication(), "Safe Box already Unlocked.", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // leave blank
            }
        });
    }

    public void videopage(View v){
        Intent intent = new Intent(this, VideoClips.class);
        startActivity(intent);
    }

    public void helppage(View v){
        Intent intent = new Intent(this, HelpScreen.class);
        startActivity(intent);
    }

    public void logspage(View v){
        Intent intent = new Intent(this, ActivityLogs.class);
        startActivity(intent);
    }

}