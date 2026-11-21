package com.example.mutantsquid.entity;

import com.example.mutantsquid.registry.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class TentacleEntity extends Entity {
    private BlockPos home = BlockPos.ORIGIN;
    private int index = 0;
    private boolean consumed = false;
    private int idleTicks = 0;
    private LivingEntity grabbed = null;
    private int grabTicks = 0;

    public TentacleEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public void setHome(BlockPos pos, int idx) { this.home = pos; this.index = idx; }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            for (int i = 0; i < 3; i++) {
                getWorld().addParticle(ParticleTypes.SQUID_INK,
                        getX() + (random.nextDouble() - 0.5),
                        getY() + random.nextDouble() * 4,
                        getZ() + (random.nextDouble() - 0.5), 0, 0.02, 0);
            }
            return;
        }
        if (consumed) {
            if (getWorld() instanceof ServerWorld sw && random.nextInt(20) == 0) {
                sw.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 1, getZ(), 3, 0.3, 0.3, 0.3, 0.01);
            }
            return;
        }
        if (grabbed != null) {
            grabTicks++;
            grabbed.setPosition(getX(), getY() + 1.0 + (grabTicks / 20.0), getZ());
            grabbed.setVelocity(0, 0, 0);
            if (grabTicks >= 40) {
                if (getWorld() instanceof ServerWorld sw) {
                    sw.playSound(null, getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EAT, net.minecraft.sound.SoundCategory.HOSTILE, 2f, 0.7f);
                    sw.playSound(null, getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_SQUID_SQUIRT, net.minecraft.sound.SoundCategory.HOSTILE, 2f, 0.7f);
                    sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 2, getZ(), 20, 0.5, 0.5, 0.5, 0.02);
                }
                grabbed.discard();
                grabbed = null;
                consumed = true;
                checkAllConsumed();
            }
            return;
        }
        idleTicks++;
        if (idleTicks % 10 == 0) {
            tryGrab();
        }
    }

    private void tryGrab() {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        Box box = new Box(getX() - 10, getY() - 3, getZ() - 10, getX() + 10, getY() + 6, getZ() + 10);
        List<MobEntity> mobs = sw.getEntitiesByClass(MobEntity.class, box,
                e -> e.isAlive() && !(e instanceof PlayerEntity) && e != this && !(e instanceof TentacleEntity));
        // order by distance, ensure each tentacle picks from queue
        Optional<MobEntity> pick = mobs.stream()
                .filter(m -> m.getUuid().hashCode() % 4 == index || mobs.size() < 4)
                .findFirst();
        if (pick.isPresent()) {
            grabbed = pick.get();
            grabTicks = 0;
            sw.playSound(null, getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_SQUID_AMBIENT, net.minecraft.sound.SoundCategory.HOSTILE, 1.5f, 0.6f);
        }
    }

    private void checkAllConsumed() {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        List<TentacleEntity> tents = sw.getEntitiesByClass(TentacleEntity.class,
                new Box(home).expand(20), e -> true);
        long consumedCount = tents.stream().filter(t -> t.consumed).count();
        if (consumedCount >= 4) {
            for (TentacleEntity t : tents) {
                if (!t.consumed) t.consumed = true;
                t.discardLater();
            }
            // spawn boss
            var lightning = EntityType.LIGHTNING_BOLT.create(sw);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
                sw.spawnEntity(lightning);
            }
            MutantSquidEntity boss = ModEntities.MUTANT_SQUID.create(sw);
            if (boss != null) {
                boss.refreshPositionAndAngles(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0, 0);
                sw.spawnEntity(boss);
                List<PlayerEntity> players = sw.getEntitiesByClass(PlayerEntity.class,
                        new Box(home).expand(30), e -> true);
                for (PlayerEntity p : players) {
                    p.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                            net.minecraft.entity.effect.StatusEffects.DARKNESS, 20 * 20, 0));
                }
            }
        }
    }

    private void discardLater() {
        this.consumed = true;
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1, getZ(), 20, 0.4, 0.4, 0.4, 0.02);
        }
        this.discard();
    }

    @Override public boolean canHit() { return false; }
    @Override protected void initDataTracker() {}
    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
}
