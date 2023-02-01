package com.github.dred974.workshop.workshop.item;

import java.util.ArrayList;

public class ModidItems {

    public static final ArrayList<Item> ITEMS = new ArrayList<Item>();

    public static void setItemName(Item item, String name) {
        item.setRegistryName(ModidMod.MODID, name).setUnlocalizedName(ModidMod.MODID + "." + name);
        ITEMS.add(item);
    }
    public static void setItemBlockName(Item item, Block block) {
        item.setRegistryName(block.getRegistryName());
        ITEMS.add(item);
    }
    @SideOnly(Side.CLIENT) // Forge annotation for Side managing
    @SubscribeEvent // Forge annotation to subscribe to an event
    protected static void registerItemModels(ModelRegistryEvente) {
        ITEMS.forEach(item->registerModel(item, 0)); // A simple forEach to call the
        method below
    }
    @SideOnly(Side.CLIENT)
    protected static void registerModel (Item item, int metadata) {
        ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
