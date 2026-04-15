package net.dualwielding.util;

import net.dualwielding.access.PlayerAccess;
import net.dualwielding.init.ParticleInit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;

public final class DualWieldingOffhandAttack {

    private DualWieldingOffhandAttack() {
    }

    private static double computeOffhandAttribute(Player player, Holder<Attribute> attribute) {
        AttributeInstance src = player.getAttribute(attribute);
        if (src == null) {
            return attribute.value().getDefaultValue();
        }
        AttributeInstance inst = new AttributeInstance(attribute, $ -> {});
        inst.setBaseValue(src.getBaseValue());
        for (AttributeModifier mod : src.getModifiers()) {
            inst.addTransientModifier(mod);
        }
        player.getMainHandItem().getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attr, mod) -> {
            if (attr == attribute) {
                inst.removeModifier(mod.id());
            }
        });
        player.getOffhandItem().getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, (attr, mod) -> {
            if (attr == attribute && !inst.hasModifier(mod.id())) {
                inst.addPermanentModifier(mod);
            }
        });
        return inst.getValue();
    }

    public static float getOffhandAttackCooldownProgressPerTick(Player player) {
        double speed = computeOffhandAttribute(player, Attributes.ATTACK_SPEED);
        return (float) (1.0 / speed * 20.0);
    }

    private static boolean cannotAttack(Player player, Entity target) {
        return !target.isAttackable() || target.skipAttackInteraction(player);
    }

    private static boolean deflectProjectile(Player player, Entity target) {
        if (target.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                && target instanceof Projectile projectile
                && projectile.deflect(ProjectileDeflection.AIM_DEFLECT, player, null, true)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource());
            return true;
        }
        return false;
    }

    private static boolean canCriticalAttack(Player player, Entity target) {
        return player.fallDistance > 0.0
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && !player.isPassenger()
                && target instanceof LivingEntity
                && !player.isSprinting();
    }

    private static float getOffhandKnockback(ServerPlayer player, Entity target, DamageSource damageSource, ItemStack weapon) {
        float knockback = (float) player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        return player.level() instanceof ServerLevel serverlevel
                ? EnchantmentHelper.modifyKnockback(serverlevel, weapon, target, damageSource, knockback) / 2.0F
                : knockback / 2.0F;
    }

    private static void playServerSideSound(Player player, net.minecraft.sounds.SoundEvent sound) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, player.getSoundSource(), 1.0F, 1.0F);
    }

    private static void attackVisualEffects(Player player, Entity target, boolean crit, boolean sweep, boolean strongCooldown, boolean magicOnly, float magicAmount) {
        if (crit) {
            playServerSideSound(player, SoundEvents.PLAYER_ATTACK_CRIT);
            player.crit(target);
        }

        if (!crit && !sweep && !magicOnly) {
            playServerSideSound(player, strongCooldown ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK);
        }

        if (magicAmount > 0.0F) {
            player.magicCrit(target);
        }
    }

    private static void damageStatsAndHearts(Player player, Entity target, float healthBefore) {
        if (target instanceof LivingEntity living) {
            float dealt = healthBefore - living.getHealth();
            player.awardStat(Stats.DAMAGE_DEALT, Math.round(dealt * 10.0F));
            if (player.level() instanceof ServerLevel serverLevel && dealt > 2.0F) {
                int particles = (int) (dealt * 0.5);
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getY(0.5), target.getZ(),
                        particles, 0.1, 0.0, 0.1, 0.2);
            }
        }
    }

    private static void itemAttackInteraction(ServerPlayer player, Entity attacked, ItemStack weapon, DamageSource damageSource, boolean hurtSucceeded) {
        Entity entity = attacked;
        if (attacked instanceof PartEntity<?> part) {
            entity = part.getParent();
        }

        boolean hurtEnemyReturn = false;
        ItemStack weaponCopy = weapon.copy();
        if (player.level() instanceof ServerLevel serverlevel) {
            if (entity instanceof LivingEntity livingentity) {
                hurtEnemyReturn = weapon.hurtEnemy(livingentity, player);
            }

            if (hurtSucceeded) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverlevel, attacked, damageSource, weapon);
            }
        }

        if (!player.level().isClientSide() && !weapon.isEmpty() && entity instanceof LivingEntity) {
            if (hurtEnemyReturn) {
                weapon.postHurtEnemy((LivingEntity) entity, player);
            }

            if (weapon.isEmpty()) {
                EventHooks.onPlayerDestroyItem(player, weaponCopy, InteractionHand.OFF_HAND);
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }

    private static void causeExtraKnockback(ServerPlayer player, Entity target, float knockback, Vec3 motionBefore) {
        if (knockback > 0.0F) {
            if (target instanceof LivingEntity livingentity) {
                livingentity.knockback(knockback, Mth.sin(player.getYRot() * ((float) Math.PI / 180)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180)));
            } else {
                target.push(
                        -Mth.sin(player.getYRot() * ((float) Math.PI / 180)) * knockback, 0.1, Mth.cos(player.getYRot() * ((float) Math.PI / 180)) * knockback);
            }

            player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            player.setSprinting(false);
        }

        if (target instanceof ServerPlayer serverTarget && serverTarget.hurtMarked) {
            serverTarget.connection.send(new ClientboundSetEntityMotionPacket(serverTarget));
            serverTarget.hurtMarked = false;
            serverTarget.setDeltaMovement(motionBefore);
        }
    }

    private static void doOffhandSweepAttack(ServerPlayer player, Entity primaryTarget, float attackDamageForRatio, DamageSource damageSource, float cooldownStrength, AABB sweepHitBox, ItemStack offWeapon) {
        playServerSideSound(player, SoundEvents.PLAYER_ATTACK_SWEEP);
        ServerLevel serverlevel = (ServerLevel) player.level();
        float ratio = 1.0F + (float) player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * attackDamageForRatio;
        double reachSq = Mth.square(player.entityInteractionRange());
        for (LivingEntity livingentity : player.level().getEntitiesOfClass(LivingEntity.class, sweepHitBox)) {
            if (livingentity != player
                    && livingentity != primaryTarget
                    && !player.isAlliedTo(livingentity)
                    && !(livingentity instanceof ArmorStand armorstand && armorstand.isMarker())
                    && player.distanceToSqr(livingentity) < reachSq) {
                float sweptDamage = EnchantmentHelper.modifyDamage(serverlevel, offWeapon, livingentity, damageSource, ratio) * cooldownStrength;
                if (livingentity.hurtOrSimulate(damageSource, sweptDamage)) {
                    livingentity.knockback(0.4F, Mth.sin(player.getYRot() * ((float) Math.PI / 180)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180)));
                    EnchantmentHelper.doPostAttackEffects(serverlevel, livingentity, damageSource);
                }
            }
        }

        double posOne = -Mth.sin(player.getYRot() * ((float) Math.PI / 180));
        double posTwo = Mth.cos(player.getYRot() * ((float) Math.PI / 180));
        serverlevel.sendParticles(ParticleInit.OFFHAND_SWEEPING.get(), player.getX() + posOne, player.getY(0.5D), player.getZ() + posTwo, 0, posOne, 0.0D, posTwo, 0.0D);
    }

    private static void finishAttackSwing(Player player) {
        player.resetAttackStrengthTicker();
    }

    public static void offhandAttack(Player player, Entity target) {
        if (!CommonHooks.onPlayerAttackTarget(player, target)) {
            return;
        }
        if (cannotAttack(player, target)) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) player.level();

        target.invulnerableTime = 0;

        ItemStack weapon = player.getItemInHand(InteractionHand.OFF_HAND);
        DamageSource damageSource = player.damageSources().playerAttack(player);

        float f = (float) computeOffhandAttribute(player, Attributes.ATTACK_DAMAGE);
        float f1 = ((PlayerAccess) player).getAttackCooldownProgressDualOffhand(0.5F);
        float f2 = f1 * (EnchantmentHelper.modifyDamage(serverLevel, weapon, target, damageSource, f) - f);
        f *= 0.2F + f1 * f1 * 0.8F;

        if (deflectProjectile(player, target)) {
            finishAttackSwing(player);
            return;
        }

        if (f <= 0.0F && f2 <= 0.0F) {
            finishAttackSwing(player);
            return;
        }

        boolean fullCooldown = f1 > 0.9F;
        boolean sprintKnockback;
        if (player.isSprinting() && fullCooldown) {
            playServerSideSound(player, SoundEvents.PLAYER_ATTACK_KNOCKBACK);
            sprintKnockback = true;
        } else {
            sprintKnockback = false;
        }

        f += weapon.getItem().getAttackDamageBonus(target, f, damageSource);

        boolean vanillaCrit = fullCooldown && canCriticalAttack(player, target);
        var critEvent = CommonHooks.fireCriticalHit(player, target, vanillaCrit, vanillaCrit ? 1.5F : 1.0F);
        boolean crit = critEvent.isCriticalHit();
        if (crit) {
            f *= critEvent.getDamageMultiplier();
        }

        float f3 = f + f2;

        boolean blockSweepFromCrit = critEvent.isCriticalHit() && critEvent.disableSweep();
        boolean vanillaSweep = fullCooldown && !blockSweepFromCrit && !sprintKnockback && player.onGround()
                && serverPlayer.getKnownMovement().horizontalDistanceSqr() < Mth.square(player.getSpeed() * 2.5)
                && weapon.canPerformAction(ItemAbilities.SWORD_SWEEP);
        var sweepEvent = CommonHooks.fireSweepAttack(player, target, vanillaSweep);
        boolean sweep = sweepEvent.isSweeping();

        float healthBefore = 0.0F;
        if (target instanceof LivingEntity livingBefore) {
            healthBefore = livingBefore.getHealth();
        }

        var enchantmentRegistry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        int fireAspectLevel = weapon.getEnchantmentLevel(enchantmentRegistry.getOrThrow(Enchantments.FIRE_ASPECT));
        boolean appliedShortFire = false;
        if (target instanceof LivingEntity && fireAspectLevel > 0 && !target.isOnFire()) {
            appliedShortFire = true;
            target.igniteForTicks(20);
        }

        Vec3 motionBefore = target.getDeltaMovement();
        boolean hurt = target.hurtOrSimulate(damageSource, f3);

        if (hurt) {
            float knockback = getOffhandKnockback(serverPlayer, target, damageSource, weapon) + (sprintKnockback ? 0.5F : 0.0F);
            causeExtraKnockback(serverPlayer, target, knockback, motionBefore);

            if (sweep) {
                AABB sweepHitBox = weapon.getSweepHitBox(player, target);
                float attackDamageForSweep = (float) computeOffhandAttribute(player, Attributes.ATTACK_DAMAGE);
                doOffhandSweepAttack(serverPlayer, target, attackDamageForSweep, damageSource, f1, sweepHitBox, weapon);
            }

            attackVisualEffects(player, target, crit, sweep, fullCooldown, false, f2);
            player.setLastHurtMob(target);
            itemAttackInteraction(serverPlayer, target, weapon, damageSource, true);
            damageStatsAndHearts(player, target, healthBefore);

            target.invulnerableTime = 0;
            player.causeFoodExhaustion(0.1F);
        } else {
            playServerSideSound(player, SoundEvents.PLAYER_ATTACK_NODAMAGE);
            if (appliedShortFire) {
                target.clearFire();
            }
        }

        if (hurt && fireAspectLevel > 0 && target instanceof LivingEntity) {
            target.igniteForTicks(fireAspectLevel * 80);
        }

        finishAttackSwing(player);
    }

}
