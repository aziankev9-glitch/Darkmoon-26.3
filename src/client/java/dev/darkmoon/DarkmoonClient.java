package dev.darkmoon;

import net.fabricmc.api.ClientModInitializer;

/**
 * Minimal, safe Fabric 26.3 entrypoint.
 * Gameplay features and vanilla-style menu will be added after a verified compile.
 */
public final class DarkmoonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[Darkmoon Lite] Fabric 26.3 client initialized");
    }
}
