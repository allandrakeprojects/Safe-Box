package com.uy.safeboxv3app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UnlockVault  extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlock_vault);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UnlockVault.this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("IS_UNLOCK_VAULT", "1");
        editor.commit();
    }

    public void unlockVault(View v){
        final Intent intent = new Intent(this, StatusVault.class);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(UnlockVault.this);
        String currentID = settings.getString("CURRENT_ID", "0");

        // firebase - change phoneButton to true
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("phoneButton");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                databaseReference.setValue("True");

                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Sensors").child("userIDReq");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        databaseReference.setValue(currentID);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
