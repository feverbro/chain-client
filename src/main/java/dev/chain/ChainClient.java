package dev.chain;

import dev.chain.config.ConfigManager;
import dev.chain.gui.HudRenderer;
import dev.chain.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ChainClient implements ClientModInitializer {
    public static ModuleManager modules;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        ConfigManager.load(modules);

        ClientTickEvents.END_CLIENT_TICK.register(client -> modules.tick(client));
        HudRenderCallback.EVENT.register((context, tickCounter) -> HudRenderer.render(context));
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save(modules));
    }
}
