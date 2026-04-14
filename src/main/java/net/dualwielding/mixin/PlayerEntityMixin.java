package net.dualwielding.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dualwielding.access.PlayerAccess;
import net.dualwielding.util.DualWieldingOffhandAttack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements PlayerAccess {

    @Unique
    private int lastAttackedOffhandTicks;

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I", ordinal = 0))
    private void tickMixin(CallbackInfo info) {
        lastAttackedOffhandTicks++;
    }

    public float getOffhandAttackCooldownProgressPerTick() {
        return DualWieldingOffhandAttack.getOffhandAttackCooldownProgressPerTick((Player) (Object) this);
    }

    @Override
    public void attackOffhand(Entity target) {
        DualWieldingOffhandAttack.offhandAttack((Player) (Object) this, target);
    }

    @Override
    public void resetLastDualOffhandAttackTicks() {
        this.lastAttackedOffhandTicks = 0;
    }

    @Override
    public float getAttackCooldownProgressDualOffhand(float baseTime) {
        return Mth.clamp(((float) this.lastAttackedOffhandTicks + baseTime) / this.getOffhandAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

}
