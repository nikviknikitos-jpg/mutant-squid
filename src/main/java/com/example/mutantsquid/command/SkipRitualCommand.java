package com.example.mutantsquid.command;

import com.example.mutantsquid.RitualManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

public class SkipRitualCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("skipritual").executes(ctx -> {
            ServerCommandSource src = ctx.getSource();
            ServerPlayerEntity player = src.getPlayer();
            if (player != null) {
                RitualManager.skipAll((net.minecraft.server.world.ServerWorld) player.getWorld());
                src.sendFeedback(() -> Text.literal("Ritual skipped to last 3 seconds."), false);
                return 1;
            }
            return 0;
        }));
    }
}
