package io.sandbox.entities.entities;

import io.sandbox.entities.Main;
import io.sandbox.entities.entities.piglinOverseer.PiglinOverseerEntity;
import io.sandbox.entities.entities.piglinOverseer.PiglinOverseerRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityLoader {
  public static final EntityType<PiglinOverseerEntity> PIGLIN_OVERSEER = Registry.register(
    Registries.ENTITY_TYPE,
    Main.id("piglin_overseer"),
    FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, PiglinOverseerEntity::new)
    .dimensions(
      EntityDimensions.fixed(1f, 2.6f)
    ).build()
  );

  public static void init() {
    FabricDefaultAttributeRegistry.register(PIGLIN_OVERSEER, PiglinOverseerEntity.setAttributes());
  }

  public static void initClient() {
    EntityRendererRegistry.register(PIGLIN_OVERSEER, PiglinOverseerRenderer::new);
  }
}
