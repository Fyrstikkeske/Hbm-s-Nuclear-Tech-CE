package com.hbm.render.item.weapon.sedna;

import com.hbm.config.ClientConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.SmokeNode;
import com.hbm.lib.RefStrings;
import com.hbm.render.item.TEISRBase;
import com.hbm.render.util.ViewModelPositonDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.util.List;

public abstract class ItemRenderWeaponBase extends TEISRBase {

    protected ViewModelPositonDebugger offsets = new ViewModelPositonDebugger()
            .get(ItemCameraTransforms.TransformType.GUI)
            .setScale(0.06f).setPosition(0.00, 16.5, -9.25).setRotation(186, -182, 0).getHelper()
            .get(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND)
            .setScale(0.5f).setPosition(-1.15, 0.9, -1.4).setRotation(-14, 105, 0).getHelper()
            .get(ItemCameraTransforms.TransformType.GROUND)
            .setScale(0.85f).setPosition(-0.5, 0.6, -0.5).getHelper();

    public static final ResourceLocation flash_plume =  new ResourceLocation(RefStrings.MODID, "textures/models/weapons/lilmac_plume.png");
    public static final ResourceLocation laser_flash = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/laser_flash.png");
    public static float interp;

    public boolean isAkimbo() { return false; }

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        this.renderByItem(itemStackIn, 1.0F);
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        ItemCameraTransforms.TransformType currentType = this.type;

        if (currentType == null) {
            currentType = ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
        }

        switch (currentType) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                setupFirstPerson(stack);
                renderFirstPerson(stack);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                offsets.apply(type);
                setupThirdPerson(stack);
                renderEquipped(stack);
            }
            case GROUND -> {
                offsets.apply(type);
                setupEntity(stack);
                renderEntity(stack);
            }
            case GUI -> {
                offsets.apply(type);
                GlStateManager.disableCull();
                setupInv(stack);
                renderInv(stack);
                GlStateManager.enableCull();
            }
            default -> {
                if (!doNullTransform()) {
                    renderOther(stack, null);
                }
            }
        }
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }

    public void renderEquipped(ItemStack stack) { renderOther(stack, null); }
    public void renderEquippedAkimbo(ItemStack stack) { renderOther(stack, null); }
    public void renderInv(ItemStack stack) { renderOther(stack, null); }
    public void renderEntity(ItemStack stack) { renderOther(stack, null); }

    public void setPerspectiveAndRender(ItemStack stack, float interp) {
        this.interp = interp;
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;
        ItemCameraTransforms.TransformType prev = this.type;
        this.type = mc.player.getPrimaryHand() == EnumHandSide.RIGHT
                ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16;
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        Project.gluPerspective(this.getFOVModifier(interp, ClientConfig.GUN_MODEL_FOV.get()), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, farPlaneDistance * 2.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.pushMatrix();
        try {
            if (mc.gameSettings.thirdPersonView == 0 && !mc.gameSettings.hideGUI) {
                entityRenderer.enableLightmap();
                this.setupTransformsAndRender(stack);
                entityRenderer.disableLightmap();
            }
        } finally {
            GlStateManager.popMatrix();
            this.type = prev; // ОБЯЗАТЕЛЬНО откатываем
        }
        if(mc.gameSettings.thirdPersonView == 0) {
            entityRenderer.itemRenderer.renderOverlays(interp);
        }
    }

    private float getFOVModifier(float interp, boolean useFOVSetting) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityLivingBase entityplayer = (EntityLivingBase) mc.getRenderViewEntity();
        float fov = getBaseFOV(entityplayer.getHeldItemMainhand());
        if(useFOVSetting) fov = mc.gameSettings.fovSetting;
        if(entityplayer.getHealth() <= 0.0F) {
            float f2 = (float) entityplayer.deathTime + interp;
            fov /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }
        net.minecraft.block.state.IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entityplayer, interp);
        if(state.getMaterial() == net.minecraft.block.material.Material.WATER) fov = fov * 60.0F / 70.0F;
        return fov;
    }

    protected float getBaseFOV(ItemStack stack) { return 70F; }
    public float getViewFOV(ItemStack stack, float fov) { return  fov; }
    protected float getSwayMagnitude(ItemStack stack) { return ItemGunBaseNT.getIsAiming(stack) ? 0.1F : 0.5F; }
    protected float getSwayPeriod(ItemStack stack) { return 0.75F; }
    protected float getTurnMagnitude(ItemStack stack) { return 2.75F; }

    protected void setupTransformsAndRender(ItemStack stack) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        float swayMagnitude = getSwayMagnitude(stack);
        float swayPeriod = getSwayPeriod(stack);
        float turnMagnitude = getTurnMagnitude(stack);

        //lighting setup (item lighting changes based on player rotation)
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * interp;
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * interp;
        GlStateManager.pushMatrix();
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();

        //floppyness
        float armPitch = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * interp;
        float armYaw = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * interp;
        GlStateManager.rotate((player.rotationPitch - armPitch) * 0.1F * turnMagnitude, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((player.rotationYaw - armYaw) * 0.1F * turnMagnitude, 0.0F, 1.0F, 0.0F);

        //brightness setup
        int brightness = mc.world.getCombinedLight(new net.minecraft.util.math.BlockPos(player.posX, player.posY, player.posZ), 0);
        int j = brightness % 65536;
        int k = brightness / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        //color setup
        int color = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, 0);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, 1.0F);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(180, 0, 1, 0);

        //viewbob
        if(mc.getRenderViewEntity() instanceof EntityPlayer entityplayer) {
            float distanceDelta = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float distanceInterp = -(entityplayer.distanceWalkedModified + distanceDelta * interp);
            float camYaw = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * interp;
            float camPitch = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * interp;
            GlStateManager.translate(MathHelper.sin(distanceInterp * (float) Math.PI * swayPeriod) * camYaw * 0.5F * swayMagnitude, -Math.abs(MathHelper.cos(distanceInterp * (float) Math.PI * swayPeriod) * camYaw) * swayMagnitude, 0.0F);
            GlStateManager.rotate(MathHelper.sin(distanceInterp * (float) Math.PI * swayPeriod) * camYaw * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(distanceInterp * (float) Math.PI * swayPeriod - 0.2F) * camYaw) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(camPitch, 1.0F, 0.0F, 0.0F);
        }
        this.renderByItem(stack, interp);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    public void setupFirstPerson(ItemStack stack) {
        GlStateManager.translate(0, 0, 1);
        if(Minecraft.getMinecraft().player.isSneaking()) {
            GlStateManager.translate(0, -3.875 / 8D, 0);
        } else {
            float offset = 0.8F;
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.translate(offset, -0.75F * offset, -0.5F * offset);
            GlStateManager.rotate(180, 0, 1, 0);
        }
    }

    public void setupThirdPerson(ItemStack stack) {
        double scale = 0.125D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(12.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(3.5, 0, 0);
    }

    public void setupThirdPersonAkimbo(ItemStack stack) {
        double scale = 0.125D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(12.5F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(5, 0, 0);
    }

    public void setupInv(ItemStack stack) {
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
        GlStateManager.enableAlpha();
        GlStateManager.scale(1, 1, -1);
        GlStateManager.translate(8, 8, 0);
        GlStateManager.rotate(225, 0, 0, 1);
        GlStateManager.rotate(90, 0, 1, 0);
    }

    public void setupEntity(ItemStack stack) {
        double scale = 0.125D;
        GlStateManager.scale(scale, scale, scale);
    }

    public void setupModTable(ItemStack stack) {
        double scale = -5D;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(90, 0, 1, 0);
    }

    public void renderModTable(ItemStack stack, int index) {
        renderOther(stack, ItemCameraTransforms.TransformType.GUI);
    }

    public abstract void renderFirstPerson(ItemStack stack);
    public void renderOther(ItemStack stack, Object type) { }

    public static void standardAimingTransform(ItemStack stack, double sX, double sY, double sZ, double aX, double aY, double aZ) {
        float aimingProgress = ItemGunBaseNT.prevAimingProgress + (ItemGunBaseNT.aimingProgress - ItemGunBaseNT.prevAimingProgress) * interp;
        double x = sX + (aX - sX) * aimingProgress;
        double y = sY + (aY - sY) * aimingProgress;
        double z = sZ + (aZ - sZ) * aimingProgress;
        GlStateManager.translate(x, y, z);
    }

    public static void renderSmokeNodes(List<SmokeNode> nodes, double scale) {
        if (nodes.size() <= 1) return;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.02F);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < nodes.size() - 1; i++) {
            SmokeNode a = nodes.get(i);
            SmokeNode b = nodes.get(i + 1);

            if (a.alpha < 0 || b.alpha < 0) {
                continue;
            }

            buffer.pos(a.forward, a.lift, a.side)                     .color(1F, 1F, 1F, (float)a.alpha).endVertex();
            buffer.pos(a.forward, a.lift, a.side + a.width * scale)   .color(1F, 1F, 1F, 0F)             .endVertex();
            buffer.pos(b.forward, b.lift, b.side + b.width * scale)   .color(1F, 1F, 1F, 0F)             .endVertex();
            buffer.pos(b.forward, b.lift, b.side)                     .color(1F, 1F, 1F, (float)b.alpha).endVertex();

            buffer.pos(a.forward, a.lift, a.side)                     .color(1F, 1F, 1F, (float)a.alpha).endVertex();
            buffer.pos(a.forward, a.lift, a.side - a.width * scale)   .color(1F, 1F, 1F, 0F)             .endVertex();
            buffer.pos(b.forward, b.lift, b.side - b.width * scale)   .color(1F, 1F, 1F, 0F)             .endVertex();
            buffer.pos(b.forward, b.lift, b.side)                     .color(1F, 1F, 1F, (float)b.alpha).endVertex();
        }
        Tessellator.getInstance().draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }


    public static void renderMuzzleFlash(long lastShot) {
        renderMuzzleFlash(lastShot, 75, 15);
    }

    public static void renderMuzzleFlash(long lastShot, int duration, double l) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        if(System.currentTimeMillis() - lastShot < duration) {

            double fire = (System.currentTimeMillis() - lastShot) / (double) duration;
            double width = 6 * fire;
            double length = l * fire;
            double inset = 2;

            Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
            beginFullbrightAdditive();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            buffer.pos(0, -width, -inset).tex(1, 1).endVertex();
            buffer.pos(0, width, -inset).tex(0, 1).endVertex();
            buffer.pos(0.1, width, length - inset).tex(0, 0).endVertex();
            buffer.pos(0.1, -width, length - inset).tex(1, 0).endVertex();

            buffer.pos(0, width, inset).tex(0, 1).endVertex();
            buffer.pos(0, -width, inset).tex(1, 1).endVertex();
            buffer.pos(0.1, -width, -length + inset).tex(1, 0).endVertex();
            buffer.pos(0.1, width, -length + inset).tex(0, 0).endVertex();

            buffer.pos(0, -inset, width).tex(0, 1).endVertex();
            buffer.pos(0, -inset, -width).tex(1, 1).endVertex();
            buffer.pos(0.1, length - inset, -width).tex(1, 0).endVertex();
            buffer.pos(0.1, length - inset, width).tex(0, 0).endVertex();

            buffer.pos(0, inset, -width).tex(1, 1).endVertex();
            buffer.pos(0, inset, width).tex(0, 1).endVertex();
            buffer.pos(0.1, -length + inset, width).tex(0, 0).endVertex();
            buffer.pos(0.1, -length + inset, -width).tex(1, 0).endVertex();

            Tessellator.getInstance().draw();

            endFullbrightAdditive();
        }
    }

    public static void renderGapFlash(long lastShot) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        int flash = 75;
        if(System.currentTimeMillis() - lastShot < flash) {

            double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
            double height = 4 * fire;
            double length = 15 * fire;
            double lift = 3 * fire;
            double offset = 1 * fire;
            double lengthOffset = 0.125;

            Minecraft.getMinecraft().getTextureManager().bindTexture(flash_plume);
            beginFullbrightAdditive();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            buffer.pos(0, -height, -offset).tex(1, 1).endVertex();
            buffer.pos(0, height, -offset).tex(0, 1).endVertex();
            buffer.pos(0, height + lift, length - offset).tex(0, 0).endVertex();
            buffer.pos(0, -height + lift, length - offset).tex(1, 0).endVertex();

            buffer.pos(0, height, offset).tex(0, 1).endVertex();
            buffer.pos(0, -height, offset).tex(1, 1).endVertex();
            buffer.pos(0, -height + lift, -length + offset).tex(1, 0).endVertex();
            buffer.pos(0, height + lift, -length + offset).tex(0, 0).endVertex();

            buffer.pos(0, -height, -offset).tex(1, 1).endVertex();
            buffer.pos(0, height, -offset).tex(0, 1).endVertex();
            buffer.pos(lengthOffset, height, length - offset).tex(0, 0).endVertex();
            buffer.pos(lengthOffset, -height, length - offset).tex(1, 0).endVertex();

            buffer.pos(0, height, offset).tex(0, 1).endVertex();
            buffer.pos(0, -height, offset).tex(1, 1).endVertex();
            buffer.pos(lengthOffset, -height, -length + offset).tex(1, 0).endVertex();
            buffer.pos(lengthOffset, height, -length + offset).tex(0, 0).endVertex();

            Tessellator.getInstance().draw();

            endFullbrightAdditive();
        }
    }

    public static void renderLaserFlash(long lastShot, int flash, double scale, int color) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        if (System.currentTimeMillis() - lastShot < flash) {
            double fire = (System.currentTimeMillis() - lastShot) / (double) flash;
            double size = 4 * fire * scale;

            Minecraft.getMinecraft().getTextureManager().bindTexture(laser_flash);
            beginFullbrightAdditive();
            GlStateManager.depthMask(false);

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            int a = 255;

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            buffer.pos(0, -size, -size).tex(1, 1).color(r, g, b, a).endVertex();
            buffer.pos(0,  size, -size).tex(0, 1).color(r, g, b, a).endVertex();
            buffer.pos(0,  size,  size).tex(0, 0).color(r, g, b, a).endVertex();
            buffer.pos(0, -size,  size).tex(1, 0).color(r, g, b, a).endVertex();

            Tessellator.getInstance().draw();

            GlStateManager.depthMask(true);
            endFullbrightAdditive();
        }
    }

    public static void beginFullbrightAdditive() {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        GlStateManager.disableCull();

        GlStateManager.disableLighting();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
    }

    public static void endFullbrightAdditive() {
        GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

    }
}
