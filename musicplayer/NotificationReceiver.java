package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

//        ArrayList<AudioModel> list = MusicPlayerActivity.songsList; // Current playlist

        switch (action) {
            case "ACTION_PAUSE":
                MusicPlayerActivity.getInstance().pausePlay();
                break;
            case "ACTION_NEXT":
                MusicPlayerActivity.getInstance().playNextSong();
                break;
            case "ACTION_PREVIOUS":
                MusicPlayerActivity.getInstance().playPreviousSong();
                break;
        }
    }
}


