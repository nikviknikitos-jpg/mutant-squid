package com.example.mutantsquid;

import com.example.mutantsquid.entity.RitualShellEntity;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;

import java.util.List;

public class RitualDropHandler {
    public static void register() {
        // Detected via tick scan of dropped nautilus shells.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % 5 != 0) return;
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class,
                    new Box(-30000000, world.getBottomY(), -30000000, 30000000, world.getTopY(), 30000000),
                    i -> i.getStack().isOf(Items.NAUTILUS_SHELL) && !i.isRemoved());
            for (ItemEntity item : items) {
                if (item.getOwner() == null && item.age > 20 && !item.getStack().isEmpty()) {
                    if (RitualShellEntity.spawnFrom(world, item)) {
                        // consumed
                    }
                }
            }
        });
    }
}
