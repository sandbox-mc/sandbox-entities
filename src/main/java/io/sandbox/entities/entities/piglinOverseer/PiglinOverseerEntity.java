package io.sandbox.entities.entities.piglinOverseer;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.sandbox.entities.entities.IAnimationTriggers;
import io.sandbox.entities.goals.AnimatedMeleeAttackGoal;
import io.sandbox.entities.goals.AoeShoutGoal;
import io.sandbox.entities.goals.IAoeShoutGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class PiglinOverseerEntity extends HostileEntity implements GeoEntity, IAnimationTriggers, IAoeShoutGoal {
  private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
  private static final TrackedData<Integer> MAIN_ATTACK_PROGRESS = DataTracker.registerData(
    PiglinOverseerEntity.class,
    TrackedDataHandlerRegistry.INTEGER
  );
  private static final TrackedData<Integer> MAIN_ATTACK_COOLDOWN = DataTracker.registerData(
    PiglinOverseerEntity.class,
    TrackedDataHandlerRegistry.INTEGER
  );
  private static final TrackedData<Integer> SHOUT_ANIMATION = DataTracker.registerData(
    PiglinOverseerEntity.class,
    TrackedDataHandlerRegistry.INTEGER
  );

  public int mainAttackCooldown = 60;
  public int mainAttackFullAnimation = 40;
  public int mainAttackTicksUntilDamage = 24;

  public static int shoutRadius = 12;
  public static int shoutTriggerEffectTick = 30;
  public static int shoutAnimationTicks = 45;
  public static int shoutCooldown = 20 * 20; // 20 seconds

  public PiglinOverseerEntity(EntityType<? extends HostileEntity> entityType, World world) {
    super(entityType, world);
  }

  public static DefaultAttributeContainer.Builder setAttributes() {
    return MobEntity.createMobAttributes()
      .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
      .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
      .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1)
      .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.7);
  }

  @Override
  @Nullable
  public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      this.initEquipment(world.getRandom(), difficulty);
      super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

      // Make sure they are right handed for the animations
      this.setLeftHanded(false);

      // The entityData is all passByReference... so... w/e
      return entityData;
  }

  @Override
  protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
    this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_AXE));
  }
  
  @Override
  protected void initGoals() {
    // Target Selectors
    this.targetSelector.add(1, new RevengeGoal(this, new Class[0]));
    this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>((MobEntity)this, PlayerEntity.class, false));
    this.targetSelector.add(3, new ActiveTargetGoal<MerchantEntity>((MobEntity)this, MerchantEntity.class, false));
    this.targetSelector.add(3, new ActiveTargetGoal<IronGolemEntity>((MobEntity)this, IronGolemEntity.class, true));

    // Goal Selectors
    this.goalSelector.add(1, new AoeShoutGoal(this));
    this.goalSelector.add(2, new AnimatedMeleeAttackGoal(this, 0.5, false));
    // this.goalSelector.add(7, new WanderAroundGoal(this, 0.2));
    this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
    this.goalSelector.add(8, new LookAroundGoal(this));
  }

  @Override
  protected void initDataTracker() {
    super.initDataTracker();
    // initialize to animation at it's finished state
    this.dataTracker.startTracking(MAIN_ATTACK_PROGRESS, mainAttackFullAnimation + 1);
    this.dataTracker.startTracking(MAIN_ATTACK_COOLDOWN, mainAttackCooldown);
    this.dataTracker.startTracking(SHOUT_ANIMATION, shoutAnimationTicks + 1);
  }

  @Override
  public void tick() {
    super.tick();

    int attackCooldown = this.getMainAttackCooldown();
    int shoutTicks = this.getShoutAnimation();
 
    // Main Attack Logic Start
    // Decrement Cooldown
    // Don't if Shout animation is active
    if (attackCooldown > 0 && shoutTicks > shoutAnimationTicks) {
      this.setMainAttackCooldown(Math.max(attackCooldown - 1, 0));
    }
    
    int attackProgress = this.getMainAttackProgress();
    if (attackProgress <= mainAttackFullAnimation) {
      if (attackProgress == mainAttackTicksUntilDamage) {
        LivingEntity target = this.getTarget();
        if (target != null && !this.isDead()) {
          double currentDistance = this.getSquaredDistanceToAttackPosOf(target);

          // Added 2 to the max range... Feels decent
          double maxDistance = this.getWidth() * 2.0f * (this.getWidth() * 2.0f) + target.getWidth() + 8;
          if (currentDistance <= maxDistance) {
            this.tryAttack(target);
          }
        }
      }
      
      this.setMainAttackProgress(attackProgress + 1);
    }
    // Main Attack Logic End

    // Shout Logic Start
    if (shoutTicks <= shoutAnimationTicks) {
      if (shoutTicks >= shoutTriggerEffectTick) {
        // Trigger Effects here
        // If someone enters the radius in the 0.5 seconds left, they will get the effect
        Box box = new Box(this.getBlockPos()).expand(shoutRadius);
        List<PlayerEntity> playerList = world.getNonSpectatingEntities(PlayerEntity.class, box);
        List<AbstractPiglinEntity> piglinList = world.getNonSpectatingEntities(AbstractPiglinEntity.class, box);

        for (PlayerEntity player : playerList) {
          player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10 * 20, 0), this);
        }

        for (AbstractPiglinEntity piglin : piglinList) {
          piglin.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 10 * 20, 0), this);
        }

        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 10 * 20, 0), this);
      }

      // Increment the shout Animation ticks if still active
      this.setShoutAnimation(shoutTicks + 1);
    }
  }

  // Movement and Idle animations here
  private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> tAnimationState) {
    // Shout Animation
    if (this.getShoutAnimation() < shoutAnimationTicks) {
      tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.piglin_overseer.shout", Animation.LoopType.LOOP));
      return PlayState.CONTINUE;
    }

    // Basic Attack Animation
    if (this.getMainAttackProgress() < this.mainAttackFullAnimation) {
      tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.piglin_overseer.attack", Animation.LoopType.LOOP));
      return PlayState.CONTINUE;
    }

    // Run Animation (for chasing?)
    if (tAnimationState.isMoving()) {
      tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.piglin_overseer.run", Animation.LoopType.LOOP));
      return PlayState.CONTINUE;
    }

    tAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.piglin_overseer.idle", Animation.LoopType.LOOP));
    return PlayState.CONTINUE;
  }
  
  @Override
  public void registerControllers(ControllerRegistrar controllerRegistrar) {
    controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }

  // Piglin Sounds
  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_PIGLIN_BRUTE_DEATH;
  }

  @Override
  protected void playStepSound(BlockPos pos, BlockState state) {
    this.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_STEP, 0.15f, 1.0f);
  }

  public void playAngrySound() {
    this.playSound(SoundEvents.ENTITY_PIGLIN_BRUTE_ANGRY, 1.0f, this.getSoundPitch());
  }

  public int getMainAttackCooldown() {
    return this.dataTracker.get(MAIN_ATTACK_COOLDOWN);
  }

  public void setMainAttackCooldown(int tick) {
    this.dataTracker.set(MAIN_ATTACK_COOLDOWN, tick);
  }

  public void triggerAnimatedAttack() {
    setMainAttackCooldown(40);
    setMainAttackProgress(0);
  }

  // Basic Attack Getter/Setters
  public int getMainAttackProgress () {
    return this.dataTracker.get(MAIN_ATTACK_PROGRESS);
  }

  public void setMainAttackProgress (int tick) {
    this.dataTracker.set(MAIN_ATTACK_PROGRESS, tick);
  }

  // Special Attack Getter/Setters
  public Integer getShoutAnimation() {
    return this.dataTracker.get(SHOUT_ANIMATION);
  }

  public void setShoutAnimation(Integer tickCount) {
    this.dataTracker.set(SHOUT_ANIMATION, tickCount);
  }

  public int triggerShoutAndSetCooldown() {
    // Set animation as active with 0 progress
    this.setShoutAnimation(0);

    // Return cooldown to prevent Goal from kicking off again
    return 30 + (20 * 20) ;
  }
}
