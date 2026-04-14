package net.dualwielding.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public class PlayerAttackPacket {

    private static final TagKey<Item> MW_DOUBLE_HANDED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("medievalweapons", "double_handed_items"));
    private static final TagKey<Item> MW_ACROSS_DOUBLE_HANDED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("medievalweapons", "accross_double_handed_items"));

    public static boolean medievalWeaponsDoubleHanded(ItemStack offHandItemStack, Item mainHandItem) {
        if (!ModList.get().isLoaded("medievalweapons")) {
            return true;
        }
        if (offHandItemStack.is(MW_DOUBLE_HANDED) || offHandItemStack.is(MW_ACROSS_DOUBLE_HANDED)) {
            return false;
        }
        ResourceLocation offId = BuiltInRegistries.ITEM.getKey(offHandItemStack.getItem());
        ResourceLocation mainId = BuiltInRegistries.ITEM.getKey(mainHandItem);
        if (offId != null && offId.getNamespace().equals("medievalweapons")) {
            String op = offId.getPath();
            if (op.contains("long_sword") || op.contains("big_axe")) {
                return false;
            }
            if (op.contains("ninjato") && mainId != null && mainId.getNamespace().equals("medievalweapons") && mainId.getPath().contains("ninjato")) {
                return false;
            }
        }
        return true;
    }

}
