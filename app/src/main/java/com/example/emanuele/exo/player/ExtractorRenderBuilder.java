package com.example.emanuele.exo.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emanuele on 13/06/16.
 */
public class ExtractorRenderBuilder implements RendererBuilder {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private final Uri mUri;

    private Context mContext;

    public ExtractorRenderBuilder(final Context context, final Uri uri) {
        mContext = context;
        mUri = uri;
    }

    @Override
    public void buildRender(Player player) {
        final Handler handler = player.getPlayerHandler();

        List<TrackRenderer> renderers = new ArrayList<>();

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(handler, null);
        DataSource dataSource = new DefaultUriDataSource(mContext, "android");
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                mUri,
                dataSource,
                new DefaultAllocator(BUFFER_SEGMENT_SIZE),
                BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE,
                handler,
                player,
                0, null);

        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(mContext,
                sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
                handler, player, 50);

        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                MediaCodecSelector.DEFAULT, null, true, handler, player,
                AudioCapabilities.getCapabilities(mContext), AudioManager.STREAM_MUSIC);

        renderers.add(videoRenderer);
        renderers.add(audioRenderer);

        player.onRendererBuild(renderers, bandwidthMeter);
    }

    @Override
    public void cancel() {

    }
}
