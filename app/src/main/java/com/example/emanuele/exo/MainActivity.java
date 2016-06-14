package com.example.emanuele.exo;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.SeekBar;

import com.example.emanuele.exo.player.CardPlayer;
import com.example.emanuele.exo.player.ExtractorRenderBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;

public class MainActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener,
        CardPlayer.CardPlayerListener {

    private CardPlayer mCardPlayer;
    private AspectRatioFrameLayout mVideoFrame;
    private SeekBar mSeekBar;

    private final Handler mHandler = new Handler();

    private Runnable mSeekBarProgressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mSeekBar == null) {
                return;
            }
            if (mCardPlayer == null || mCardPlayer.getState() == ExoPlayer.STATE_ENDED) {
                mSeekBar.setProgress((int) (mCardPlayer.getCurrentPosition() / 1000));
                return;
            }
            mSeekBar.setProgress((int) (mCardPlayer.getCurrentPosition() / 1000));
            mHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        TextureView textureView = (TextureView) findViewById(R.id.video_texture_view);
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }
        preparePlayer(Uri.parse("http://qa.jwplayer.com/support-demos/static/bunny.mp4"));
    }

    private void preparePlayer(Uri videoUri) {
        mCardPlayer = new CardPlayer(new ExtractorRenderBuilder(this, videoUri));
        mCardPlayer.setPlayWhenReady(true);
        mCardPlayer.addListener(this);
        mCardPlayer.prepare();
    }

    @Override
    public void onStateChanged(boolean playWhenReady, final int playbackState) {

        switch (playbackState) {
            case ExoPlayer.STATE_READY:
                setupSeekBar();
                setCardProgressBarVisibility(false);
                mHandler.post(mSeekBarProgressUpdater);
                break;
            case ExoPlayer.STATE_BUFFERING:
            case ExoPlayer.STATE_PREPARING:
                setCardProgressBarVisibility(true);
                break;
        }
    }

    private void setCardProgressBarVisibility(final boolean visible) {
        findViewById(R.id.playback_progress_bar).setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    private void setupSeekBar() {
        if (mCardPlayer == null) {
            return;
        }
        mSeekBar = (SeekBar) findViewById(R.id.playback_seek_bar);
        if (mSeekBar == null) {
            return;
        }
        mSeekBar.setMax((int) mCardPlayer.getDuration() / 1000);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mSeekBarProgressUpdater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mCardPlayer != null) {
                    mCardPlayer.seekTo(seekBar.getProgress() * 1000);
                }
                mHandler.postDelayed(mSeekBarProgressUpdater, 500);
            }
        });
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        mVideoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_volume) {
            mCardPlayer.toggleVolume();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mCardPlayer != null) {
            mCardPlayer.clearSurface();
            mCardPlayer.release();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCardPlayer != null) {
            mCardPlayer.clearSurface();
            mCardPlayer.release();
        }
        mHandler.removeCallbacks(mSeekBarProgressUpdater);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCardPlayer != null && mCardPlayer.getSurfaceTexture() == null) {
            mCardPlayer.setSurfaceTexture(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCardPlayer.clearSurface();
        return mCardPlayer != null && mCardPlayer.getSurfaceTexture() == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
