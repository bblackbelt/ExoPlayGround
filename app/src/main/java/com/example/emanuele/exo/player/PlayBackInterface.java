package com.example.emanuele.exo.player;

/**
 * Created by emanuele on 14/06/16.
 */
public interface PlayBackInterface {
    void seekTo(long positionMs);

    long getDuration();

    long getCurrentPosition();

    void mute();

    void unMute();

    void toggleVolume();

    /**
     * @param volume float value in the range [0,1] representing the volume level.
     */
    void setVolume(float volume);
    
    void pause();
    
    void start();

}
