package com.ins.floating.player;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.ins.floating.R;

public class FullScreenWebPlayer extends Activity {

    static boolean active = false;
    static Activity fullScreenAct;

    ViewGroup parent;
    WebView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.active = true;
        fullScreenAct = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player_web);

        Answers.getInstance().logCustom(new CustomEvent("FullScreenPlayer"));
        LinearLayout ll = (LinearLayout) findViewById(R.id.layout_fullscreen);
        player = WebPlayer.getPlayer();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        );
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        parent = (ViewGroup) player.getParent();
        parent.removeView(player);

        ll.addView(player, params);

        WebPlayer.loadScript(JavaScript.playVideoScript());

    }

    @Override
    public void onBackPressed() {
        if(active){
            ((ViewGroup) player.getParent()).removeView(player);
            parent.addView(player);
            YouTubePlayerService.startAgain();
        }
        active = false;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if(active) {
            fullScreenAct.onBackPressed();
        }
        active = false;
        super.onPause();
    }
}
