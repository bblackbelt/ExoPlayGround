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
    
}
