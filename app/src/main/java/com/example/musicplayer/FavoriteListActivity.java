package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private AppDatabase database;
    private ArrayList<AudioModel> songsList;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoriteAdapter(this);
        recyclerView.setAdapter(adapter);
        songsList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        ImageButton buttonMain= findViewById(R.id.btn_home);
        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FavoriteListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        ImageButton btnMusic = findViewById(R.id.btn_music);
        btnMusic.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                Intent intent = new Intent(FavoriteListActivity.this, MusicPlayerActivity.class);
                intent.putExtra("LIST", songsList);
                intent.putExtra("CURRENT_INDEX", MyMediaPlayer.currentIndex);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No song is currently playing", Toast.LENGTH_SHORT).show();
            }
        });

        database = AppDatabase.getInstance(this);
        loadFavorites();
    }

    public void playFavoriteSong(int index) {
        MyMediaPlayer.currentIndex = index;

        Intent intent = new Intent(FavoriteListActivity.this, MusicPlayerActivity.class);
        intent.putExtra("LIST", songsList);
        intent.putExtra("CURRENT_INDEX", index);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadFavorites() {
        new AsyncTask<Void, Void, List<FavoriteSong>>() {
            @Override
            protected List<FavoriteSong> doInBackground(Void... voids) {
                return database.favoriteDao().getAllFavorites();
            }

            @Override
            protected void onPostExecute(List<FavoriteSong> favoriteSongs) {
                adapter.setFavorites(favoriteSongs);

                songsList = new ArrayList<>();
                for (FavoriteSong fav : favoriteSongs) {
                    AudioModel song = new AudioModel(
                            fav.getSongPath(),
                            fav.getSongTitle(),
                            fav.getSongDuration(),
                            fav.getSongArtist()
                    );
                    songsList.add(song);
                }
            }
        }.execute();
    }
}


