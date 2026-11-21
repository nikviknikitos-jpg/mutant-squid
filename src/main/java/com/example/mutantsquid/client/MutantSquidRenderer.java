package com.example.mutantsquid.client;

import com.example.mutantsquid.entity.MutantSquidEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class MutantSquidRenderer extends EntityRenderer<MutantSquidEntity> {
    private static final Identifier HEAD = new Identifier("mutantsquid", "textures/entity/squid_head.png");
    private static final Identifier TENT = new Identifier("mutantsquid", "textures/entity/squid_tentacle.png");
    private static final Identifier EYE = new Identifier("mutantsquid", "textures/entity/squid_eye.png");

    public MutantSquidRenderer(EntityRendererFactory.Context ctx) { super(ctx); }

    @Override
    public Identifier getTexture(MutantSquidEntity entity) { return HEAD; }

    @Override
    public void render(MutantSquidEntity e, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
        float age = e.age + tickDelta;
        float bob = MathHelper.sin(age * 0.15f) * 0.2f;
        matrices.push();
        matrices.translate(0, 2.5 + bob, 0);

        // body (head)
        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer headVc = vcp.getBuffer(RenderLayer.getEntityCutout(HEAD));
        float hs = 2.0f;
        drawBox(headVc, m, -hs, -hs*0.8f, -hs, hs, hs*0.8f, hs, light);

        // eyes
        VertexConsumer eyeVc = vcp.getBuffer(RenderLayer.getEntityCutout(EYE));
        drawEye(eyeVc, m, hs + 0.01f, 0.2f, 1.0f, light);
        drawEye(eyeVc, m, -hs - 0.01f, 0.2f, 1.0f, light);

        // 8 support tentacles around base
        VertexConsumer tentVc = vcp.getBuffer(RenderLayer.getEntityCutout(TENT));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8.0;
            float bx = (float)Math.cos(angle) * 1.5f;
            float bz = (float)Math.sin(angle) * 1.5f;
            float lift = e.legLift[i] * 0.6f;
            float swing = MathHelper.sin(e.legPhase[i]) * 0.3f;
            drawLeg(tentVc, m, bx, bz, -1.5f, lift, swing, angle, light);
        }

        // 2 front grabbing arms
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? 1f : -1f;
            float bx = side * 1.8f;
            float bz = 1.8f;
            float extend = (e.grabTarget != null && e.grabProgress > 0) ? e.grabProgress * 2.5f : 0;
            drawArm(tentVc, m, bx, bz, -0.5f, extend, side, light);
        }

        matrices.pop();
    }

    private static void drawEye(VertexConsumer vc, Matrix4f m, float x, float y, float z, int light) {
        float s = 0.5f;
        RitualShellRenderer.quad(vc, m, x,y-s,z-s, x,y+s,z-s, x,y+s,z+s, x,y-s,z+s, 1,0,0, light);
    }

    private static void drawLeg(VertexConsumer vc, Matrix4f m, float bx, float bz, float by, float lift, float swing, double angle, int light) {
        // simple: 3 segments pointing down, bending outward
        float segLen = 0.6f;
        float x = bx, z = bz, y = by;
        for (int s = 0; s < 3; s++) {
            float nx = x + (float)Math.cos(angle) * swing;
            float nz = z + (float)Math.sin(angle) * swing;
            float ny = y - segLen;
            if (s == 1) ny += lift; // lift middle
            if (s == 2) ny += lift * 0.5f;
            drawLine(vc, m, x,y,z, nx,ny,nz, 0.28f - s*0.05f, light);
            x = nx; y = ny; z = nz;
        }
    }

    private static void drawArm(VertexConsumer vc, Matrix4f m, float bx, float bz, float by, float extend, float side, int light) {
        float x = bx, z = bz, y = by;
        for (int s = 0; s < 4; s++) {
            float nx = x + extend * 0.35f * side;
            float nz = z + extend * 0.35f;
            float ny = y - 0.4f + (extend > 0 ? 0.25f : 0) * MathHelper.sin(ageStatic(s));
            drawLine(vc, m, x,y,z, nx,ny,nz, 0.30f - s*0.04f, light);
            x = nx; y = ny; z = nz;
        }
    }

    private static float ageStatic(int s) { return s * 1.3f; }

    private static void drawLine(VertexConsumer vc, Matrix4f m, float x0,float y0,float z0, float x1,float y1,float z1, float r, int light) {
        // approximate with 4 crossed quads
        float dx = x1-x0, dy = y1-y0, dz = z1-z0;
        float len = (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        if (len < 1e-4) return;
        // build two perpendicular vectors
        float ux = -dz, uy = 0, uz = dx;
        float ul = (float)Math.sqrt(ux*ux+uz*uz); if (ul<1e-4){ux=1;uz=0;ul=1;}
        ux/=ul; uz/=ul;
        float vx = 0, vy = 1, vz = 0;
        // 2 cross quads
        RitualShellRenderer.quad(vc, m,
                x0+ux*r,y0+vy*r,z0+uz*r, x0+ux*r,y0-vy*r,z0+uz*r,
                x1+ux*r,y1-vy*r,z1+uz*r, x1+ux*r,y1+vy*r,z1+uz*r, ux,0,uz, light);
        RitualShellRenderer.quad(vc, m,
                x0-ux*r,y0+vy*r,z0-uz*r, x1-ux*r,y1+vy*r,z1-uz*r,
                x1-ux*r,y1-vy*r,z1-uz*r, x0-ux*r,y0-vy*r,z0-uz*r, -ux,0,-uz, light);
        RitualShellRenderer.quad(vc, m,
                x0+ux*r,y0+vy*r,z0+uz*r, x1+ux*r,y1+vy*r,z1+uz*r,
                x1-ux*r,y1+vy*r,z1-uz*r, x0-ux*r,y0+vy*r,z0-uz*r, 0,1,0, light);
        RitualShellRenderer.quad(vc, m,
                x0+ux*r,y0-vy*r,z0+uz*r, x0-ux*r,y0-vy*r,z0-uz*r,
                x1-ux*r,y1-vy*r,z1-uz*r, x1+ux*r,y1-vy*r,z1+uz*r, 0,-1,0, light);
    }

    static void drawBox(VertexConsumer vc, Matrix4f m, float x0,float y0,float z0, float x1,float y1,float z1, int light) {
        RitualShellRenderer.drawBox(vc, m, x0, y0, z0, x1, y1, z1, light);
    }
}
