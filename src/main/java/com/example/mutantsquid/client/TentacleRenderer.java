package com.example.mutantsquid.client;

import com.example.mutantsquid.entity.TentacleEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class TentacleRenderer extends EntityRenderer<TentacleEntity> {
    private static final Identifier TEX = new Identifier("mutantsquid", "textures/entity/squid_tentacle.png");

    public TentacleRenderer(EntityRendererFactory.Context ctx) { super(ctx); }

    @Override
    public Identifier getTexture(TentacleEntity entity) { return TEX; }

    @Override
    public void render(TentacleEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        matrices.push();
        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutout(TEX));
        float height = 4.0f;
        int segments = 8;
        float prevR = 0.6f;
        for (int i = 0; i < segments; i++) {
            float t0 = (float)i / segments;
            float t1 = (float)(i+1) / segments;
            float y0 = t0 * height;
            float y1 = t1 * height;
            float r0 = 0.6f * (1 - t0 * 0.8f);
            float r1 = 0.6f * (1 - t1 * 0.8f);
            float wobble = (float)Math.sin((entity.age + tickDelta) * 0.1f + i) * 0.15f;
            float x0 = wobble * t0, z0 = wobble * t0;
            float x1 = wobble * t1, z1 = wobble * t1;
            drawCylinderSegment(vc, m, x0, y0, z0, r0, x1, y1, z1, r1, light);
            prevR = r1;
        }
        matrices.pop();
    }

    static void drawCylinderSegment(VertexConsumer vc, Matrix4f m,
                                    float x0, float y0, float z0, float r0,
                                    float x1, float y1, float z1, float r1, int light) {
        int sides = 8;
        for (int i = 0; i < sides; i++) {
            float a0 = (float)(Math.PI * 2 * i / sides);
            float a1 = (float)(Math.PI * 2 * (i+1) / sides);
            float cx0 = (float)Math.cos(a0), cz0 = (float)Math.sin(a0);
            float cx1 = (float)Math.cos(a1), cz1 = (float)Math.sin(a1);
            vc.vertex(m, x0 + cx0*r0, y0, z0 + cz0*r0).color(180,160,200,255).texture(0,0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(cx0,0,cz0).next();
            vc.vertex(m, x0 + cx1*r0, y0, z0 + cz1*r0).color(180,160,200,255).texture(1,0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(cx1,0,cz1).next();
            vc.vertex(m, x1 + cx1*r1, y1, z1 + cz1*r1).color(180,160,200,255).texture(1,1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(cx1,0,cz1).next();
            vc.vertex(m, x1 + cx0*r1, y1, z1 + cz0*r1).color(180,160,200,255).texture(0,1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(cx0,0,cz0).next();
        }
    }
}
