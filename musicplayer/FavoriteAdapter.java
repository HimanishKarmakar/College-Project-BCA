package com.example.musicplayer;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    private List<FavoriteSong> favorites = new ArrayList<>();
    private final Map<String, Bitmap> albumArtCache = new HashMap<>();
    private Context context;

    public FavoriteAdapter(Context context) {
        this.context = context;
    }

    public void setFavorites(List<FavoriteSong> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_song, parent, false);
        return new FavoriteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteSong song = favorites.get(position);
        holder.music_title_text.setText(song.songTitle);
        holder.artistTextView.setText(song.songArtist);
        String path = song.songPath;
        holder.icon_view.setImageResource(R.drawable.album_art_background);
        holder.icon_view.setTag(path);

        if (albumArtCache.containsKey(path)) {
            Bitmap cachedArt = albumArtCache.get(path);
            if (cachedArt != null) {
                holder.icon_view.setImageBitmap(cachedArt);
            }
        } else {
            new Thread(() -> {
                Bitmap albumArt = getAlbumArt(path);
                albumArtCache.put(path, albumArt);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (path.equals(holder.icon_view.getTag())) {
                        if (albumArt != null) {
                            holder.icon_view.setImageBitmap(albumArt);
                        }
                    }
                });
            }).start();
        }

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FavoriteListActivity) {
                ((FavoriteListActivity) context).playFavoriteSong(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return favorites.size();
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

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView music_title_text, artistTextView;
        ImageView icon_view;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            music_title_text = itemView.findViewById(R.id.music_title_text);
            artistTextView = itemView.findViewById(R.id.artist_name_text);
            icon_view = itemView.findViewById(R.id.icon_view);
        }
    }
}

