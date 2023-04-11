package io.sandbox.entities.entities.piglinOverseer;

import org.jetbrains.annotations.Nullable;

import io.sandbox.entities.Main;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class PiglinOverseerRenderer extends DynamicGeoEntityRenderer<PiglinOverseerEntity> {
  private static final String LEFT_HAND = "hand_left";
  private static final String RIGHT_HAND = "hand_right";

  protected ItemStack mainHandItem;
  protected ItemStack offhandItem;

  public PiglinOverseerRenderer(Context renderManager) {
    super(renderManager, new PiglinOverseerModel());
    this.addRenderLayer(
      new BlockAndItemGeoLayer<>(this) {
        @Nullable
        @Override
        protected ItemStack getStackForBone(GeoBone bone, PiglinOverseerEntity animatable) {
          // Retrieve the items in the entity's hands for the relevant bone
          return switch (bone.getName()) {
            case LEFT_HAND -> animatable.isLeftHanded() ?
                PiglinOverseerRenderer.this.mainHandItem : PiglinOverseerRenderer.this.offhandItem;
            case RIGHT_HAND -> animatable.isLeftHanded() ?
                PiglinOverseerRenderer.this.offhandItem : PiglinOverseerRenderer.this.mainHandItem;
            default -> null;
          };
        }

        @Override
        protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, PiglinOverseerEntity animatable) {
          // Apply the camera transform for the given hand
          return switch (bone.getName()) {
          	case LEFT_HAND, RIGHT_HAND -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
          	default -> ModelTransformationMode.NONE;
          };
        }

        @Override
        protected void renderStackForBone(
          MatrixStack poseStack,
          GeoBone bone,
          ItemStack stack,
          PiglinOverseerEntity animatable,
          VertexConsumerProvider bufferSource,
          float partialTick,
          int packedLight,
          int packedOverlay
        ) {
          if (stack == PiglinOverseerRenderer.this.mainHandItem) {
            poseStack.scale(2.0f, 2.0f, 2.0f);
          }

          super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
        }
      }
    );
  }
  
  @Override
  public Identifier getTextureLocation(PiglinOverseerEntity animatable) {
    return Main.id("textures/entity/piglin_overseer.png");
  }

  // @Override
  // public void render(
  //   PiglinOverseerEntity entity,
  //   float entityYaw,
  //   float partialTick,
  //   MatrixStack poseStack,
  //   VertexConsumerProvider bufferSource,
  //   int packedLight
  // ) {
  //     if(entity.isBaby()) {
  //         poseStack.scale(0.4f, 0.4f, 0.4f);
  //     }

  //     super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  // }

  @Override
  public void preRender(
    MatrixStack poseStack,
    PiglinOverseerEntity animatable,
    BakedGeoModel model,
    VertexConsumerProvider bufferSource,
    VertexConsumer buffer,
    boolean isReRender,
    float partialTick,
    int packedLight,
    int packedOverlay,
    float red,
    float green,
    float blue,
    float alpha
  ) {
    super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    
    this.mainHandItem = animatable.getMainHandStack();
    this.offhandItem = animatable.getOffHandStack();
  }
}
