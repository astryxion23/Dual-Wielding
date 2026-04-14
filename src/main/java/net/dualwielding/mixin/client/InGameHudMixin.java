package net.dualwielding.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At.Shift;

import net.dualwielding.access.PlayerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    @Mutable
    protected Minecraft minecraft;

    private static final ResourceLocation CROSS_HAIR_TEXTURE = new ResourceLocation("dualwielding", "textures/gui/crosshair_indicator.png");
    private static final ResourceLocation HOTBAR_INDICATOR_TEXTURE = new ResourceLocation("dualwielding", "textures/gui/crosshair_indicator.png");

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F", shift = Shift.AFTER, ordinal = 0))
    private void renderCrosshairMixinTEST(GuiGraphics context, CallbackInfo info) {
        float o = ((PlayerAccess) this.minecraft.player).getAttackCooldownProgressDualOffhand(1.0F);
        if (o < 1.0F) {
            int u = (int) (o * 17.0F);
            int sw = this.minecraft.getWindow().getGuiScaledWidth();
            int sh = this.minecraft.getWindow().getGuiScaledHeight();
            context.blit(CROSS_HAIR_TEXTURE, sw / 2 - 8, sh / 2 - 7 + 16, 0.0F, 0.0F, u, 4, 16, 16);
        }
    }

    @Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F", shift = Shift.AFTER, ordinal = 0))
    private void renderHotbar(float tickDelta, GuiGraphics context, CallbackInfo info) {
        float o = ((PlayerAccess) this.minecraft.player).getAttackCooldownProgressDualOffhand(1.0F);
        if (o < 1.0F) {
            HumanoidArm arm = this.minecraft.player.getMainArm().getOpposite();
            int sw = this.minecraft.getWindow().getGuiScaledWidth();
            int sh = this.minecraft.getWindow().getGuiScaledHeight();
            int r = (sw / 2) + 91 + 6;
            if (arm == HumanoidArm.RIGHT) {
                r = (sw / 2) - 91 - 22;
            }
            int s = (int) (o * 19.0F);
            context.blit(HOTBAR_INDICATOR_TEXTURE, r, sh - 20 + 18 - s, 0.0F, 18.0F - s, 18, s, 32, 32);
        }
    }

}
