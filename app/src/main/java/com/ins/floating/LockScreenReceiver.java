package com.ins.floating;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ins.floating.player.YouTubePlayerService;

/**
 * Created by Maradona on 09/05/2017.
 */

public class LockScreenReceiver extends BroadcastReceiver {

    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            screenOff = true;
            YouTubePlayerService.pauseVid(screenOff);
            Log.d("LOCKSCREEN", "CALL" + screenOff);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            screenOff = false;
            YouTubePlayerService.pauseVid(screenOff);
            Log.d("LOCKSCREEN", "CALL" + screenOff);
        }
    }
}