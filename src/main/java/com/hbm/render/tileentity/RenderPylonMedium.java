package com.hbm.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.network.energy.TileEntityPylonMedium;

public class RenderPylonMedium extends TileEntitySpecialRenderer<TileEntityPylonMedium> {

	@Override
	public void render(TileEntityPylonMedium tile, double x, double y, double z, float f, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(90, 0F, 1F, 0F); break;
		}
		
		TileEntityPylonMedium pyl = (TileEntityPylonMedium)tile;

		
		if(tile.getBlockType() == ModBlocks.red_pylon_medium_steel || tile.getBlockType() == ModBlocks.red_pylon_medium_steel_transformer)
			bindTexture(ResourceManager.pylon_medium_steel_tex);
		else
			bindTexture(ResourceManager.pylon_medium_tex);
		
		ResourceManager.pylon_medium.renderPart("Pylon");
		if(pyl.hasTransformer()) ResourceManager.pylon_medium.renderPart("Transformer");
		
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		RenderPylon.renderPowerLines(pyl, x, y, z);
		GL11.glPopMatrix();
	}
/*
	@Override
	public Item[] getItemsForRenderer() {
		return new Item[] {
				Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood),
				Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood_transformer),
				Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel),
				Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer)
		};
	}

	@Override
	public Item getItemForRenderer() { return Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood); }

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() {
				GL11.glTranslated(1, -5, 0);
				GL11.glScaled(4.5, 4.5, 4.5);
			}
			public void renderCommonWithStack(ItemStack stack) {
				GL11.glRotatef(90, 0F, 1F, 0F);
				GL11.glScaled(0.5, 0.5, 0.5);
				GL11.glTranslated(0.75, 0, 0);
				
				if(stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel) || stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer))
					bindTexture(ResourceManager.pylon_medium_steel_tex);
				else
					bindTexture(ResourceManager.pylon_medium_tex);
				
				ResourceManager.pylon_medium.renderPart("Pylon");
				
				if(stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_wood_transformer) || stack.getItem() == Item.getItemFromBlock(ModBlocks.red_pylon_medium_steel_transformer))
					ResourceManager.pylon_medium.renderPart("Transformer");
			}
		};
	}

 */
}
