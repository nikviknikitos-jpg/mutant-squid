package com.example.mutantsquid.client;

import com.example.mutantsquid.entity.RitualShellEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class RitualShellRenderer extends EntityRenderer<RitualShellEntity> {
    private static final Identifier TEX = new Identifier("mutantsquid", "textures/entity/ritual_shell.png");

    public RitualShellRenderer(EntityRendererFactory.Context ctx) { super(ctx); }

    @Override
    public Identifier getTexture(RitualShellEntity entity) { return TEX; }

    @Override
    public void render(RitualShellEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        matrices.push();
        matrices.translate(0, 0.5, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.age * 2f + tickDelta));
        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutout(TEX));
        float s = 0.5f;
        float pulse = 1.0f + MathHelper.sin((entity.age + tickDelta) * 0.15f) * 0.1f;
        drawBox(vc, m, -s*pulse, 0, -s*pulse, s*pulse, s*2*pulse, s*pulse, light);
        matrices.pop();
    }

    static void drawBox(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int light) {
        // +Y
        quad(vc, m, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1, 0,1,0, light);
        // -Y
        quad(vc, m, x0,y0,z0, x0,y0,z1, x1,y0,z1, x1,y0,z0, 0,-1,0, light);
        // +Z
        quad(vc, m, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1, 0,0,1, light);
        // -Z
        quad(vc, m, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0, 0,0,-1, light);
        // +X
        quad(vc, m, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1, 1,0,0, light);
        // -X
        quad(vc, m, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0, -1,0,0, light);
    }

    static void quad(VertexConsumer vc, Matrix4f m, float x0,float y0,float z0, float x1,float y1,float z1, float x2,float y2,float z2, float x3,float y3,float z3, float nx,float ny,float nz, int light) {
        vc.vertex(m, x0,y0,z0).color(255,255,255,255).texture(0,1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(nx,ny,nz).next();
        vc.vertex(m, x1,y1,z1).color(255,255,255,255).texture(1,1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(nx,ny,nz).next();
        vc.vertex(m, x2,y2,z2).color(255,255,255,255).texture(1,0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(nx,ny,nz).next();
        vc.vertex(m, x3,y3,z3).color(255,255,255,255).texture(0,0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(nx,ny,nz).next();
    }
}
