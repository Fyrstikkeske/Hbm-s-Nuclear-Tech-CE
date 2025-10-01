package com.hbm.render.model;

import com.hbm.blocks.network.energy.BlockCable;
import com.hbm.render.amlfrom1710.WavefrontObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class BlockCableBakedModel extends AbstractWavefrontBakedModel {

    private final TextureAtlasSprite sprite;
    private final boolean forBlock;
    private final float itemYaw;

    private final Map<Integer, List<BakedQuad>> cacheByMask = new HashMap<>();
    private List<BakedQuad> itemQuads;

    private BlockCableBakedModel(WavefrontObject model, TextureAtlasSprite sprite, boolean forBlock, float baseScale, float tx, float ty, float tz, float itemYaw) {
        super(model, forBlock ? DefaultVertexFormats.BLOCK : DefaultVertexFormats.ITEM, baseScale, tx, ty, tz, BakedModelTransforms.pipeItem());
        this.sprite = sprite;
        this.forBlock = forBlock;
        this.itemYaw = itemYaw;
    }

    public static BlockCableBakedModel forBlock(WavefrontObject model, TextureAtlasSprite sprite) {
        return new BlockCableBakedModel(model, sprite, true, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    public static BlockCableBakedModel forItem(WavefrontObject model, TextureAtlasSprite sprite, float baseScale, float tx, float ty, float tz, float yaw) {
        return new BlockCableBakedModel(model, sprite, false, baseScale, tx, ty, tz, yaw);
    }

    public static BlockCableBakedModel empty(TextureAtlasSprite sprite) {
        return new BlockCableBakedModel(new WavefrontObject(new ResourceLocation("minecraft:empty")), sprite, true, 1.0F, 0, 0, 0, 0);
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (forBlock) {
            boolean pX = false, nX = false, pY = false, nY = false, pZ = false, nZ = false;
            if (state != null) {
                try {
                    if (state.getPropertyKeys().contains(BlockCable.POS_X)) pX = state.getValue(BlockCable.POS_X);
                    if (state.getPropertyKeys().contains(BlockCable.NEG_X)) nX = state.getValue(BlockCable.NEG_X);
                    if (state.getPropertyKeys().contains(BlockCable.POS_Y)) pY = state.getValue(BlockCable.POS_Y);
                    if (state.getPropertyKeys().contains(BlockCable.NEG_Y)) nY = state.getValue(BlockCable.NEG_Y);
                    if (state.getPropertyKeys().contains(BlockCable.POS_Z)) pZ = state.getValue(BlockCable.POS_Z);
                    if (state.getPropertyKeys().contains(BlockCable.NEG_Z)) nZ = state.getValue(BlockCable.NEG_Z);
                } catch (Exception ignored) {}
            }
            int mask = (pX ? 1 : 0)
                    | (nX ? 2 : 0)
                    | (pY ? 4 : 0)
                    | (nY ? 8 : 0)
                    | (pZ ? 16 : 0)
                    | (nZ ? 32 : 0);

            List<BakedQuad> quads = cacheByMask.get(mask);
            if (quads != null) return quads;

            quads = buildWorldQuads(pX, nX, pY, nY, pZ, nZ);
            cacheByMask.put(mask, quads);
            return quads;
        } else {
            if (itemQuads == null) {
                itemQuads = buildItemQuads();
            }
            return itemQuads;
        }
    }

    private List<BakedQuad> buildWorldQuads(boolean pX, boolean nX, boolean pY, boolean nY, boolean pZ, boolean nZ) {
        List<String> parts = new ArrayList<>();

        if (pX && nX && !pY && !nY && !pZ && !nZ) {
            parts.add("CX");
        } else if (!pX && !nX && pY && nY && !pZ && !nZ) {
            parts.add("CY");
        } else if (!pX && !nX && !pY && !nY && pZ && nZ) {
            parts.add("CZ");
        } else {
            parts.add("Core");
            if (pX) parts.add("posX");
            if (nX) parts.add("negX");
            if (pY) parts.add("posY");
            if (nY) parts.add("negY");
            if (nZ) parts.add("posZ"); // note: mirrors original 1.7.10 code (nZ -> posZ)
            if (pZ) parts.add("negZ"); // mirrors original (pZ -> negZ)
        }

        return bakeSimpleQuads(parts, 0.0F, 0.0F, 0.0F, true, true, sprite);
    }

    private List<BakedQuad> buildItemQuads() {
        List<String> parts = Arrays.asList("Core", "posX", "negX", "posZ", "negZ");
        return bakeSimpleQuads(parts, 0.0F, 0.0F, itemYaw, false, false, sprite);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return sprite;
    }
}
