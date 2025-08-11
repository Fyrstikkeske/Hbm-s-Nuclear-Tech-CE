package com.hbm.render.item.weapon.sedna;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
@AutoRegister(item = "gun_lasrifle")
public class ItemRenderLasrifle extends ItemRenderWeaponBase {

	@Override
	protected float getTurnMagnitude(ItemStack stack) {
		return ItemGunBaseNT.getIsAiming(stack) ? 2.5F : -0.25F;
	}

	@Override
	public float getViewFOV(ItemStack stack, float fov) {
		float aimingProgress = ItemGunBaseNT.prevAimingProgress +
				(ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
		return fov * (1 - aimingProgress * 0.75F);
	}

	@Override
	public void setupFirstPerson(ItemStack stack) {
		GlStateManager.translate(0, 0, 0.875);

		float offset = 0.8F;
		standardAimingTransform(stack,
				-1.5F * offset, -1.5F * offset, 2.5F * offset,
				0, -7.375 / 8D, 0.75);
	}

	@Override
	public void renderFirstPerson(ItemStack stack) {

		if(ItemGunBaseNT.prevAimingProgress == 1 && ItemGunBaseNT.aimingProgress == 1) return;
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.lasrifle_tex);
		double scale = 0.3125D;
		GlStateManager.scale(scale, scale, scale);

		double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
		double[] recoil = HbmAnimationsSedna.getRelevantTransformation("RECOIL");
		double[] lever = HbmAnimationsSedna.getRelevantTransformation("LEVER");
		double[] mag = HbmAnimationsSedna.getRelevantTransformation("MAG");

		GlStateManager.translate(0, -1, -6);
		GlStateManager.rotate((float) equip[0], 1, 0, 0);
		GlStateManager.translate(0, 1, 6);

		GlStateManager.translate(0, 0, recoil[2]);

		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		ResourceManager.lasrifle.renderPart("Gun");
		ResourceManager.lasrifle.renderPart("Barrel");
		ResourceManager.lasrifle.renderPart("Stock");
		ResourceManager.lasrifle.renderPart("Scope");

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, -0.375, 2.375);
		GlStateManager.rotate((float) lever[0], 1, 0, 0);
		GlStateManager.translate(0, 0.375, -2.375);
		ResourceManager.lasrifle.renderPart("Lever");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(mag[0], mag[1], mag[2]);
		ResourceManager.lasrifle.renderPart("Battery");
		GlStateManager.popMatrix();

		GlStateManager.shadeModel(GL11.GL_FLAT);
	}

	@Override
	public void setupThirdPerson(ItemStack stack) {
		super.setupThirdPerson(stack);
		double scale = 1.25D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(0, 0, 4);
	}

	@Override
	public void setupInv(ItemStack stack) {
		super.setupInv(stack);
		double scale = 1.0625D;
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.rotate(25, 1, 0, 0);
		GlStateManager.rotate(45, 0, 1, 0);
		GlStateManager.translate(0.5, 0, 0);
	}

	@Override
	public void renderOther(ItemStack stack, Object type) {
		GlStateManager.enableLighting();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.lasrifle_tex);
		ResourceManager.lasrifle.renderPart("Gun");
		ResourceManager.lasrifle.renderPart("Barrel");
		ResourceManager.lasrifle.renderPart("Stock");
		ResourceManager.lasrifle.renderPart("Scope");
		ResourceManager.lasrifle.renderPart("Lever");
		ResourceManager.lasrifle.renderPart("Battery");
		GlStateManager.shadeModel(GL11.GL_FLAT);
	}
}

