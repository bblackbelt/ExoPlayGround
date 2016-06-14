package com.example.emanuele.exo.player;

/**
 * Created by emanuele on 13/06/16.
 */
public interface RendererBuilder {
    void buildRender(Player player);
    void cancel();
}
