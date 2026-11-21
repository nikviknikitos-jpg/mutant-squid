package com.example.mutantsquid.registry;

import com.example.mutantsquid.MutantSquidMod;
import com.example.mutantsquid.entity.MutantSquidEntity;
import com.example.mutantsquid.entity.RitualShellEntity;
import com.example.mutantsquid.entity.TentacleEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static EntityType<RitualShellEntity> RITUAL_SHELL;
    public static EntityType<TentacleEntity> TENTACLE;
    public static EntityType<MutantSquidEntity> MUTANT_SQUID;

    public static void register() {
        RITUAL_SHELL = Registry.register(Registries.ENTITY_TYPE,
                new Identifier(MutantSquidMod.MODID, "ritual_shell"),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, RitualShellEntity::new)
                        .dimensions(EntityDimensions.fixed(1.0f, 1.0f)).build());
        TENTACLE = Registry.register(Registries.ENTITY_TYPE,
                new Identifier(MutantSquidMod.MODID, "tentacle"),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, TentacleEntity::new)
                        .dimensions(EntityDimensions.fixed(1.0f, 4.0f)).build());
        MUTANT_SQUID = Registry.register(Registries.ENTITY_TYPE,
                new Identifier(MutantSquidMod.MODID, "mutant_squid"),
                FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MutantSquidEntity::new)
                        .dimensions(EntityDimensions.fixed(5.0f, 5.0f)).build());
    }
}
