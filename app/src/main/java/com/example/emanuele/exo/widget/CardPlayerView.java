package com.example.emanuele.exo.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;

import com.example.emanuele.exo.player.Player;

/**
 * Created by emanuele on 14/06/16.
 */
public class CardPlayerView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private Player mCardPlayer;
    private float mVideoAspectRatio;
    private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

    public CardPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void acquireVideoTexture(Player player) {
        mCardPlayer = player;
        TextureView textureView = new TextureView(getContext());
        addView(textureView);
        if (player != null) {
            if (player.getSurfaceTexture() != null) {
                textureView.setSurfaceTexture(player.getSurfaceTexture());
            }
        }
        textureView.setSurfaceTextureListener(this);
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
        return mCardPlayer != null && mCardPlayer.getSurfaceTexture() == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void releaseVideoTexture() {
        removeAllViews();
        mCardPlayer = null;
    }

    /**
     * Set the aspect ratio that this view should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    public void setAspectRatio(float widthHeightRatio) {
        if (mVideoAspectRatio != widthHeightRatio) {
            mVideoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mVideoAspectRatio == 0) {
            // Aspect ratio not set.
            return;
        }

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = mVideoAspectRatio / viewAspectRatio - 1;
        if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            // We're within the allowed tolerance.
            return;
        }

        if (aspectDeformation > 0) {
            height = (int) (width / mVideoAspectRatio);
        } else {
            width = (int) (height * mVideoAspectRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
