package com.example.mutantsquid.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class MutantSquidEntity extends HostileEntity {
    public final float[] legPhase = new float[8];
    public final float[] legLift = new float[8];
    public final float[] armPhase = new float[2];
    public float bodyBob = 0;
    public float headYaw = 0;
    public float headPitch = 0;
    private int stepTimer = 0;
    private int grabCooldown = 0;
    private LivingEntity grabTarget = null;
    private int grabTime = 0;
    private float grabProgress = 0;
    private final Random rng = new Random();

    public MutantSquidEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 500;
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_ARMOR, 8.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 32));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            clientAnimate();
            return;
        }
        serverAnimate();
        LivingEntity target = getTarget();
        if (target instanceof PlayerEntity) {
            // stay locked on player
            getLookControl().lookAt(target, 30, 30);
        }
        // parallel eating
        if (grabCooldown > 0) grabCooldown--;
        if (grabTarget != null) {
            updateGrab();
        } else if (grabCooldown == 0) {
            tryGrabRandomMob();
        }
    }

    private void clientAnimate() {
        bodyBob = MathHelper.sin(this.age * 0.15f) * 0.15f;
        headYaw = MathHelper.lerp(0.1f, headYaw, (float)(this.getVelocity().horizontalLength() * 200.0));
        headPitch = MathHelper.lerp(0.1f, headPitch, (float)(this.getVelocity().y * 100.0));
        for (int i = 0; i < 8; i++) {
            legPhase[i] += 0.08f;
            legLift[i] = Math.max(0, MathHelper.sin(legPhase[i] + i * 0.785f));
        }
        for (int i = 0; i < 2; i++) {
            armPhase[i] += 0.06f;
        }
        if (grabTarget != null) {
            grabProgress = Math.min(1f, grabProgress + 0.05f);
        } else {
            grabProgress = Math.max(0f, grabProgress - 0.1f);
        }
    }

    private void serverAnimate() {
        stepTimer++;
        double speed = this.getVelocity().horizontalLength();
        if (speed > 0.01) {
            // leg lifting handled client side but server tracks to sync body bob
            bodyBob = MathHelper.sin(stepTimer * 0.2f) * 0.12f;
        } else {
            bodyBob = 0;
        }
    }

    private void tryGrabRandomMob() {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        LivingEntity target = getTarget();
        if (target == null) return;
        Box box = new Box(getX() - 5, getY() - 1, getZ() - 5, getX() + 5, getY() + 6, getZ() + 5);
        List<MobEntity> mobs = sw.getEntitiesByClass(MobEntity.class, box,
                e -> e.isAlive() && e != this && !(e instanceof PlayerEntity) && !(e instanceof MutantSquidEntity) && !(e instanceof TentacleEntity));
        if (!mobs.isEmpty()) {
            grabTarget = mobs.get(rng.nextInt(mobs.size()));
            grabTime = 0;
            grabProgress = 0;
        }
    }

    private void updateGrab() {
        if (!(getWorld() instanceof ServerWorld sw) || grabTarget == null) { grabTarget = null; return; }
        grabTime++;
        Vec3d mouth = new Vec3d(getX(), getY() + 2.5, getZ());
        Vec3d mobPos = grabTarget.getPos();
        Vec3d dir = mouth.subtract(mobPos).normalize();
        double dist = mobPos.distanceTo(mouth);
        if (dist > 0.8) {
            Vec3d newPos = mobPos.add(dir.multiply(0.6));
            grabTarget.requestTeleport(newPos.x, newPos.y, newPos.z);
            grabTarget.setVelocity(0, 0.1, 0);
            if (grabTime % 10 == 0) {
                sw.spawnParticles(ParticleTypes.SQUID_INK, mobPos.x, mobPos.y + 0.5, mobPos.z, 3, 0.1, 0.1, 0.1, 0.01);
            }
        } else {
            // eaten
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.HOSTILE, 2.0f, 0.8f);
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_SQUID_SQUIRT, SoundCategory.HOSTILE, 2.0f, 0.9f);
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, mouth.x, mouth.y, mouth.z, 25, 0.3, 0.3, 0.3, 0.02);
            grabTarget.discard();
            float max = this.getMaxHealth();
            this.setHealth(Math.min(max, this.getHealth() + 15.0f));
            grabTarget = null;
            grabCooldown = 60;
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean ok = super.damage(source, amount);
        if (ok && source.getAttacker() instanceof PlayerEntity p) {
            setTarget(p);
        }
        return ok;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) { return false; }

    @Override
    public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("Persistent", true);
    }
}
