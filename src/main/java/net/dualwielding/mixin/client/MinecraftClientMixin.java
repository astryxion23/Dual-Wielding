package net.dualwielding.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dualwielding.access.PlayerAccess;
import net.dualwielding.network.AttackEntityPayload;
import net.dualwielding.network.PlayerAttackPacket;
import net.dualwielding.util.DualWieldingWeaponHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow
    public LocalPlayer player;
    @Shadow
    public MultiPlayerGameMode gameMode;
    @Shadow
    public HitResult hitResult;
    @Shadow
    private int rightClickDelay;
    @Unique
    private int secondAttackCooldown;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"))
    public void tickMixin(CallbackInfo info) {
        if (this.secondAttackCooldown > 0) {
            --this.secondAttackCooldown;
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "HEAD"), cancellable = true)
    private void doItemUseMixin(CallbackInfo info) {
        if (this.player == null || this.gameMode == null) {
            return;
        }
        if (!DualWieldingWeaponHelper.isMeleeWeapon(player.getOffhandItem()) || !DualWieldingWeaponHelper.isMeleeWeapon(player.getMainHandItem())) {
            return;
        }
        if (!PlayerAttackPacket.medievalWeaponsDoubleHanded(player.getOffhandItem(), player.getMainHandItem().getItem())) {
            return;
        }

        if (this.secondAttackCooldown <= 0) {
            if (this.hitResult != null && !this.player.isPassenger()) {
                switch (this.hitResult.getType()) {
                case ENTITY:
                    ((PlayerAccess) player).resetLastDualOffhandAttackTicks();
                    ((PlayerAccess) this.player).attackOffhand(((EntityHitResult) this.hitResult).getEntity());

                    if (Minecraft.getInstance().getConnection() != null) {
                        Minecraft.getInstance().getConnection().send(new AttackEntityPayload(((EntityHitResult) this.hitResult).getEntity().getId()));
                    }
                    break;
                case BLOCK:
                    BlockHitResult blockHitResult = (BlockHitResult) this.hitResult;
                    BlockPos blockPos = blockHitResult.getBlockPos();
                    if (!player.level().getBlockState(blockPos).isAir()) {
                        this.gameMode.useItemOn(player, InteractionHand.OFF_HAND, blockHitResult);
                        break;
                    }
                case MISS:
                    if (!this.player.getAbilities().instabuild) {
                        this.secondAttackCooldown = 10;
                    }
                    ((PlayerAccess) player).resetLastDualOffhandAttackTicks();
                }
                this.rightClickDelay = 4;
                this.player.swing(InteractionHand.OFF_HAND);
                info.cancel();
            }
        } else {
            info.cancel();
        }
    }

}
