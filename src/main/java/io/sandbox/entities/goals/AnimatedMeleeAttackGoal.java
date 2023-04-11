package io.sandbox.entities.goals;

import io.sandbox.entities.entities.IAnimationTriggers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;

public class AnimatedMeleeAttackGoal extends MeleeAttackGoal {
  public AnimatedMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
    super(mob, speed, pauseWhenMobIdle);
  }

  @Override
  public boolean canStart() {
    IAnimationTriggers aniMob = (IAnimationTriggers)this.mob;
    if (aniMob.getMainAttackCooldown() <= 0) {
      return super.canStart();
    }

    return false;
  }

  @Override
  protected void attack(LivingEntity target, double squaredDistance) {
    double d = this.getSquaredMaxAttackDistance(target);
    IAnimationTriggers aniMob = (IAnimationTriggers)this.mob;

    if (squaredDistance <= d && aniMob.getMainAttackCooldown() <= 0) {
      this.resetCooldown();
      aniMob.triggerAnimatedAttack();
      this.mob.swingHand(Hand.MAIN_HAND);

      // Stop naviation to prevent spinning or moving while swinging
      this.mob.getNavigation().stop();
      this.mob.getLookControl().lookAt(target,180,180);
      this.mob.setBodyYaw(this.mob.getHeadYaw());
    }
  }

  @Override
  protected int getMaxCooldown() {
    double attackSpeed = (1 / this.mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED)) * 20 + 20;
    return this.getTickCount((int)attackSpeed);
  }

  @Override
  protected double getSquaredMaxAttackDistance(LivingEntity entity) {
    return this.mob.getWidth() * 2.0f * (this.mob.getWidth() * 2.0f) + entity.getWidth();
  }
}
