package com.uy.safeboxv3app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VideoClips extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_clips);

        recyclerView = findViewById(R.id.recycler_video);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Video");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter <Video, VideoHolder> firebaseRecyclerAdapter =
        new FirebaseRecyclerAdapter<Video, VideoHolder>(
                Video.class,
                R.layout.video_row,
                VideoHolder.class,
                reference
        ) {
            @Override
            protected void populateViewHolder(VideoHolder videoHolder, Video video, int i) {
                videoHolder.setVideo(getApplication(), video.getUrl());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }
}
