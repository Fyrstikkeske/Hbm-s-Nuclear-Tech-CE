package com.hbm.items.weapon;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

import java.util.ArrayList;
import java.util.List;

public interface IMetaItemTesr {
    List<IMetaItemTesr> INSTANCES = new ArrayList<>();

    static void redirectModels() {
        INSTANCES.forEach(IMetaItemTesr::redirectModel);
    }

    int getSubitemCount();

    String getName();

    default void redirectModel() {
        for (int i = 1; i < getSubitemCount(); i++) {
            ModelLoader.setCustomModelResourceLocation((Item) this, i, new ModelResourceLocation(getName(), "inventory"));
        }
    }


}
