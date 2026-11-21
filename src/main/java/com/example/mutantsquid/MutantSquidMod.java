package com.example.mutantsquid;

import com.example.mutantsquid.command.SkipRitualCommand;
import com.example.mutantsquid.entity.MutantSquidEntity;
import com.example.mutantsquid.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutantSquidMod implements ModInitializer {
    public static final String MODID = "mutantsquid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        ModEntities.register();
        FabricDefaultAttributeRegistry.register(ModEntities.MUTANT_SQUID, MutantSquidEntity.createAttributes());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SkipRitualCommand.register(dispatcher));
        ServerTickEvents.END_WORLD_TICK.register(world -> RitualManager.tick(world));
        RitualDropHandler.register();
        LOGGER.info("MutantSquidMod initialized");
    }
}
