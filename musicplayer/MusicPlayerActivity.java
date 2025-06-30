package com.example.musicplayer;

import static com.example.musicplayer.MyMediaPlayer.currentIndex;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, artistNameTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView musicIcon;
    ImageButton pausePlay, nextBtn, previousBtn, back_button, optionBar, btn_favorite;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    public static boolean isFavorite = false;
    public static FavoriteDao favoriteDao;
    public static final String CHANNEL_ID = "MUSIC_CHANNEL_ID";
    int noti_play = R.drawable.noti_pause;

    Bitmap bitmap;

    public static MusicPlayerActivity instance;
    public static MusicPlayerActivity getInstance() {
        return instance;
    }
    private static boolean isActive = false;

    public static boolean isActive() {
        return isActive;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        instance = this;

        titleTv = findViewById(R.id.songTitle);
        artistNameTv = findViewById(R.id.artistName);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.albumArt);
        back_button = findViewById(R.id.back_button);
        optionBar = findViewById(R.id.optionBar);
        btn_favorite = findViewById(R.id.btn_favorite);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            window.setNavigationBarColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        ImageButton buttonMain= findViewById(R.id.btn_home);
        Intent intent = new Intent();
        currentSong = (AudioModel) intent.getSerializableExtra("song");

        buttonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        ImageButton buttonFavorites = findViewById(R.id.buttonFavorites);

        buttonFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicPlayerActivity.this, FavoriteListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        favoriteDao = AppDatabase.getInstance(this).favoriteDao();

        btn_favorite.setOnClickListener(v -> {
            toggleFavorite();
        });

        titleTv.setSelected(true);

        //option Bar for Fav and playlists
        optionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MusicPlayerActivity.this, "Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();

        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));

                    if(mediaPlayer.isPlaying()) {
                        pausePlay.setBackgroundResource(R.drawable.ic_pause);
                    } else {
                        pausePlay.setBackgroundResource(R.drawable.ic_play);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void showNotification(String title) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.music) // Use your icon here
                .setContentTitle(title)
                .setLargeIcon(bitmap)
                .setContentText(currentSong.getArtist())
                .addAction(R.drawable.noti_prev, "Previous", getActionIntent("ACTION_PREVIOUS"))
                .addAction(noti_play, "Pause", getActionIntent("ACTION_PAUSE"))
                .addAction(R.drawable.noti_next, "Next", getActionIntent("ACTION_NEXT"))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1001, builder.build());
        }

    }

    private PendingIntent getActionIntent(String action) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void setResourcesWithMusic() {
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        titleTv.setText(currentSong.getTitle());
        artistNameTv.setText(currentSong.getArtist());

        AudioModel songData = currentSong;
        String path = songData.getPath();
        musicIcon.setImageDrawable(null);
        bitmap = getAlbumArt(path);
        if (bitmap != null) {
            musicIcon.setImageBitmap(bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.album_art_background);
        }

        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));
        String currentTitle = currentSong.getTitle();

        isFavorite = (favoriteDao.getFavoriteByTitle(currentTitle) != null);
        btn_favorite.setBackgroundResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_heart);
        pausePlay.setOnClickListener(v-> pausePlay());
        nextBtn.setOnClickListener(v-> playNextSong());
        previousBtn.setOnClickListener(v-> playPreviousSong());

        playMusic();
    }

    void toggleFavorite() {
        String currentTitle = currentSong.getTitle();
        if (isFavorite) {
            favoriteDao.deleteByTitle(currentTitle);
            isFavorite = false;
            btn_favorite.setBackgroundResource(R.drawable.ic_heart);
        } else if (mediaPlayer != null) {
            String path = currentSong.getPath();
            String duration = currentSong.getDuration();
            String artist = currentSong.getArtist();

            FavoriteSong favoriteSong = new FavoriteSong(currentTitle, path, duration, artist);
            favoriteDao.insert(favoriteSong);
            isFavorite = true;
            btn_favorite.setBackgroundResource(R.drawable.ic_favorite);
        }
    }


    private void playMusic() {

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
        showNotification(currentSong.getTitle());
    }

    void playNextSong() {
        if(MyMediaPlayer.currentIndex==songsList.size()-1)
            return;
        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    void playPreviousSong() {
        if(MyMediaPlayer.currentIndex==0)
            return;
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    void pausePlay() {
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            noti_play = R.drawable.noti_play;
            showNotification(currentSong.getTitle());
        }
        else {
            mediaPlayer.start();
            noti_play = R.drawable.noti_pause;
            showNotification(currentSong.getTitle());
        }
    }


    private Bitmap getAlbumArt(String filePath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    public static String convertToMMSS(String duration) {
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}