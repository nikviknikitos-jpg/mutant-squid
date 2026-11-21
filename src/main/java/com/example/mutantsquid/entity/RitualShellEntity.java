package com.example.mutantsquid.entity;

import com.example.mutantsquid.RitualManager;
import com.example.mutantsquid.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RitualShellEntity extends Entity {
    private static final TrackedData<Integer> TIMER = DataTracker.registerData(RitualShellEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int RITUAL_TICKS = 6000;
    private boolean struck = false;

    public RitualShellEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public static boolean spawnFrom(ServerWorld world, ItemEntity item) {
        RitualShellEntity shell = ModEntities.RITUAL_SHELL.create(world);
        if (shell == null) return false;
        shell.refreshPositionAndAngles(item.getX(), item.getY() + 0.05, item.getZ(), 0, 0);
        shell.dataTracker.set(TIMER, RITUAL_TICKS);
        world.spawnEntity(shell);
        item.discard();
        world.playSound(null, shell.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.AMBIENT, 1.0f, 1.0f);
        try {
            var sound = net.minecraft.registry.Registries.SOUND_EVENT.get(new Identifier("mutantsquid", "ritual_song"));
            if (sound != null) {
                world.playSound(null, shell.getBlockPos(), sound, SoundCategory.AMBIENT, 2.0f, 1.0f);
            }
        } catch (Exception ignored) {}
        RitualManager.ACTIVE.add(shell);
        return true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TIMER, RITUAL_TICKS);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            for (int i = 0; i < 2; i++) {
                getWorld().addParticle(ParticleTypes.BUBBLE_POP,
                        getX() + (random.nextDouble() - 0.5) * 1.2,
                        getY() + random.nextDouble() * 1.5,
                        getZ() + (random.nextDouble() - 0.5) * 1.2,
                        0, 0.05, 0);
            }
        }
    }

    public void tickRitual() {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        int t = dataTracker.get(TIMER);
        if (t > 0) {
            t--;
            dataTracker.set(TIMER, t);
            if (t <= 60 && !struck) {
                struck = true;
                var lightning = EntityType.LIGHTNING_BOLT.create(sw);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(getX(), getY(), getZ());
                    sw.spawnEntity(lightning);
                }
                sw.playSound(null, getBlockPos(), • SoundEvents.ENTITY_GENERIC_EXPLODE
, SoundCategory.HOSTILE, 2.0f, 1.0f);
                this.discard();
                spawnTentacles(sw);
            }
        }
    }

    public void skipToEnd() {
        if (dataTracker.get(TIMER) > 60) {
            dataTracker.set(TIMER, 60);
        }
    }

    private void spawnTentacles(ServerWorld sw) {
        for (int i = 0; i < 4; i++) {
            double angle = Math.PI * 2 * i / 4.0;
            double x = getX() + Math.cos(angle) * 4;
            double z = getZ() + Math.sin(angle) * 4;
            TentacleEntity tent = ModEntities.TENTACLE.create(sw);
            if (tent != null) {
                tent.refreshPositionAndAngles(x, getY(), z, (float) Math.toDegrees(-angle), 0);
                tent.setHome(getBlockPos(), i);
                sw.spawnEntity(tent);
            }
        }
        sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 2.0f, 0.7f);
    }

    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
}
