package com.example.emanuele.exo.player;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.os.Handler;
import android.view.Surface;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.upstream.BandwidthMeter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by emanuele on 13/06/16.
 */
public class CardPlayer implements Player {


    public interface CardPlayerListener {
        void onStateChanged(boolean playWhenReady, int playbackState);

        void onError(Exception e);

        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                float pixelWidthHeightRatio);
    }

    private final ExoPlayer mExoPlayer;
    private final RendererBuilder mRendererBuilder;
    private final CopyOnWriteArrayList<CardPlayerListener> mListeners;

    private final Handler mHandler = new Handler();
    private SurfaceTexture mSurfaceTexture;
    private TrackRenderer mVideoRender;
    private TrackRenderer mAudioRender;


    private boolean mPlayWhenReady;
    private boolean mMute;
    private boolean mPlaying;

    public CardPlayer(RendererBuilder rendererBuilder) {
        mExoPlayer = ExoPlayer.Factory.newInstance(2, 1000, 5000);
        mExoPlayer.setPlayWhenReady(true);
        mExoPlayer.addListener(this);
        mListeners = new CopyOnWriteArrayList<>();
        mRendererBuilder = rendererBuilder;
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        for (CardPlayerListener listener : mListeners) {
            listener.onStateChanged(playWhenReady, playbackState);
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        mPlaying = mExoPlayer.getPlayWhenReady();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        for (CardPlayerListener listener : mListeners) {
            listener.onError(error);
        }
    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        for (CardPlayerListener listener : mListeners) {
            listener.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
        }
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
    }

    @Override
    public void onDecoderInitializationError(MediaCodecTrackRenderer.DecoderInitializationException e) {
    }

    @Override
    public void onCryptoError(MediaCodec.CryptoException e) {
    }

    @Override
    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
    }

    @Override
    public void onAudioTrackInitializationError(AudioTrack.InitializationException e) {
    }

    @Override
    public void onAudioTrackWriteError(AudioTrack.WriteException e) {
    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
    }

    @Override
    public Handler getPlayerHandler() {
        return mHandler;
    }

    @Override
    public void onRendererBuild(List<TrackRenderer> renderers, BandwidthMeter bandwidthMeter) {
        notifySurface();
        mExoPlayer.prepare(renderers.toArray(new TrackRenderer[renderers.size()]));
        for (TrackRenderer renderer : renderers) {
            if (renderer instanceof MediaCodecVideoTrackRenderer) {
                mVideoRender = renderer;
            } else if (renderer instanceof MediaCodecAudioTrackRenderer) {
                mAudioRender = renderer;
            }
        }
    }

    public void prepare() {
        if (mRendererBuilder != null) {
            mRendererBuilder.cancel();
            mRendererBuilder.buildRender(this);
        }
    }

    @Override
    public void onLoadError(int sourceId, IOException e) {
    }

    private void notifySurface() {
        if (mVideoRender == null || mExoPlayer == null) {
            return;
        }
        Surface surface = mSurfaceTexture == null ? null : new Surface(mSurfaceTexture);
        mExoPlayer.sendMessage(
                mVideoRender, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }

    private void notifySurfaceBlocking() {
        if (mVideoRender == null || mExoPlayer == null) {
            return;
        }
        Surface surface = mSurfaceTexture == null ? null : new Surface(mSurfaceTexture);
        mExoPlayer.blockingSendMessage(
                mVideoRender, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (mPlayWhenReady != playWhenReady) {
            mExoPlayer.setPlayWhenReady(playWhenReady);
        }
        mPlayWhenReady = playWhenReady;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        notifySurface();
    }

    @Override
    public void seekTo(long positionMs) {
        if (mExoPlayer == null) {
            return;
        }
        mExoPlayer.seekTo(positionMs);
    }

    @Override
    public long getDuration() {
        if (mExoPlayer == null) {
            return ExoPlayer.UNKNOWN_TIME;
        }
        return mExoPlayer.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        if (mExoPlayer == null) {
            return ExoPlayer.UNKNOWN_TIME;
        }
        return mExoPlayer.getCurrentPosition();
    }

    @Override
    public void mute() {
        if (mExoPlayer == null || mAudioRender == null || mMute) {
            return;
        }
        mMute = true;
        mExoPlayer.sendMessage(mAudioRender, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 0f);
    }

    @Override
    public void unMute() {
        if (mExoPlayer == null || mAudioRender == null || !mMute) {
            return;
        }
        mMute = false;
        mExoPlayer.sendMessage(mAudioRender, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, 1f);
    }

    @Override
    public void toggleVolume() {
        if (mExoPlayer == null) {
            return;
        }
        if (mMute) {
            unMute();
        } else {
            mute();
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mExoPlayer == null || mAudioRender == null) {
            return;
        }
        mExoPlayer.sendMessage(mAudioRender, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, volume);
    }

    @Override
    public void pause() {
        if (mExoPlayer == null || !mPlaying) {
            return;
        }
        mExoPlayer.setPlayWhenReady(mPlaying = false);
    }

    @Override
    public void start() {
        if (mExoPlayer == null || mPlaying) {
            return;
        }
        mExoPlayer.setPlayWhenReady(mPlaying = true);
    }

    public void clearSurface() {
        mSurfaceTexture = null;
        notifySurfaceBlocking();
    }

    public void addListener(CardPlayerListener l) {
        mListeners.add(l);
    }

    public void release() {
        mRendererBuilder.cancel();
        mSurfaceTexture = null;
        mExoPlayer.release();
    }


    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public long getState() {
        if (mExoPlayer == null) {
            return ExoPlayer.UNKNOWN_TIME;
        }
        return mExoPlayer.getPlaybackState();
    }

}
