package io.sandbox.entities.entities.piglinOverseer;

import io.sandbox.entities.Main;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PiglinOverseerModel extends GeoModel<PiglinOverseerEntity> {

  @Override
  public Identifier getModelResource(PiglinOverseerEntity animatable) {
    return Main.id("geo/piglin_overseer.geo.json");
  }

  @Override
  public Identifier getTextureResource(PiglinOverseerEntity animatable) {
    return Main.id("textures/entity/piglin_overseer.png");
  }

  @Override
  public Identifier getAnimationResource(PiglinOverseerEntity animatable) {
    return Main.id("animations/piglin_overseer.animation.json");
  }
}
