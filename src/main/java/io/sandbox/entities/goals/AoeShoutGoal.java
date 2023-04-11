package io.sandbox.entities.goals;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

public class AoeShoutGoal extends Goal {
  protected final PathAwareEntity mob;
  private int cooldown = 0;

  public AoeShoutGoal(PathAwareEntity mob) {
    this.mob = mob;
  }

  @Override
  public boolean canStart() {
    // Cooldown is up and health is less than half
    if (cooldown == 0 && this.mob.getHealth() < Math.floor(this.mob.getMaxHealth() / 2)) {
      return true;
    }

    return false;
  }
  
  @Override
  public void tick() {
    if (this.cooldown > 0) {
      this.cooldown--;
    } else if (this.canStart()) {
      // Trigger Shout and set cooldown
      IAoeShoutGoal aniMob = (IAoeShoutGoal)this.mob;
      this.cooldown = aniMob.triggerShoutAndSetCooldown();
    }
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }
}
