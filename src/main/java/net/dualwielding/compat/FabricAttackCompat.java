package net.dualwielding.compat;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Replaces NeoForge {@code CommonHooks} / {@code EventHooks} calls used by off-hand attacks.
 */
public final class FabricAttackCompat {

    private FabricAttackCompat() {
    }

    public static boolean onPlayerAttackTarget(Player player, Entity target) {
        InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.OFF_HAND, target, null);
        return result == InteractionResult.PASS;
    }

    public static CritResult fireCriticalHit(Player player, Entity target, boolean vanillaCrit, float vanillaDamageMultiplier) {
        return new CritResult(vanillaCrit, vanillaDamageMultiplier, false);
    }

    public static SweepResult fireSweepAttack(Player player, Entity target, boolean vanillaSweep) {
        return new SweepResult(vanillaSweep);
    }

    public static Entity resolveMultipart(Entity attacked) {
        if (attacked instanceof EnderDragonPart part) {
            return part.parentMob;
        }
        return attacked;
    }

    /**
     * Mirrors NeoForge main-hand sweep gating ({@code ItemAbilities.SWORD_SWEEP}) using vanilla item tags.
     */
    public static boolean canPerformSwordSweep(ItemStack weapon) {
        return weapon.is(ItemTags.SWORDS);
    }

    public record CritResult(boolean criticalHit, float damageMultiplier, boolean disableSweep) {
    }

    public record SweepResult(boolean sweeping) {
    }
}
