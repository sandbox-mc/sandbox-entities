package io.sandbox.entities.items;

import io.sandbox.entities.Main;
import io.sandbox.entities.entities.EntityLoader;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemLoader {
  public static final Item PIGLIN_OVERSEER_EGG = new SpawnEggItem(
    EntityLoader.PIGLIN_OVERSEER,
    0x948e8d,
    0x3b3635,
    new FabricItemSettings().maxCount(64)
  );


  public static void init() {
    Registry.register(
      Registries.ITEM,
      Main.id("piglin_overseer_spawn_egg"),
      PIGLIN_OVERSEER_EGG
    );

    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
      content.add(PIGLIN_OVERSEER_EGG);
    });
  }
}
