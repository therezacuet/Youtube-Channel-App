package com.ins.floating;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.SearchEvent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ins.floating.player.YouTubePlayerService;
import com.ins.floating.views.CustomSwipeRefresh;
import com.ins.floating.volley.AppController;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import devlight.io.library.ntb.NavigationTabBar;

import static com.ins.floating.player.YouTubePlayerService.visible;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    Activity mainAct;
    WebView youtubeView;
    String currUrl;
    boolean doubleClickToExit = false;
    //For Result Activity
    public static int OVERLAY_PERMISSION_REQ = 1234;
    String VID, PID;
    //SearchView
    SearchView searchView;
    //Swipe Refresh
    CustomSwipeRefresh swipeRefreshLayout;
    boolean exit = false;
    private TabLayout tabLayout;
    Button retry, changeSettings, exitApp;

    ViewStub viewStub;
    private Drawer result = null;
    public static AdView mAdView;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mainAct = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (result != null && result.isDrawerOpen()) {
            Toast.makeText(getApplicationContext(),"dggdhghg",Toast.LENGTH_LONG).show();
        }


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.exit_dialog_layout);

        mAdView = (AdView) findViewById(R.id.adView);

        if(visible)
            hideBanner();
        else
            showBanner();

        viewStub = (ViewStub) findViewById(R.id.view_stub);

        //tabLayout = (TabLayout) findViewById(R.id.tabs);



        if(isInternetAvailable(mainAct)) {

            viewStub.setLayoutResource(R.layout.content_main);
            viewStub.inflate();

            exit = false;

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            //Swipe Refresh WebView
            swipeRefreshLayout = (CustomSwipeRefresh) findViewById(R.id.swipe_refresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(true);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            youtubeView.loadUrl(youtubeView.getUrl());
                        }
                    });
                }
            });


            // after initialization
            swipeRefreshLayout.setCanChildScrollUpCallback(new CustomSwipeRefresh.CanChildScrollUpCallback() {
                @Override
                public boolean canSwipeRefreshChildScrollUp() {
                    return youtubeView.getScrollY() > 0;
                }
            });

            youtubeView = (WebView) findViewById(R.id.youtube_view);
            youtubeView.getSettings().setJavaScriptEnabled(true);
            youtubeView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String str, Bitmap bitmap) {
                    super.onPageStarted(view, str, bitmap);
                    Log.d("Main Page Loading ", str);
                    swipeRefreshLayout.setRefreshing(true);

                    currUrl = str;
                }

                @Override
                public void onPageFinished(WebView view, String str) {
                    super.onPageFinished(view, str);
                    swipeRefreshLayout.setRefreshing(false);
                    Log.d("Main Page Finished", str);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains("?app=desktop") && !url.contains("signin?app=desktop")) {
                        Log.d("Url stopped to load : ", url);
                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                        final Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Desktop View Unavailable", Snackbar.LENGTH_LONG);
                        //Changing Text Color
                        View snkBar = snackbar.getView();
                        TextView tv = (TextView) snkBar.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.parseColor("#e52d27"));
                        snackbar.show();
                        return true;
                    }
                    return false;
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (String.valueOf(request.getUrl()).contains("http://m.youtube.com/watch?") ||
                                String.valueOf(request.getUrl()).contains("https://m.youtube.com/watch?")) {
                            String url = String.valueOf(request.getUrl());
                            Log.d("Yay Catches!!!! ", url);
                            //Video Id
                            VID = url.substring(url.indexOf("&v=") + 3, url.length());
                            Log.d("VID ", VID);
                            //Playlist Id
                            final String listID = url.substring(url.indexOf("&list=") + 6, url.length());
                            Pattern pattern = Pattern.compile(
                                    "([A-Za-z0-9_-]+)&[\\w]+=.*",
                                    Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(listID.toString());
                            Log.d("ListID", listID);
                            PID = "";
                            if (matcher.matches()) {
                                PID = matcher.group(1);
                            }
                            if (listID.contains("m.youtube.com")) {
                                Log.d("Not a ", "Playlist.");
                                PID = null;
                            } else {
                                Constants.linkType = 1;
                                Log.d("PlaylistID ", PID);
                            }
                            Handler handler = new Handler(getMainLooper());
                            final String finalPID = PID;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    youtubeView.stopLoading();
                                    youtubeView.goBack();
                                    if (isServiceRunning(YouTubePlayerService.class)) {
                                        Log.d("Service : ", "Already Running!");
                                        YouTubePlayerService.startVid(VID, finalPID);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                                            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:" + getPackageName()));
                                            startActivityForResult(i, OVERLAY_PERMISSION_REQ);
                                        } else {
                                            Intent i = new Intent(MainActivity.this, YouTubePlayerService.class);
                                            i.putExtra("VID_ID", VID);
                                            i.putExtra("PLAYLIST_ID", finalPID);
                                            i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                                            startService(i);
                                            if(finalPID!=null){
                                                Answers.getInstance().logCustom(new CustomEvent("StartPlayer")
                                                        .putCustomAttribute("Tipo", "PlayList"));
                                            }else{
                                                Answers.getInstance().logCustom(new CustomEvent("StartPlayer")
                                                        .putCustomAttribute("Tipo", "Video Unico"));
                                            }
                                        }

//                                    Intent i = new Intent(MainActivity.this, YouTubePlayerService.class);
//                                    i.putExtra("VID_ID", VID);
//                                    i.putExtra("PLAYLIST_ID", finalPID);
//                                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
//                                    startService(i);
                                    }

                                }
                            });
                        }
                    }
                    return super.shouldInterceptRequest(view, request);
                }
            });
            youtubeView.canGoBack();
            currUrl = "https://m.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA";
            youtubeView.loadUrl(currUrl);
        }
        else{

            viewStub.setLayoutResource(R.layout.content_no_internet);
            viewStub.inflate();

            exit = true;
            retry = (Button) findViewById(R.id.retry_internet);
            changeSettings = (Button) findViewById(R.id.change_settings);
            exitApp = (Button) findViewById(R.id.exit_app);
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainAct.recreate();
                }
            });
            changeSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                }
            });
            exitApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        final String[] colors = getResources().getStringArray(R.array.red_wine);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        new IconicsDrawable(this)
                                .icon(GoogleMaterial.Icon.gmd_home)
                                .color(Color.RED)
                                .sizeDp(18),
                        Color.parseColor(colors[2]))
//                        .selectedIcon(getResources().getDrawable(R.drawable.ic_eighth))
                        .title("Cup")
                        .badgeTitle("with")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        new IconicsDrawable(this)
                                .icon(GoogleMaterial.Icon.gmd_view_list)
                                .color(Color.RED)
                                .sizeDp(18),
                        Color.parseColor(colors[2]))
                        .title("Diploma")
                        .badgeTitle("state")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_favorite)
                        .color(Color.RED)
                        .sizeDp(24),
                        Color.parseColor(colors[2]))
                        .title("Heart")
                        .badgeTitle("NTB")
                        .build()
        );

        /*models.add(
                new NavigationTabBar.Model.Builder(
                        new IconicsDrawable(this)
                                .icon(GoogleMaterial.Icon.gmd_account_circle)
                                .color(Color.RED)
                                .sizeDp(18),
                        Color.parseColor(colors[2]))
                        .title("Diploma")
                        .badgeTitle("state")
                        .build()
        );*/
        navigationTabBar.setSelected(true);
        navigationTabBar.setModels(models);
        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {
                switch (index){
                    case 0:
                        youtubeView.loadUrl("https://m.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA");
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Home")
                                .putContentType("https://m.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA"));
                        break;

                    case 1:
                        youtubeView.loadUrl("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA/playlists");
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Playlists")
                                .putContentType("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA/playlists"));
                        break;

                    case 2:
                        youtubeView.loadUrl("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA/videos");
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Trending")
                                .putContentType("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA/videos"));
                        break;
                    /*case 3:
                        youtubeView.loadUrl("https://m.youtube.com/feed/account");
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Login")
                                .putContentType("https://m.youtube.com/feed/account"));
                        break;*/
                    default:
                        youtubeView.loadUrl("https://m.youtube.com/");
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Home")
                                .putContentType("https://m.youtube.com/"));
                        break;
                }
            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {
            }
        });
        //navigationTabBar.setViewPager(viewPager, 2);










        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/

        int w=120;
        //Create the drawer
        result = new DrawerBuilder(this)
                //this layout have to contain child layouts
                .withHeader(R.layout.nav_header_main)
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withInnerShadow(false)
                .withDrawerWidthDp(220)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("HOME").withIcon(GoogleMaterial.Icon.gmd_home),
                        /*new ExpandableDrawerItem().withName("MUSIC").withIcon(GoogleMaterial.Icon.gmd_library_music).withIdentifier(19).withSelectable(false).withSubItems(
                                new SecondaryDrawerItem().withName("POP").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002),
                                new SecondaryDrawerItem().withName("HIP HOP").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002),
                                new SecondaryDrawerItem().withName("ROCK").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2003),
                                new SecondaryDrawerItem().withName("HOUSE").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002),
                                new SecondaryDrawerItem().withName("GOSPEL MUSIC").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002),
                                new SecondaryDrawerItem().withName("ALTERNATIVE ROCK").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002),
                                new SecondaryDrawerItem().withName("HEAVY METAL").withLevel(2).withIcon(FontAwesome.Icon.faw_music).withIdentifier(2002)
                        ),*/
                        new PrimaryDrawerItem().withName("FAVORITE").withIcon(GoogleMaterial.Icon.gmd_favorite),
                        new PrimaryDrawerItem().withName("RATE US").withIcon(GoogleMaterial.Icon.gmd_rate_review),
                        new PrimaryDrawerItem().withName("RATE APPS").withIcon(GoogleMaterial.Icon.gmd_more),
                        new PrimaryDrawerItem().withName("SETTINGS").withIcon(GoogleMaterial.Icon.gmd_settings),
                        new SectionDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        Log.d("DREWARPOSITION", "POSITION ->" + position);

                        switch (position){
                            case 1:
                                youtubeView.loadUrl("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA");
                                break;
                            case 2:
                                youtubeView.loadUrl("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA");
                                break;
                            case 3:
                                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                // To count with Play market backstack, After pressing back button,
                                // to taken back to our application, we need to add following flags to intent.
                                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                                }
                                break;
                            case 4:
                                Uri uri2 = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                                Intent goToMarket2 = new Intent(Intent.ACTION_VIEW, uri2);
                                // To count with Play market backstack, After pressing back button,
                                // to taken back to our application, we need to add following flags to intent.
                                goToMarket2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(goToMarket2);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                                }
                                break;
                            case 5:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                break;
                            /*case 6:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UCTaFu5zwa9ySHWFlBo3aDPQ");
                                break;
                            case 7:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UCHtUkBSmt4d92XP8q17JC3w");
                                break;
                            case 8:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UCSkJDgBGvNOEXSQl4YNjDtQ");
                                break;
                            case 9:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw");
                                break;
                            case 10:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UCOpNcN46UbXVtpKMrmU4Abg");
                                break;

                            case 11:
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                break;*/
                            default:
                                youtubeView.loadUrl("https://m.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA");
                                break;
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();


    }

    public static void showBanner(){
        if(mAdView!=null) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("33BE2250B43518CCDA7DE426D04EE232").build();
            mAdView.loadAd(adRequest);
        }
    }

    public static void hideBanner(){
        if(mAdView!=null){
            mAdView.setVisibility(View.GONE);
        }
    }

    private boolean isServiceRunning(Class<YouTubePlayerService> playerServiceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (playerServiceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    needPermissionDialog(requestCode);
                } else {
                    Intent i = new Intent(this, YouTubePlayerService.class);
                    i.putExtra("VID_ID", VID);
                    i.putExtra("PLAYLIST_ID", PID);
                    i.setAction(Constants.ACTION.STARTFOREGROUND_WEB_ACTION);
                    startService(i);
                }
            }
        }
        else if(requestCode == 0) {
            mainAct.recreate();
        }
    }
    private void needPermissionDialog(final int requestCode) {
        if(requestCode == OVERLAY_PERMISSION_REQ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You need to grant the permission.");
            builder.setPositiveButton("OK",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(i, OVERLAY_PERMISSION_REQ);
                        }
                    });
            builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        final SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        if(searchView != null){
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            Log.d("Settings", "Act");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }*/

        if (id == R.id.share) {
            Log.d("Settings", "Act");
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Here is the share content body";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
            return true;
        }
       else if (id == R.id.rateUsa) {
            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            }
            return true;
        }







        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {

        final MainActivity acti = this;
        // custom dialog



        // set the custom dialog components - text, image and button


        Button dialogButton = (Button) dialog.findViewById(R.id.rateus_button);
        Button dialogButtonNo = (Button) dialog.findViewById(R.id.no_button);
        Button dialogButtonYes = (Button) dialog.findViewById(R.id.yesbutton);
        // if button is clicked, close the custom dialog
        dialogButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acti.finish();
                dialog.cancel();
            }
        });
        dialogButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                }
            }
        });
        dialog.show();


        /*if(exit){
            super.onBackPressed();
            return;
        }
        Log.d("Curr Url", currUrl);
        if(currUrl.equals("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA")) {
            if (doubleClickToExit) {
                super.onBackPressed();
                return;
            }

            this.doubleClickToExit = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleClickToExit = false;
                }
            }, 2000);
        }
        else {
            youtubeView.goBack();
        }

        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }*/
    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null) {
            Log.d("Network Test","no internet connection");
            return false;
        }
        else {
            if(info.isConnected()) {
                Log.d("Network Test"," internet connection available...");
                return true;
            }
            else {
                Log.d("Network Test"," internet connection");
                return true;
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        youtubeView.loadUrl("http://m.youtube.com/results?q="+ query);
        searchView.clearFocus();
        Answers.getInstance().logSearch(new SearchEvent()
                .putQuery(query));
        return true;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.length() > 0) {
            newText = newText.replace(" ", "+");
            String url = "http://suggestqueries.google.com/complete/search?client=youtube&ds=yt&client=firefox&hl="+ Locale.getDefault().getCountry()+"&q="
                    + newText;
            JsonArrayRequest req = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONArray jsonArraySuggestion = (JSONArray) response.get(1);
                                String[] suggestions = new String[10];
                                for (int i = 0; i < 10; i++) {
                                    if (!jsonArraySuggestion.isNull(i)) {
                                        suggestions[i] = jsonArraySuggestion.get(i).toString();
                                    }
                                }
                                Log.d("Suggestions", Arrays.toString(suggestions));
                                //Cursor Adaptor
                                String[] columnNames = {"_id", "suggestion"};
                                MatrixCursor cursor = new MatrixCursor(columnNames);
                                String[] temp = new String[2];
                                int id = 0;
                                for (String item : suggestions) {
                                    if (item != null) {
                                        temp[0] = Integer.toString(id++);
                                        temp[1] = item;
                                        cursor.addRow(temp);
                                    }
                                }
                                CursorAdapter cursorAdapter = new CursorAdapter(getApplicationContext(), cursor, false) {
                                    @Override
                                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                        return LayoutInflater.from(context).inflate(R.layout.search_suggestion_list_item, parent, false);
                                    }

                                    @Override
                                    public void bindView(View view, Context context, Cursor cursor) {
                                        final TextView suggest = (TextView) view.findViewById(R.id.suggest);
                                        ImageView putInSearchBox = (ImageView) view.findViewById(R.id.put_in_search_box);
                                        String body = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"));
                                        suggest.setText(body);
                                        suggest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                searchView.setQuery(suggest.getText(), true);
                                                searchView.clearFocus();
                                                Answers.getInstance().logCustom(new CustomEvent("Search suggestion")
                                                        .putCustomAttribute("Keyword", ""+suggest.getText()));
                                            }
                                        });
                                        putInSearchBox.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                searchView.setQuery(suggest.getText(), false);
                                            }
                                        });
                                    }
                                };
                                searchView.setSuggestionsAdapter(cursorAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Tag", "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req);

        }
        return true;
    }
    public static void checkUpdate(final Context ctx){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, "http://aresmp3.com.br/api/update/" + ctx.getPackageName(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            boolean update          = response.isNull("update") ? false            : response.getBoolean("update");
                            boolean cancel          = response.isNull("cancel") ? false            : response.getBoolean("cancel");
                            String mensagem        = response.isNull("mensagem") ? "null"         : response.getString("mensagem");
                            String btnOk           = response.isNull("btn") ? "null"              : response.getString("btn");
                            final String url       = response.isNull("url") ? "null"              : response.getString("url");

                            if(update){
                                new AlertDialog.Builder(ctx)
                                        .setCancelable(cancel)
                                        .setMessage(mensagem)
                                        .setPositiveButton(btnOk, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        //Creating a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        //Adding our request to the queue
        requestQueue.add(jsObjRequest);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment(), "ONE");
        adapter.addFragment(new Fragment(), "TWO");
        adapter.addFragment(new Fragment(), "THREE");
        viewPager.setAdapter(adapter);
    }

    /*public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
            youtubeView.loadUrl("https://www.youtube.com/channel/UC3cFw5JWFlht4T5vLPlejgA");

        } else if (id == R.id.nav_favorite) {

        } else if (id == R.id.nav_rateus) {

        } else if (id == R.id.nav_rateapps) {

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}