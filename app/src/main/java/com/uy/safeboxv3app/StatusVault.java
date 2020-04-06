package com.uy.safeboxv3app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StatusVault extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_vault);
    }
    public void unlockpage(View v){
        Intent intent = new Intent(this, UnlockVault.class);
        startActivity(intent);
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