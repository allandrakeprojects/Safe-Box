package com.uy.safeboxv3app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HelpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_screen);
    }
    public void helpunlock(View v){
        Intent intent = new Intent(this, HelpUnlock.class);
        startActivity(intent);
    }
    public void helpregister(View v){
        Intent intent = new Intent(this, HelpRegister.class);
        startActivity(intent);
    }
    public void helpdelete(View v){
        Intent intent = new Intent(this, HelpDelete.class);
        startActivity(intent);
    }
    public void helpforgot(View v){
        Intent intent = new Intent(this, HelpForgot.class);
        startActivity(intent);
    }
}
