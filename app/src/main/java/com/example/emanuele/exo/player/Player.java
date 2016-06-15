package com.example.emanuele.exo.player;

import android.graphics.SurfaceTexture;
import android.os.Handler;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.BandwidthMeter;

import java.util.List;

/**
 * Created by emanuele on 13/06/16.
 */
public interface Player extends
        ExoPlayer.Listener,
        ExtractorSampleSource.EventListener,
        MediaCodecVideoTrackRenderer.EventListener,
        MediaCodecAudioTrackRenderer.EventListener,
        PlayBackInterface {
    
    Handler getPlayerHandler();

    void onRendererBuild(List<TrackRenderer> renderers, BandwidthMeter bandwidthMeter);

    SurfaceTexture getSurfaceTexture();

    void setSurfaceTexture(SurfaceTexture texture);

}
