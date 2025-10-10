package com.hbm.util;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manual GL attrib snapshot/restore built on top of {@link GlStateManager}.
 *
 * <p><b>Important</b>: This only handles state that {@code GlStateManager} itself tracks.
 * Anything not tracked there is intentionally <i>not</i> snapshotted/restored:
 * <ul>
 *   <li>Stencil buffer state</li>
 *   <li>Viewport/scissor/matrix stacks</li>
 *   <li>Blend equation</li>
 *   <li>TexEnv / combine / sampler params</li>
 *   <li>VBO/VAO bindings</li>
 * </ul>
 */
public final class RenderUtil {
    private static final Deque<AttribSnapshot> ATTRIB_STACK = new ArrayDeque<>();

    private RenderUtil() {
    }

    public static int getAlphaFunc() {
        return GlStateManager.alphaState.func;
    }

    public static float getAlphaRef() {
        return GlStateManager.alphaState.ref;
    }

    public static boolean isAlphaEnabled() {
        return GlStateManager.alphaState.alphaTest.currentState;
    }

    public static int getBlendSrcFactor() {
        return GlStateManager.blendState.srcFactor;
    }

    public static int getBlendDstFactor() {
        return GlStateManager.blendState.dstFactor;
    }

    public static int getBlendSrcAlphaFactor() {
        return GlStateManager.blendState.srcFactorAlpha;
    }

    public static int getBlendDstAlphaFactor() {
        return GlStateManager.blendState.dstFactorAlpha;
    }

    public static boolean isBlendEnabled() {
        return GlStateManager.blendState.blend.currentState;
    }

    public static boolean isCullEnabled() {
        return GlStateManager.cullState.cullFace.currentState;
    }

    public static boolean isDepthMaskEnabled() {
        return GlStateManager.depthState.maskEnabled;
    }

    public static int getDepthFunc() {
        return GlStateManager.depthState.depthFunc;
    }

    public static boolean isDepthEnabled() {
        return GlStateManager.depthState.depthTest.currentState;
    }

    public static float getCurrentColorRed() {
        return GlStateManager.colorState.red;
    }

    public static float getCurrentColorGreen() {
        return GlStateManager.colorState.green;
    }

    public static float getCurrentColorBlue() {
        return GlStateManager.colorState.blue;
    }

    public static float getCurrentColorAlpha() {
        return GlStateManager.colorState.alpha;
    }

    public static boolean getColorWriteMaskRed() {
        return GlStateManager.colorMaskState.red;
    }

    public static boolean getColorWriteMaskGreen() {
        return GlStateManager.colorMaskState.green;
    }

    public static boolean getColorWriteMaskBlue() {
        return GlStateManager.colorMaskState.blue;
    }

    public static boolean getColorWriteMaskAlpha() {
        return GlStateManager.colorMaskState.alpha;
    }

    public static boolean isLightingEnabled() {
        return GlStateManager.lightingState.currentState;
    }

    public static boolean isTexture2DEnabled() {
        final int unit = GlStateManager.activeTextureUnit;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static boolean isTexture2DEnabled(int unit) {
        if (unit < 0 || unit >= GlStateManager.textureState.length) return false;
        return GlStateManager.textureState[unit].texture2DState.currentState;
    }

    public static int getActiveTextureUnitIndex() {
        return GlStateManager.activeTextureUnit;
    }

    public static int getShadeModel() {
        return GlStateManager.activeShadeModel;
    }

    public static void pushAttrib(int mask) {
        if (mask == GL11.GL_ALL_ATTRIB_BITS) {
            mask = GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_POLYGON_BIT | GL11.GL_FOG_BIT;
        }
        AttribSnapshot s = new AttribSnapshot();
        if ((mask & GL11.GL_ENABLE_BIT) != 0) s.enable = EnableAttrib.capture();
        if ((mask & GL11.GL_LIGHTING_BIT) != 0) s.lighting = LightingAttrib.capture();
        if ((mask & GL11.GL_TEXTURE_BIT) != 0) s.texture = TextureAttrib.capture();
        if ((mask & GL11.GL_COLOR_BUFFER_BIT) != 0) s.color = ColorBufferAttrib.capture();
        if ((mask & GL11.GL_DEPTH_BUFFER_BIT) != 0) s.depth = DepthBufferAttrib.capture();
        if ((mask & GL11.GL_POLYGON_BIT) != 0) s.polygon = PolygonAttrib.capture();
        if ((mask & GL11.GL_FOG_BIT) != 0) s.fog = FogAttrib.capture();
        s.shade = ShadeAttrib.capture();
        ATTRIB_STACK.push(s);
    }

    public static void popAttrib() {
        AttribSnapshot s = ATTRIB_STACK.pollFirst();
        if (s == null) return;
        if (s.texture != null) s.texture.restore();
        if (s.enable != null) s.enable.restore();
        if (s.lighting != null) s.lighting.restore();
        if (s.polygon != null) s.polygon.restore();
        if (s.depth != null) s.depth.restore();
        if (s.fog != null) s.fog.restore();
        if (s.color != null) s.color.restore();
        if (s.shade != null) s.shade.restore();
    }

    public static void pushAllAttribs() {
        pushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    }


    public static void pushGuiBits() {
        pushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_COLOR_BUFFER_BIT);
    }

    public static void popGuiBits() {
        popAttrib();
    }

    public static void clearAttribStack() {
        ATTRIB_STACK.clear();
    }

    private static GlStateManager.FogMode fogModeFromId(int id) {
        for (GlStateManager.FogMode m : GlStateManager.FogMode.values()) {
            if (m.capabilityId == id) return m;
        }
        return null;
    }

    @Desugar
    public record EnableAttrib(boolean alphaTest, boolean blend, boolean cull, boolean depthTest, boolean fog,
                               boolean lighting, boolean normalize, boolean rescaleNormal, boolean colorMaterial,
                               boolean polygonOffsetFill, boolean polygonOffsetLine, boolean colorLogicOp) {
        static EnableAttrib capture() {
            return new EnableAttrib(GlStateManager.alphaState.alphaTest.currentState, GlStateManager.blendState.blend.currentState, GlStateManager.cullState.cullFace.currentState, GlStateManager.depthState.depthTest.currentState, GlStateManager.fogState.fog.currentState, GlStateManager.lightingState.currentState, GlStateManager.normalizeState.currentState, GlStateManager.rescaleNormalState.currentState, GlStateManager.colorMaterialState.colorMaterial.currentState, GlStateManager.polygonOffsetState.polygonOffsetFill.currentState, GlStateManager.polygonOffsetState.polygonOffsetLine.currentState, GlStateManager.colorLogicState.colorLogicOp.currentState);
        }

        void restore() {
            if (alphaTest) GlStateManager.enableAlpha();
            else GlStateManager.disableAlpha();
            if (blend) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            if (cull) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            if (depthTest) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            if (fog) GlStateManager.enableFog();
            else GlStateManager.disableFog();
            if (lighting) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            if (normalize) GlStateManager.enableNormalize();
            else GlStateManager.disableNormalize();
            if (rescaleNormal) GlStateManager.enableRescaleNormal();
            else GlStateManager.disableRescaleNormal();
            if (colorMaterial) GlStateManager.enableColorMaterial();
            else GlStateManager.disableColorMaterial();
            GlStateManager.polygonOffsetState.polygonOffsetFill.setState(polygonOffsetFill);
            GlStateManager.polygonOffsetState.polygonOffsetLine.setState(polygonOffsetLine);
            if (colorLogicOp) GlStateManager.enableColorLogic();
            else GlStateManager.disableColorLogic();
        }
    }

    @Desugar
    public record LightingAttrib(boolean lightingEnabled, boolean[] lightEnabled, // length 8
                                 boolean colorMaterialEnabled, int colorMaterialFace, int colorMaterialMode) {
        static LightingAttrib capture() {
            boolean[] lights = new boolean[GlStateManager.lightState.length];
            for (int i = 0; i < lights.length; i++) {
                lights[i] = GlStateManager.lightState[i].currentState;
            }
            return new LightingAttrib(GlStateManager.lightingState.currentState, lights, GlStateManager.colorMaterialState.colorMaterial.currentState, GlStateManager.colorMaterialState.face, GlStateManager.colorMaterialState.mode);
        }

        void restore() {
            if (lightingEnabled) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            for (int i = 0; i < lightEnabled.length && i < GlStateManager.lightState.length; i++) {
                GlStateManager.lightState[i].setState(lightEnabled[i]);
            }
            GlStateManager.colorMaterial(colorMaterialFace, colorMaterialMode);
            GlStateManager.colorMaterialState.colorMaterial.setState(colorMaterialEnabled);
        }
    }

    @Desugar
    public record TextureAttrib(int activeUnit, boolean[] tex2DEnabled, int[] boundTex2D) {
        static TextureAttrib capture() {
            int n = GlStateManager.textureState.length;
            boolean[] e = new boolean[n];
            int[] names = new int[n];
            for (int i = 0; i < n; i++) {
                e[i] = GlStateManager.textureState[i].texture2DState.currentState;
                names[i] = GlStateManager.textureState[i].textureName;
            }
            return new TextureAttrib(GlStateManager.activeTextureUnit, e, names);
        }

        void restore() {
            int n = Math.min(tex2DEnabled.length, GlStateManager.textureState.length);
            for (int i = 0; i < n; i++) {
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
                GlStateManager.textureState[i].texture2DState.setState(tex2DEnabled[i]);
                GlStateManager.bindTexture(boundTex2D[i]);
            }
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + activeUnit);
        }
    }

    @Desugar
    public record ColorBufferAttrib(float r, float g, float b, float a, boolean maskR, boolean maskG, boolean maskB,
                                    boolean maskA, boolean blendEnabled, int srcRGB, int dstRGB, int srcA, int dstA,
                                    boolean colorLogicEnabled, int colorLogicOpcode, float clearR, float clearG,
                                    float clearB, float clearA) {
        static ColorBufferAttrib capture() {
            return new ColorBufferAttrib(GlStateManager.colorState.red, GlStateManager.colorState.green, GlStateManager.colorState.blue, GlStateManager.colorState.alpha, GlStateManager.colorMaskState.red, GlStateManager.colorMaskState.green, GlStateManager.colorMaskState.blue, GlStateManager.colorMaskState.alpha, GlStateManager.blendState.blend.currentState, GlStateManager.blendState.srcFactor, GlStateManager.blendState.dstFactor, GlStateManager.blendState.srcFactorAlpha, GlStateManager.blendState.dstFactorAlpha, GlStateManager.colorLogicState.colorLogicOp.currentState, GlStateManager.colorLogicState.opcode, GlStateManager.clearState.color.red, GlStateManager.clearState.color.green, GlStateManager.clearState.color.blue, GlStateManager.clearState.color.alpha);
        }

        void restore() {
            GlStateManager.color(r, g, b, a);
            GlStateManager.colorMask(maskR, maskG, maskB, maskA);
            if (blendEnabled) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            GlStateManager.tryBlendFuncSeparate(srcRGB, dstRGB, srcA, dstA);
            if (colorLogicEnabled) GlStateManager.enableColorLogic();
            else GlStateManager.disableColorLogic();
            GlStateManager.colorLogicOp(colorLogicOpcode);
            GlStateManager.clearColor(clearR, clearG, clearB, clearA);
        }
    }

    @Desugar
    public record DepthBufferAttrib(boolean depthEnabled, int depthFunc, boolean maskEnabled, double clearDepth) {
        static DepthBufferAttrib capture() {
            return new DepthBufferAttrib(GlStateManager.depthState.depthTest.currentState, GlStateManager.depthState.depthFunc, GlStateManager.depthState.maskEnabled, GlStateManager.clearState.depth);
        }

        void restore() {
            if (depthEnabled) GlStateManager.enableDepth();
            else GlStateManager.disableDepth();
            GlStateManager.depthFunc(depthFunc);
            GlStateManager.depthMask(maskEnabled);
            GlStateManager.clearDepth(clearDepth);
        }
    }

    @Desugar
    public record PolygonAttrib(boolean cullEnabled, int cullMode, boolean polyOffsetFill, boolean polyOffsetLine,
                                float polyOffsetFactor, float polyOffsetUnits) {
        static PolygonAttrib capture() {
            return new PolygonAttrib(GlStateManager.cullState.cullFace.currentState, GlStateManager.cullState.mode, GlStateManager.polygonOffsetState.polygonOffsetFill.currentState, GlStateManager.polygonOffsetState.polygonOffsetLine.currentState, GlStateManager.polygonOffsetState.factor, GlStateManager.polygonOffsetState.units);
        }

        void restore() {
            if (cullEnabled) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            GlStateManager.cullState.mode = cullMode;
            GL11.glCullFace(cullMode);
            GlStateManager.polygonOffsetState.polygonOffsetFill.setState(polyOffsetFill);
            GlStateManager.polygonOffsetState.polygonOffsetLine.setState(polyOffsetLine);
            GlStateManager.doPolygonOffset(polyOffsetFactor, polyOffsetUnits);
        }
    }

    @Desugar
    public record FogAttrib(boolean fogEnabled, int mode, float density, float start, float end) {
        static FogAttrib capture() {
            return new FogAttrib(GlStateManager.fogState.fog.currentState, GlStateManager.fogState.mode, GlStateManager.fogState.density, GlStateManager.fogState.start, GlStateManager.fogState.end);
        }

        void restore() {
            if (fogEnabled) GlStateManager.enableFog();
            else GlStateManager.disableFog();
            GlStateManager.FogMode fm = fogModeFromId(mode);
            if (fm != null) {
                GlStateManager.setFog(fm);
            } else {
                GlStateManager.fogState.mode = mode;
                GL11.glFogi(GL11.GL_FOG_MODE, mode);
            }
            GlStateManager.setFogDensity(density);
            GlStateManager.setFogStart(start);
            GlStateManager.setFogEnd(end);
        }
    }

    @Desugar
    public record ShadeAttrib(int shadeModel) {
        static ShadeAttrib capture() {
            return new ShadeAttrib(GlStateManager.activeShadeModel);
        }

        void restore() {
            GlStateManager.shadeModel(shadeModel);
        }
    }

    public static final class AttribSnapshot {
        EnableAttrib enable;
        LightingAttrib lighting;
        TextureAttrib texture;
        ColorBufferAttrib color;
        DepthBufferAttrib depth;
        PolygonAttrib polygon;
        FogAttrib fog;
        ShadeAttrib shade;

        @Override
        public String toString() {
            return "AttribSnapshot{" + "enable=" + (enable != null) + ", lighting=" + (lighting != null) + ", texture=" + (texture != null ? ("units=" + texture.tex2DEnabled.length + ", active=" + texture.activeUnit) : "false") + ", color=" + (color != null) + ", depth=" + (depth != null) + ", polygon=" + (polygon != null) + ", fog=" + (fog != null) + ", shade=" + (shade != null ? shade.shadeModel : "false") + '}';
        }
    }
}
