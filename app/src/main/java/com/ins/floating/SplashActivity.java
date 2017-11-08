package com.ins.floating;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.ins.floating.player.PlayerYouTubeView;

import static com.ins.floating.player.YouTubePlayerService.visible;

/**
 * Created by Maradona on 09/05/2017.
 */

public class SplashActivity extends AppCompatActivity {

    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_splash));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                sairSplash();

                Answers.getInstance().logCustom(new CustomEvent("AdsEvent - Splash")
                        .putCustomAttribute("AdsEvent", "AdsClose"));
            }

            @Override
            public void onAdFailedToLoad(int i) {
                sairSplash();

                Answers.getInstance().logCustom(new CustomEvent("AdsEvent - Splash")
                        .putCustomAttribute("AdsEvent", "AdsFalhouCarregar"));
            }

            @Override
            public void onAdLoaded() {
                if(!visible){
                    if(mInterstitialAd.isLoaded()){
                    mInterstitialAd.show();

                    Answers.getInstance().logCustom(new CustomEvent("AdsEvent - Splash")
                            .putCustomAttribute("AdsEvent", "ExibiuAnuncio"));
                }}else{
                    sairSplash();
                }
            }

            @Override
            public void onAdOpened() {
                Answers.getInstance().logCustom(new CustomEvent("AdsEvent - Splash")
                        .putCustomAttribute("AdsEvent", "AbriuAds"));
            }
        });

        requestNewInterstitial();
    }

    @Override
    public void onBackPressed() {
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE232")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
    public void sairSplash(){
        startActivity(new Intent(this, PlayerYouTubeView.class));
        finish();
    }
}
