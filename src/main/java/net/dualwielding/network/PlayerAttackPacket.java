package net.dualwielding.network;

import net.dualwielding.DualWieldingMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

public class PlayerAttackPacket {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(DualWieldingMain.MODID, "main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int nextPacketId;

    private static final TagKey<Item> MW_DOUBLE_HANDED = TagKey.create(Registries.ITEM, new ResourceLocation("medievalweapons", "double_handed_items"));
    private static final TagKey<Item> MW_ACROSS_DOUBLE_HANDED = TagKey.create(Registries.ITEM, new ResourceLocation("medievalweapons", "accross_double_handed_items"));

    public static void sendAttackEntity(Entity entity) {
        INSTANCE.sendToServer(new AttackEntityMessage(entity.getId()));
    }

    public static void init() {
        INSTANCE.registerMessage(nextPacketId++, AttackEntityMessage.class, AttackEntityMessage::encode, AttackEntityMessage::decode, AttackEntityMessage::handle);
    }

    public static boolean medievalWeaponsDoubleHanded(ItemStack offHandItemStack, Item mainHandItem) {
        if (!ModList.get().isLoaded("medievalweapons")) {
            return true;
        }
        if (offHandItemStack.is(MW_DOUBLE_HANDED) || offHandItemStack.is(MW_ACROSS_DOUBLE_HANDED)) {
            return false;
        }
        ResourceLocation offId = ForgeRegistries.ITEMS.getKey(offHandItemStack.getItem());
        ResourceLocation mainId = ForgeRegistries.ITEMS.getKey(mainHandItem);
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
