package net.dualwielding.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dualwielding.access.PlayerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    private float offHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    @Final
    @Mutable
    private Minecraft minecraft;

    @Shadow
    private ItemStack offHandItem;

    private float equipOffhand;
    private boolean isOffhandAttack;

    @Inject(method = "tick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 3, shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void updateHeldItemsMixin(CallbackInfo info, LocalPlayer localplayer, ItemStack itemstack, ItemStack itemstack1) {
        float o = ((PlayerAccess) localplayer).getAttackCooldownProgressDualOffhand(1.0F);
        if (o < 0.1F)
            this.isOffhandAttack = true;
        if (this.isOffhandAttack) {
            if (this.mainHandHeight >= 1.0F) {
                this.isOffhandAttack = false;
            }
            this.equipOffhand += Mth.clamp((this.offHandItem == itemstack1 ? o * o * o : 0.0F) - this.equipOffhand, -0.4F, 0.4F);
            this.offHandHeight = this.equipOffhand;
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
    public void updateHeldMainhandMixin(CallbackInfo info) {
        float o = ((PlayerAccess) minecraft.player).getAttackCooldownProgressDualOffhand(1.0F);
        if (o < 0.9F && o > 0.15F) {
            this.offHandItem = new ItemStack(Items.AIR);
        }
    }

}
