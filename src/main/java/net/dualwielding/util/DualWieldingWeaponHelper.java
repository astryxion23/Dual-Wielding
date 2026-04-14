package net.dualwielding.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

public final class DualWieldingWeaponHelper {

    private DualWieldingWeaponHelper() {
    }

    /**
     * Matches legacy {@code SwordItem} / {@code DiggerItem} checks using 1.21 item tags.
     */
    public static boolean isMeleeWeapon(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES));
    }
}
