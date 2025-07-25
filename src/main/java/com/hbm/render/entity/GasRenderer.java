package com.hbm.render.entity;

import com.hbm.entity.particle.EntityGasFX;
import com.hbm.items.ModItems;
import com.hbm.render.NTMRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;
import java.util.Map;

public class GasRenderer extends Render<EntityGasFX> {

	public static final IRenderFactory<EntityGasFX> FACTORY = (RenderManager man) -> {return new GasRenderer(man);};
	
	private Item renderItem;
	private static final Map<Item, TextureAtlasSprite> textures = new HashMap<Item, TextureAtlasSprite>();
	
	protected GasRenderer(RenderManager renderManager) {
		super(renderManager);
		renderItem = ModItems.gas1;
	}
	
	@Override
	public void doRender(EntityGasFX fx, double x, double y, double z, float entityYaw, float partialTicks) {
		if(textures.isEmpty()){
			textures.put(ModItems.gas1, NTMRenderHelper.getItemTexture(ModItems.gas1));
			textures.put(ModItems.gas2, NTMRenderHelper.getItemTexture(ModItems.gas2));
			textures.put(ModItems.gas3, NTMRenderHelper.getItemTexture(ModItems.gas3));
			textures.put(ModItems.gas4, NTMRenderHelper.getItemTexture(ModItems.gas4));
			textures.put(ModItems.gas5, NTMRenderHelper.getItemTexture(ModItems.gas5));
			textures.put(ModItems.gas6, NTMRenderHelper.getItemTexture(ModItems.gas6));
			textures.put(ModItems.gas7, NTMRenderHelper.getItemTexture(ModItems.gas7));
			textures.put(ModItems.gas8, NTMRenderHelper.getItemTexture(ModItems.gas8));
		}
		if (fx.particleAge <= fx.maxAge && fx.particleAge >= fx.maxAge / 8 * 7) {
			renderItem = ModItems.gas8;
		}

		if (fx.particleAge < fx.maxAge / 8 * 7 && fx.particleAge >= fx.maxAge / 8 * 6) {
			renderItem = ModItems.gas7;
		}

		if (fx.particleAge < fx.maxAge / 8 * 6 && fx.particleAge >= fx.maxAge / 8 * 5) {
			renderItem = ModItems.gas6;
		}

		if (fx.particleAge < fx.maxAge / 8 * 5 && fx.particleAge >= fx.maxAge / 8 * 4) {
			renderItem = ModItems.gas5;
		}

		if (fx.particleAge < fx.maxAge / 8 * 4 && fx.particleAge >= fx.maxAge / 8 * 3) {
			renderItem = ModItems.gas4;
		}

		if (fx.particleAge < fx.maxAge / 8 * 3 && fx.particleAge >= fx.maxAge / 8 * 2) {
			renderItem = ModItems.gas3;
		}

		if (fx.particleAge < fx.maxAge / 8 * 2 && fx.particleAge >= fx.maxAge / 8 * 1) {
			renderItem = ModItems.gas2;
		}

		if (fx.particleAge < fx.maxAge / 8 && fx.particleAge >= 0) {
			renderItem = ModItems.gas1;
		}

		TextureAtlasSprite iicon = textures.get(renderItem);

		if (iicon != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GlStateManager.disableLighting();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.scale(7.5F, 7.5F, 7.5F);
			//
			GlStateManager.scale(0.25F, 0.25F, 0.25F);
			//
			this.bindEntityTexture(fx);

			this.func_77026_a(iicon);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGasFX entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
	
	private void func_77026_a(TextureAtlasSprite p_77026_2_) {
		float f = p_77026_2_.getMinU();
		float f1 = p_77026_2_.getMaxU();
		float f2 = p_77026_2_.getMinV();
		float f3 = p_77026_2_.getMaxV();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		NTMRenderHelper.startDrawingTexturedQuads();
		NTMRenderHelper.addVertexWithUV(0.0F - f5, 0.0F - f6, 0.0D, f, f3);
		NTMRenderHelper.addVertexWithUV(f4 - f5, 0.0F - f6, 0.0D, f1, f3);
		NTMRenderHelper.addVertexWithUV(f4 - f5, f4 - f6, 0.0D, f1, f2);
		NTMRenderHelper.addVertexWithUV(0.0F - f5, f4 - f6, 0.0D, f, f2);
		NTMRenderHelper.draw();
	}

}
