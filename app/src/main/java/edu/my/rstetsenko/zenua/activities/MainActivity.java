package edu.my.rstetsenko.zenua.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;

import edu.my.rstetsenko.zenua.Constants;
import edu.my.rstetsenko.zenua.R;
import edu.my.rstetsenko.zenua.Utility;
import edu.my.rstetsenko.zenua.fragments.ExchangeRateFragment;
import edu.my.rstetsenko.zenua.sync.ZenUaSyncAdapter;


public class MainActivity extends ActionBarActivity {
    private final String RATE_FRAGMENT_TAG = "RFTAG";

    private VideoView videoView;
    private MenuItem soundItem;
    private MediaPlayer mediaPlayer;
    private boolean isPlayerPrepared = false;
    private int mSourceId;
    private View container;
    private int screenHeight, screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        getWindow().setAttributes(attributes);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSourceId = Utility.getPreferredSource();
        setContentView(R.layout.activity_main);

        container = findViewById(R.id.container);
        getScreenSize();

        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoURI(getVideoUri());
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnErrorListener(onErrorListener);
        LogMessage("onCreate");
        mediaPlayer = MediaPlayer.create(this, R.raw.wawe49s);
        isPlayerPrepared = false;
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPlayerPrepared = true;
                if (Utility.isSoundOn()) {
                    mediaPlayer.start();
                    LogMessage("Start mediaPlayer OnPreparedListener");
                }
                mp.setLooping(true);
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ExchangeRateFragment(), RATE_FRAGMENT_TAG)
                    .commit();
        }
        ZenUaSyncAdapter.initializeSyncAdapter(this);
        //TODO handle VideoView state. maybe migrate selecting of sources from settings to navigation drawer
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            LogMessage("Start mediaPlayer onStart");
        }
        LogMessage("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            LogMessage("Start mediaPlayer onResume");
        }
        int sourceId = Utility.getPreferredSource();
        if (sourceId != mSourceId) {
            ExchangeRateFragment erf = (ExchangeRateFragment) getSupportFragmentManager().findFragmentByTag(RATE_FRAGMENT_TAG);
            if (null != erf) {
                erf.onSourceChanged();
                LogMessage("MyActivity update via onSourceChanged");
            }
            mSourceId = sourceId;
        }
        LogMessage("onResume");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        soundItem = menu.findItem(R.id.sound);
        updateSoundItem();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogMessage("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
//        if (videoView != null && videoView.isPlaying()) {
//            videoView.pause();
//        }
        LogMessage("onStop, videoView.pause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (videoView != null){
            videoView.suspend();
            videoView = null;
        }
        LogMessage("onDestroy");
    }

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            videoView.start();
            mp.setLooping(true);
            LogMessage("Start videoView onPrepared");
        }
    };

    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogMessage("MediaPlayer.OnErrorListener, what:" + what + ", extra:" + extra);
            videoView.setVisibility(View.INVISIBLE);
            videoView.suspend();
            Bitmap background = decodeFile(R.drawable.sea5);
            BitmapDrawable backgroundDrawable = new BitmapDrawable(getResources(), background);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                container.setBackgroundDrawable(backgroundDrawable);
            } else {
                container.setBackground(backgroundDrawable);
            }
            return true;
        }
    };

    private Uri getVideoUri(){
        return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wawe1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sound:
                Utility.toggleSound();
                if (Utility.isSoundOn() && isPlayerPrepared && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    LogMessage("Start mediaPlayer onOptionsItemSelected sound");
                }
                if (!Utility.isSoundOn() && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                updateSoundItem();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSoundItem() {
        if (Utility.isSoundOn()){
            soundItem.setTitle(getString(R.string.sound_on));
            soundItem.setIcon(R.drawable.ic_volume_on);
        } else {
            soundItem.setTitle(getString(R.string.sound_off));
            soundItem.setIcon(R.drawable.ic_volume_off);
        }
    }

    private void getScreenSize(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
    }

    private Bitmap decodeFile(int imageId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Resources resources = getResources();
        BitmapFactory.decodeResource(resources, imageId, options);
        final int longest = options.outHeight > options.outWidth ? options.outHeight : options.outWidth;
        int required = screenHeight > screenWidth ? screenHeight/2 : screenWidth/2;
        int inSampleSize = 1;
        if (longest > required) {
            while ((longest / inSampleSize) > required) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, imageId, options);
    }

    private void LogMessage(String msg) {
        Log.d(Constants.LOG_TAG, msg);
    }
}
