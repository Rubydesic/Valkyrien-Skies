package com.best108.atom_animation_reader.impl;

import net.minecraft.client.renderer.GlStateManager;
import valkyrienwarfare.mod.common.coordinates.VectorImmutable;
import valkyrienwarfare.mod.common.math.Vector;

import java.util.List;

public class GroupedDagNodeRenderer extends BasicDagNodeRenderer {

    private final List<BasicDagNodeRenderer> children;

    public GroupedDagNodeRenderer(String modelName, List<BasicAnimationTransform> transformations, List<BasicDagNodeRenderer> children, VectorImmutable pivot) {
        super(modelName, transformations, null);
        this.children = children;
        this.pivot = pivot;
    }

    @Override
    public void render(double keyframe, int brightness) {
        for (int i = 0; i < transformations.size(); i++) {
            Vector customPivot = pivot.createMutibleVectorCopy();
            for (int j = transformations.size() - 1; j > i; j--) {
                transformations.get(j)
                        .changePivot(customPivot, keyframe);
            }
            GlStateManager.translate(customPivot.X, customPivot.Y, customPivot.Z);
            transformations.get(i)
                    .transform(keyframe);
            GlStateManager.translate(-customPivot.X, -customPivot.Y, -customPivot.Z);
        }

        for (BasicDagNodeRenderer child : children) {
            child.render(keyframe, brightness);
        }
    }
}
