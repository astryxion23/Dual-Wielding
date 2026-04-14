package net.dualwielding.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At.Shift;

import net.dualwielding.util.DualWieldingWeaponHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "swing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;)V", shift = Shift.AFTER), cancellable = true)
    private void swingHandMixin(InteractionHand hand, CallbackInfo info) {
        Player self = (Player) (Object) this;
        if (hand == InteractionHand.OFF_HAND && DualWieldingWeaponHelper.isMeleeWeapon(self.getOffhandItem())) {
            info.cancel();
        }
    }

}
