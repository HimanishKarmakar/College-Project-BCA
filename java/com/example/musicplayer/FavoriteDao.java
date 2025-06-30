package com.example.musicplayer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteSong song);

    @Query("DELETE FROM favorites WHERE songTitle = :title")
    void deleteByTitle(String title);

    @Query("SELECT * FROM favorites WHERE songTitle = :title LIMIT 1")
    FavoriteSong getFavoriteByTitle(String title);

    @Query("SELECT * FROM favorites")
    List<FavoriteSong> getAllFavorites();
}



