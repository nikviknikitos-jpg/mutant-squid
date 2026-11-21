package com.example.mutantsquid;

import com.example.mutantsquid.entity.RitualShellEntity;
import com.example.mutantsquid.entity.TentacleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RitualManager {
    public static final List<RitualShellEntity> ACTIVE = new ArrayList<>();

    public static void tick(ServerWorld world) {
        ACTIVE.removeIf(e -> !e.isAlive());
        for (RitualShellEntity shell : new ArrayList<>(ACTIVE)) {
            if (shell.getWorld() != world) continue;
            shell.tickRitual();
        }
    }

    public static void skipAll(ServerWorld world) {
        for (RitualShellEntity shell : ACTIVE) {
            if (shell.getWorld() == world) shell.skipToEnd();
        }
    }

    public static int countMobsNear(ServerWorld world, double x, double y, double z, double radius) {
        Box box = new Box(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        return (int) world.getEntitiesByClass(Entity.class, box, e -> !(e instanceof net.minecraft.entity.player.PlayerEntity) && e.isAlive() && !(e instanceof TentacleEntity) && !(e instanceof RitualShellEntity)).size();
    }
}
