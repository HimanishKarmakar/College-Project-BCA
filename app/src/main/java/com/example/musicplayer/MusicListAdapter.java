package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>{

    ArrayList<AudioModel> songsList;
    private ArrayList<AudioModel> fullList;
    Context context;
    private final Map<String, Bitmap> albumArtCache = new HashMap<>();

    public MusicListAdapter(ArrayList<AudioModel> songsList, Context context) {
        this.songsList = songsList;
        this.fullList = new ArrayList<>(songsList);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MusicListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        AudioModel songData = songsList.get(position);
        String path = songData.getPath();
        holder.titleTextView.setText(songData.getTitle());
        holder.artistTextView.setText(songData.getArtist());
        holder.iconImageView.setImageResource(R.drawable.album_art_background);
        holder.iconImageView.setTag(path);

        if (albumArtCache.containsKey(path)) {
            Bitmap cachedArt = albumArtCache.get(path);
            if (cachedArt != null) {
                holder.iconImageView.setImageBitmap(cachedArt);
            }
        } else {
            new Thread(() -> {
                Bitmap albumArt = getAlbumArt(path);
                albumArtCache.put(path, albumArt);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (path.equals(holder.iconImageView.getTag())) {
                        if (albumArt != null) {
                            holder.iconImageView.setImageBitmap(albumArt);
                        }
                    }
                });
            }).start();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to another activity
                MyMediaPlayer.getInstance().reset();
                MyMediaPlayer.currentIndex = position;
                Intent intent = new Intent(context, MusicPlayerActivity.class);
                intent.putExtra("LIST", songsList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    public void filter(String text) {
        songsList.clear();
        if (text.isEmpty()) {
            songsList.addAll(fullList);
        } else {
            text = text.toLowerCase();
            for (AudioModel song : fullList) {
                if (song.getTitle().toLowerCase().contains(text) ||
                        song.getArtist().toLowerCase().contains(text)) {
                    songsList.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }


    private Bitmap getAlbumArt(String filePath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            if (art != null) {
                Bitmap originalBitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                return resizeBitmap(originalBitmap, 128, 128);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalHeight = maxHeight;
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalWidth = maxWidth;
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, artistTextView;
        ImageView iconImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            artistTextView = itemView.findViewById(R.id.artist_name_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
        }
    }
}
