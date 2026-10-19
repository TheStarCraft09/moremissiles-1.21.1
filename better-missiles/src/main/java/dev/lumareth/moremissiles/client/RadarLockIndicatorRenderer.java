package dev.lumareth.moremissiles.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.network.RadarLockMarkers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.List;

/**
 * Draws the radar lock indicator at every target locked by a nearby Radar Control Node. The marker always faces the
 * camera, keeps the same size on screen regardless of distance, and is visible through blocks.
 */
@EventBusSubscriber(modid = WariumBetterMissiles.MOD_ID, value = Dist.CLIENT)
public class RadarLockIndicatorRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(WariumBetterMissiles.MOD_ID, "textures/item/radar_lock_indicator.png");
    // Half the marker size per block of distance, i.e. its size on screen
    private static final float SCREEN_SCALE = 0.02F;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<Vec3> targets = RadarLockMarkers.getActive(minecraft.level.getGameTime());
        if (targets.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        // Use the camera's view rotation explicitly instead of relying on whatever the stage left on the stack
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (Vec3 target : targets) {
            Vec3 relative = target.subtract(cameraPos);
            float size = (float) relative.length() * SCREEN_SCALE;
            Matrix4f pose = new Matrix4f()
                    .translate((float) relative.x, (float) relative.y, (float) relative.z)
                    .rotate(camera.rotation())
                    .scale(size);
            buffer.addVertex(pose, -1F, -1F, 0F).setUv(0F, 1F);
            buffer.addVertex(pose, 1F, -1F, 0F).setUv(1F, 1F);
            buffer.addVertex(pose, 1F, 1F, 0F).setUv(1F, 0F);
            buffer.addVertex(pose, -1F, 1F, 0F).setUv(0F, 0F);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}
