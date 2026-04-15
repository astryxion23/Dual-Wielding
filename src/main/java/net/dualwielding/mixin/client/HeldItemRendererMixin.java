package net.dualwielding.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dualwielding.access.PlayerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    @Unique
    private float equipOffhand;
    @Unique
    private boolean isOffhandAttack;

    @Inject(method = "tick", at = @At("TAIL"))
    public void dualwielding$tickOffhandAnim(CallbackInfo info) {
        LocalPlayer localplayer = this.minecraft.player;
        if (localplayer == null) {
            return;
        }
        ItemStack itemstack = localplayer.getMainHandItem();
        ItemStack itemstack1 = localplayer.getOffhandItem();

        float o = ((PlayerAccess) localplayer).getAttackCooldownProgressDualOffhand(1.0F);
        if (o < 0.1F) {
            this.isOffhandAttack = true;
        }
        if (this.isOffhandAttack) {
            if (this.mainHandHeight >= 1.0F) {
                this.isOffhandAttack = false;
            }
            this.equipOffhand += Mth.clamp((this.offHandItem == itemstack1 ? o * o * o : 0.0F) - this.equipOffhand, -0.4F, 0.4F);
            this.offHandHeight = this.equipOffhand;
        }

        float oCooldown = ((PlayerAccess) localplayer).getAttackCooldownProgressDualOffhand(1.0F);
        if (oCooldown < 0.9F && oCooldown > 0.15F) {
            this.offHandItem = new ItemStack(Items.AIR);
        }
    }

}
