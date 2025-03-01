package com.hbm.items.special;

import com.google.common.collect.ImmutableMap;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ItemAutogen extends Item {

    public static List<ItemAutogen> INSTANCES = new ArrayList<>();
    MaterialShapes shape;
    private HashMap<NTMMaterial, String> textureOverrides = new HashMap();
    private String overrideUnlocalizedName = null;

    public ItemAutogen(MaterialShapes shape, String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.shape = shape;

        ModItems.ALL_ITEMS.add(this);
        INSTANCES.add(this);

    }

    /**
     * add override texture
     */
    public ItemAutogen aot(NTMMaterial mat, String tex) {
        textureOverrides.put(mat, tex);
        return this;
    }

    public ItemAutogen oun(String overrideUnlocalizedName) {
        this.overrideUnlocalizedName = overrideUnlocalizedName;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (NTMMaterial mat : Mats.orderedList) {
            if (mat.autogen.contains(this.shape)) {
                String texturePath = getTexturePath(mat);
                ModelResourceLocation location = new ModelResourceLocation(
                        RefStrings.MODID + ":" + texturePath, "inventory"
                );
                ModelLoader.setCustomModelResourceLocation(this, mat.id, location);
            }
        }
    }

    public void bakeModels(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));
            for (NTMMaterial mat : Mats.orderedList) {
                if (mat.autogen.contains(this.shape)) {
                    String pathIn = getTexturePath(mat);
                    ResourceLocation spriteLoc = new ResourceLocation(RefStrings.MODID, pathIn);
                    IModel retexturedModel = baseModel.retexture(
                            ImmutableMap.of(
                                    "layer0", spriteLoc.toString()
                            )

                    );
                    IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation bakedModelLocation = new ModelResourceLocation(new ResourceLocation(RefStrings.MODID, pathIn), "inventory");
                    event.getModelRegistry().putObject(bakedModelLocation, bakedModel);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerSprites(TextureMap map) {
        for (NTMMaterial mat : Mats.orderedList) {
            if(!textureOverrides.containsKey(mat) && mat.solidColorLight != mat.solidColorDark && (shape == null || mat.autogen.contains(shape))) {
                ResourceLocation spriteLoc = new ResourceLocation(RefStrings.MODID, "items/"+ this.getRegistryName().getPath() + "-" + mat.names[0]);
                TextureAtlasSprite sprite = new TextureAtlasSpriteMutatable(spriteLoc.toString(), new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, mat.solidColorLight, mat.solidColorDark));
                map.setTextureEntry(sprite);
            }
            if(textureOverrides.containsKey(mat) && (shape == null || mat.autogen.contains(shape))) {
                ResourceLocation spriteLoc = new ResourceLocation(RefStrings.MODID, "items/" + textureOverrides.get(mat));
                map.registerSprite(spriteLoc);
            }
        }
    }

    private String getTexturePath(NTMMaterial mat) {
        if (textureOverrides.containsKey(mat)) {
            return "items/" + textureOverrides.get(mat);
        } else {
            return "items/" + this.getRegistryName().getPath() + "-" + mat.names[0];
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (NTMMaterial mat : Mats.orderedList) {
                if (mat.autogen.contains(this.shape)) {
                    items.add(new ItemStack(this, 1, mat.id));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {

        NTMMaterial mat = Mats.matById.get(stack.getItemDamage());

        if (mat == null) {
            return "UNDEFINED";
        }

        String matName = I18n.format(mat.getTranslationKey());
        return I18n.format(this.getUnlocalizedNameInefficiently(stack) + ".name", matName);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return overrideUnlocalizedName != null ? "item." + overrideUnlocalizedName : super.getTranslationKey(stack);
    }
}
