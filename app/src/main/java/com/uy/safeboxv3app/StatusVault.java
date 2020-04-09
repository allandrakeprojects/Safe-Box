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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
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

        UserStatus();
    }

    public void UserStatus() {
        // detect Device table exists
        final Intent intent = new Intent(this, MainActivity.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String android_id = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
                if (dataSnapshot.hasChild("Users")) {
                    // loop all users if deviceID match

                    // if exists, count if 3 show toast 2 users required
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference yourRef = rootRef.child("Users");
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long count = dataSnapshot.getChildrenCount();

                            if(count == 2) {
                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference yourRef = rootRef.child("Users");
                                ValueEventListener eventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean detected = false;

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if(snapshot.child("deviceID").getValue().equals(android_id)) {
                                                detected = true;
                                                // save id local
                                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("CURRENT_ID", snapshot.child("ID").getValue() + "");
                                                editor.commit();

                                                RealtimeVaultStatus();
                                                PhoneStatus();
                                                VibrationStatus();

                                                break;
                                            }
                                        }

                                        if(!detected) {
                                            Toast.makeText(getApplication(), "Limit 2 users only. Auto close initiated.", Toast.LENGTH_SHORT).show();

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                    moveTaskToBack(true);
                                                }
                                            }, 2000);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                };
                                yourRef.addListenerForSingleValueEvent(eventListener);
                            } else {
                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference yourRef = rootRef.child("Users");
                                ValueEventListener eventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if(snapshot.child("deviceID").getValue().equals(android_id)) {
//                                                Toast.makeText(getApplication(), snapshot.child("ID").getValue() + "", Toast.LENGTH_SHORT).show();
//                                                Toast.makeText(getApplication(), snapshot.child("deviceID").getValue() + "", Toast.LENGTH_SHORT).show();

                                                // save id local
                                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("CURRENT_ID", snapshot.child("ID").getValue() + "");
                                                editor.commit();

                                                RealtimeVaultStatus();
                                                PhoneStatus();
                                                VibrationStatus();
                                            } else {
                                                DatabaseReference databaseReferenceStore = FirebaseDatabase.getInstance().getReference("Users");
                                                String id = databaseReferenceStore.push().getKey();
                                                databaseReferenceStore.child(id).child("ID").setValue("2");
                                                databaseReferenceStore.child(id).child("deviceID").setValue(android_id);

                                                // save id local
                                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("CURRENT_ID", "2");
                                                editor.commit();

                                                RealtimeVaultStatus();
                                                PhoneStatus();
                                                VibrationStatus();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                };
                                yourRef.addListenerForSingleValueEvent(eventListener);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    yourRef.addListenerForSingleValueEvent(eventListener);
                } else {
                    // if not insert, get mac address
                    // store
                    DatabaseReference databaseReferenceStore = FirebaseDatabase.getInstance().getReference("Users");
                    String id = databaseReferenceStore.push().getKey();
                    databaseReferenceStore.child(id).child("ID").setValue("1");
                    databaseReferenceStore.child(id).child("deviceID").setValue(android_id);

                    // save id local
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("CURRENT_ID", "1");
                    editor.commit();

                    RealtimeVaultStatus();
                    PhoneStatus();
                    VibrationStatus();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
    }

    public void RealtimeVaultStatus() {
        final Intent intent = new Intent(this, StatusVault.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("lockSensor");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String vaultStatus = (String) dataSnapshot.getValue();

//                Toast.makeText(getApplication(), vaultStatus + "", Toast.LENGTH_LONG).show();

                if(vaultStatus.equals("True"))
                {
                    //unlock image
                    Log.d("Test", "Unlock");
                    IV_VaultStatus.setImageDrawable(getResources().getDrawable(R.drawable.safeboxstatus_unlock));

                    SharedPreferences settingss = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                    SharedPreferences.Editor editor = settingss.edit();
                    editor.putString("IS_UNLOCK_VAULT", "0");
                    editor.commit();


                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }

                    // notification
                    // TODO If userIDReq is not equal to CURRENT_ID show notification
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("userIDReq");
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String userIDReq = (String) dataSnapshot.getValue();

                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                            String currentId = settings.getString("CURRENT_ID", "0");

                            if(!userIDReq.equals(currentId)) {
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

//                                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("userIDReq");
//                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        databaseReference.setValue("0");
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // leave blank
                        }
                    });
                }
                else {
                    //lock image
                    //if previously at unlockvault
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                    String IS_UNLOCK_VAULT = settings.getString("IS_UNLOCK_VAULT", "0");

//                    Toast.makeText(getApplication(), IS_UNLOCK_VAULT + "", Toast.LENGTH_LONG).show();

                    if(IS_UNLOCK_VAULT.equals("0")) {
                        Log.d("Test", "Lock");
                        IV_VaultStatus.setImageDrawable(getResources().getDrawable(R.drawable.safeboxstatus));

                        SharedPreferences settingss = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                        SharedPreferences.Editor editor = settingss.edit();
                        editor.putString("IS_UNLOCK_VAULT", "0");
                        editor.commit();

                        // notification
                        // TODO If userIDReq is not equal to CURRENT_ID show notification
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
                                .setContentText("Vault Lock.")
                                .setContentInfo("Info");

                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                                new Intent(getApplicationContext(), StatusVault.class), PendingIntent.FLAG_UPDATE_CURRENT);

                        notificationBuilder.setContentIntent(contentIntent);

                        notificationManager.notify(/*notification id*/2, notificationBuilder.build());
                    }
                }

                IV_VaultStatus.setVisibility(View.VISIBLE);
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
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("userIDReq");
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String userIDReq = (String) dataSnapshot.getValue();

                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(StatusVault.this);
                            String currentId = settings.getString("CURRENT_ID", "0");

                            if(userIDReq.equals(currentId)) {
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

                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("vibrationSensor");
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            databaseReference.setValue("False");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
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