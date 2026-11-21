package com.example.mutantsquid.client;

import com.example.mutantsquid.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MutantSquidClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.RITUAL_SHELL, RitualShellRenderer::new);
        EntityRendererRegistry.register(ModEntities.TENTACLE, TentacleRenderer::new);
        EntityRendererRegistry.register(ModEntities.MUTANT_SQUID, MutantSquidRenderer::new);
    }
}
