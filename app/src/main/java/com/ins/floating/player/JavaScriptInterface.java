package com.ins.floating.player;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by INS Studio on 18/2/16.
 */
class JavaScriptInterface {
    Context context;
    static Handler handlerForJavascriptInterface = new Handler();
    public JavaScriptInterface(YouTubePlayerService playerService) {
        this.context = playerService;
    }

    @JavascriptInterface
    public void showPlayerState (final int status) {
        Log.d("Player Status ", String.valueOf(status));
        handlerForJavascriptInterface.post(new Runnable() {
            @Override
            public void run() {
                YouTubePlayerService.setPlayingStatus(status);
            }
        });
    }
    @JavascriptInterface
    public void showVID (final String vId) {
        Log.d("New Video Id ", vId);
        handlerForJavascriptInterface.post(new Runnable() {
            @Override
            public void run() {
                YouTubePlayerService.setImageTitleAuthor(vId);
            }
        });
    }
    @JavascriptInterface
    public void playlistItems (final String[] items) {
        Log.d("Playlist Items", String.valueOf(items.length));
        YouTubePlayerService.setNoItemsInPlaylist(items.length);
        YouTubePlayerService.compare();
    }
//    @JavascriptInterface
//    public void videosInPlaylist (final String[] items) {
//        Log.e("Videos In Playlist", String.valueOf(items));
//    }
    @JavascriptInterface
    public void currVidIndex (final int index) {
        Log.d("Current Video Index ", String.valueOf(index));
        YouTubePlayerService.setCurrVideoIndex(index);
        YouTubePlayerService.compare();
    }
}