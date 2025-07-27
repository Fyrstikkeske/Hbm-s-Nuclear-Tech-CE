package com.hbm.render.model;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.entity.Entity;

public class ModelArmorDesh extends ModelArmorBase {

	public ModelArmorDesh(int type) {
		super(type);

		this.head = new ModelRendererObj(ResourceManager.armor_steamsuit, "Head");
		this.body = new ModelRendererObj(ResourceManager.armor_steamsuit, "Body");
		this.leftArm = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(ResourceManager.armor_steamsuit, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(ResourceManager.armor_steamsuit, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {

		super.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);

		GL11.glPushMatrix();
		GL11.glShadeModel(GL11.GL_SMOOTH);
		if(this.isChild) {
			GL11.glScalef(0.75F, 0.75F, 0.75F);
			GL11.glTranslatef(0.0F, 16.0F * par7, 0.0F);
		}
		if(type == 0) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.steamsuit_helmet);
			head.render(par7*1.001F);
		}
		if(this.isChild) {
			GL11.glScalef(0.75F, 0.75F, 0.75F);
		}
		if(type == 1) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.steamsuit_chest);
			body.render(par7);
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.steamsuit_arm);
			leftArm.render(par7);
			rightArm.render(par7);
		}
		if(type == 2) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.steamsuit_leg);
			leftLeg.render(par7);
			rightLeg.render(par7);
		}
		if(type == 3) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.steamsuit_leg);
			leftFoot.render(par7);
			rightFoot.render(par7);
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}
}
