package com.uy.safeboxv3app;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityLogs extends AppCompatActivity {

    public TextView TV_Date;
    public TextView TV_Message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        TV_Date = findViewById(R.id.TV_Date);
        TV_Message = findViewById(R.id.TV_Message);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date formatDate = dateFormat.parse(snapshot.child("logsTimestamp").getValue() + "");
                    TV_Date.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(formatDate));
                    TV_Message.setText(snapshot.child("updatedLogs").getValue().toString());
                } catch (ParseException e) {
                    Log.d("test", e.getMessage());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}