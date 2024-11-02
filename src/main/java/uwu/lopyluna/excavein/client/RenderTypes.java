package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.config.ServerConfig;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;

public class RenderTypes extends RenderStateShard {

    public static RenderType getOutline(ResourceLocation loc, boolean blur) {
        return RenderType.create(MOD_ID + ":outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(loc, blur, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setDepthTestState(ServerConfig.XRAY_OUTLINE_SELECTION.get() && ClientConfig.XRAY_OUTLINE_SELECTION.get() ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                        .createCompositeState(false));
    }

    @SuppressWarnings("all")
    public RenderTypes() {
        super(null, null, null);
    }
}
