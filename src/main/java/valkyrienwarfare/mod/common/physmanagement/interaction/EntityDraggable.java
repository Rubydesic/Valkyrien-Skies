/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2015-2018 the Valkyrien Warfare team
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income unless it is to be used as a part of a larger project (IE: "modpacks"), nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from the Valkyrien Warfare team.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: The Valkyrien Warfare team), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package valkyrienwarfare.mod.common.physmanagement.interaction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import valkyrienwarfare.addon.combat.entity.EntityCannonBall;
import valkyrienwarfare.api.TransformType;
import valkyrienwarfare.mod.client.EventsClient;
import valkyrienwarfare.mod.common.coordinates.ShipTransform;
import valkyrienwarfare.mod.common.entity.PhysicsWrapperEntity;
import valkyrienwarfare.mod.common.math.RotationMatrices;
import valkyrienwarfare.mod.common.math.Vector;
import valkyrienwarfare.mod.common.physics.management.ShipTransformationManager;
import valkyrienwarfare.mod.common.util.EntityShipMountData;
import valkyrienwarfare.mod.common.util.ValkyrienUtils;

import java.util.List;

/**
 * Bad class, delete soon!
 *
 * @author thebest108
 */
@Deprecated
public class EntityDraggable {

    /**
     * Moves entities such that they move with the ship below them.
     */
    public static void tickAddedVelocityForWorld(World world) {
        try {
            for (int i = 0; i < world.loadedEntityList.size(); i++) {
                Entity e = world.loadedEntityList.get(i);
                if (!(e instanceof PhysicsWrapperEntity) && !(e instanceof EntityCannonBall) && !e.isDead) {
                    IDraggable draggable = getDraggableFromEntity(e);

                    if (draggable.getWorldBelowFeet() == null) {
                        if (e.onGround) {
                            draggable.getVelocityAddedToPlayer().zero();
                            draggable.setYawDifVelocity(0);
                        } else {
                            if (e instanceof EntityPlayer) {
                                EntityPlayer player = (EntityPlayer) e;
                                if (player.isCreative() && player.capabilities.isFlying) {
                                    draggable.getVelocityAddedToPlayer().multiply(.99D * .95D);
                                    draggable.setYawDifVelocity(draggable.getYawDifVelocity() * .95D * .95D);
                                }
                            }
                        }
                    }
                    // Only run the added velocity code if there's a significant amount to add; or if we're standing on top of a ship.
                    if (draggable.getVelocityAddedToPlayer()
                            .lengthSq() > .01 || draggable.getWorldBelowFeet() != null) {
                        addEntityVelocityFromShipBelow(e);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the ship below velocity to entity.
     */
    private static void addEntityVelocityFromShipBelow(Entity entity) {
        IDraggable draggable = EntityDraggable.getDraggableFromEntity(entity);

        EntityShipMountData mountData = ValkyrienUtils.getMountedShipAndPos(entity);

        if (draggable.getWorldBelowFeet() != null) {
            ShipTransformationManager coordTransform = draggable.getWorldBelowFeet().getPhysicsObject().getShipTransformationManager();

            if (entity.world.isRemote && entity instanceof EntityPlayer) {
                EventsClient.updatePlayerMouseOver(entity);
            }

            float rotYaw = entity.rotationYaw;
            float rotPitch = entity.rotationPitch;
            float prevYaw = entity.prevRotationYaw;
            float prevPitch = entity.prevRotationPitch;

            Vector oldPos = new Vector(entity);

            //            RotationMatrices.applyTransform(coordTransform.prevwToLTransform, entity);
            // This is causing crashes
            double[] prev = coordTransform.getPrevTickTransform().getInternalMatrix(TransformType.GLOBAL_TO_SUBSPACE);
            double[] next = coordTransform.getCurrentTickTransform().getInternalMatrix(TransformType.SUBSPACE_TO_GLOBAL);

            Vector playerPos = new Vector(entity);
            ShipTransform betweenTransform = new ShipTransform(RotationMatrices.getMatrixProduct(next, prev));
            // betweenTransform.transform(playerPos, TransformType.SUBSPACE_TO_GLOBAL);

            RotationMatrices.applyTransform(betweenTransform, entity, TransformType.SUBSPACE_TO_GLOBAL);
            // This is what the code used to do, but this caused problems when other threads read this data.
            // RotationMatrices.applyTransform(coordTransform.getPrevTickTransform(), entity, TransformType.GLOBAL_TO_SUBSPACE);
            // RotationMatrices.applyTransform(coordTransform.getCurrentTickTransform(), entity, TransformType.SUBSPACE_TO_GLOBAL);

            Vector newPos = new Vector(entity);

            // Move the entity back to its old position, the added velocity will be used
            // afterwards
            entity.setPosition(oldPos.X, oldPos.Y, oldPos.Z);
            Vector addedVel = oldPos.getSubtraction(newPos);

            draggable.setVelocityAddedToPlayer(addedVel);

            entity.rotationYaw = rotYaw;
            entity.rotationPitch = rotPitch;
            entity.prevRotationYaw = prevYaw;
            entity.prevRotationPitch = prevPitch;

            Vector oldLookingPos = new Vector(entity.getLook(1.0F));
            //            coordTransform.getPrevTickTransform().rotate(oldLookingPos, TransformType.GLOBAL_TO_SUBSPACE);
            //            coordTransform.getCurrentTickTransform().rotate(oldLookingPos, TransformType.SUBSPACE_TO_GLOBAL);
            betweenTransform.rotate(oldLookingPos, TransformType.SUBSPACE_TO_GLOBAL);

            double newPitch = Math.asin(oldLookingPos.Y) * -180D / Math.PI;
            double f4 = -Math.cos(-newPitch * 0.017453292D);
            double radianYaw = Math.atan2((oldLookingPos.X / f4), (oldLookingPos.Z / f4));
            radianYaw += Math.PI;
            radianYaw *= -180D / Math.PI;

            if (!(Double.isNaN(radianYaw) || Math.abs(newPitch) > 85)) {
                double wrappedYaw = MathHelper.wrapDegrees(radianYaw);
                double wrappedRotYaw = MathHelper.wrapDegrees(entity.rotationYaw);
                double yawDif = wrappedYaw - wrappedRotYaw;
                if (Math.abs(yawDif) > 180D) {
                    if (yawDif < 0) {
                        yawDif += 360D;
                    } else {
                        yawDif -= 360D;
                    }
                }
                yawDif %= 360D;
                final double threshold = .1D;
                if (Math.abs(yawDif) < threshold) {
                    yawDif = 0D;
                }
                draggable.setYawDifVelocity(yawDif);
            }
        }

        if (true) {
            boolean originallySneaking = entity.isSneaking();
            entity.setSneaking(false);
            if (draggable.getWorldBelowFeet() == null && entity.onGround) {
                draggable.getVelocityAddedToPlayer().zero();
            }


            Vector velocityProper = new Vector(draggable.getVelocityAddedToPlayer());
            AxisAlignedBB originalBoundingBox = entity.getEntityBoundingBox();
            if (velocityProper.lengthSq() < 1000000) {
                draggable.setVelocityAddedToPlayer(getVelocityProper(velocityProper, entity));
            } else {
                System.err.println(entity.getName() + " tried moving way too fast!");
            }


            entity.setEntityBoundingBox(originalBoundingBox);

            entity.setEntityBoundingBox(entity.getEntityBoundingBox().offset(draggable.getVelocityAddedToPlayer().X,
                    draggable.getVelocityAddedToPlayer().Y, draggable.getVelocityAddedToPlayer().Z));
            entity.resetPositionToBB();

            if (!mountData.isMounted()) {
                entity.rotationYaw += draggable.getYawDifVelocity();
            }

            // Do not add this movement as if the entity were walking it
            // entity.distanceWalkedModified = originalWalked;
            // entity.distanceWalkedOnStepModified = originalWalkedOnStep;
            entity.setSneaking(originallySneaking);
        }

        draggable.getVelocityAddedToPlayer().multiply(.99D);
        draggable.setYawDifVelocity(draggable.getYawDifVelocity() * .95D);
    }

    public static IDraggable getDraggableFromEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        return (IDraggable) entity;
    }

    public static Entity getEntityFromDraggable(IDraggable draggable) {
        if (draggable == null) {
            return null;
        }
        return (Entity) draggable;
    }

    public static Vector getVelocityProper(Vector improperVelocity, Entity thisClassAsAnEntity) {
        double x = improperVelocity.X;
        double y = improperVelocity.Y;
        double z = improperVelocity.Z;

        if (thisClassAsAnEntity.isInWeb) {
            thisClassAsAnEntity.isInWeb = false;
            x *= 0.25D;
            y *= 0.05000000074505806D;
            z *= 0.25D;
            thisClassAsAnEntity.motionX = 0.0D;
            thisClassAsAnEntity.motionY = 0.0D;
            thisClassAsAnEntity.motionZ = 0.0D;
        }

        double d2 = x;
        double d3 = y;
        double d4 = z;

        AxisAlignedBB potentialCrashBB = thisClassAsAnEntity.getEntityBoundingBox().offset(x, y, z);

        // TODO: This is a band aid not a solution
        if (potentialCrashBB.getAverageEdgeLength() > 999999) {
            // The player went too fast, something is wrong.
            System.err.println("Entity with ID " + thisClassAsAnEntity.getEntityId() + " went way too fast! Reseting its position.");
            return new Vector();
        }

        List<AxisAlignedBB> list1 = thisClassAsAnEntity.world.getCollisionBoxes(thisClassAsAnEntity, potentialCrashBB);
        AxisAlignedBB axisalignedbb = thisClassAsAnEntity.getEntityBoundingBox();

        if (y != 0.0D) {
            int k = 0;

            for (int l = list1.size(); k < l; ++k) {
                y = list1.get(k).calculateYOffset(thisClassAsAnEntity.getEntityBoundingBox(), y);
            }

            thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, y, 0.0D));
        }

        if (x != 0.0D) {
            int j5 = 0;

            for (int l5 = list1.size(); j5 < l5; ++j5) {
                x = list1.get(j5).calculateXOffset(thisClassAsAnEntity.getEntityBoundingBox(), x);
            }

            if (x != 0.0D) {
                thisClassAsAnEntity
                        .setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
            }
        }

        if (z != 0.0D) {
            int k5 = 0;

            for (int i6 = list1.size(); k5 < i6; ++k5) {
                z = list1.get(k5).calculateZOffset(thisClassAsAnEntity.getEntityBoundingBox(), z);
            }

            if (z != 0.0D) {
                thisClassAsAnEntity
                        .setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, 0.0D, z));
            }
        }

        boolean flag = thisClassAsAnEntity.onGround || d3 != y && d3 < 0.0D;

        if (thisClassAsAnEntity.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
            double d14 = x;
            double d6 = y;
            double d7 = z;
            AxisAlignedBB axisalignedbb1 = thisClassAsAnEntity.getEntityBoundingBox();
            thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb);
            y = thisClassAsAnEntity.stepHeight;
            List<AxisAlignedBB> list = thisClassAsAnEntity.world.getCollisionBoxes(thisClassAsAnEntity,
                    thisClassAsAnEntity.getEntityBoundingBox().offset(d2, y, d4));
            AxisAlignedBB axisalignedbb2 = thisClassAsAnEntity.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb3 = axisalignedbb2.offset(d2, 0.0D, d4);
            double d8 = y;
            int j1 = 0;

            for (int k1 = list.size(); j1 < k1; ++j1) {
                d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
            double d18 = d2;
            int l1 = 0;

            for (int i2 = list.size(); l1 < i2; ++l1) {
                d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
            }

            axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
            double d19 = d4;
            int j2 = 0;

            for (int k2 = list.size(); j2 < k2; ++j2) {
                d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
            AxisAlignedBB axisalignedbb4 = thisClassAsAnEntity.getEntityBoundingBox();
            double d20 = y;
            int l2 = 0;

            for (int i3 = list.size(); l2 < i3; ++l2) {
                d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
            double d21 = d2;
            int j3 = 0;

            for (int k3 = list.size(); j3 < k3; ++j3) {
                d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
            }

            axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
            double d22 = d4;
            int l3 = 0;

            for (int i4 = list.size(); l3 < i4; ++l3) {
                d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
            double d23 = d18 * d18 + d19 * d19;
            double d9 = d21 * d21 + d22 * d22;

            if (d23 > d9) {
                x = d18;
                z = d19;
                y = -d8;
                thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb2);
            } else {
                x = d21;
                z = d22;
                y = -d20;
                thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb4);
            }

            int j4 = 0;

            for (int k4 = list.size(); j4 < k4; ++j4) {
                y = list.get(j4).calculateYOffset(thisClassAsAnEntity.getEntityBoundingBox(), y);
            }

            thisClassAsAnEntity.setEntityBoundingBox(thisClassAsAnEntity.getEntityBoundingBox().offset(0.0D, y, 0.0D));

            if (d14 * d14 + d7 * d7 >= x * x + z * z) {
                x = d14;
                y = d6;
                z = d7;
                thisClassAsAnEntity.setEntityBoundingBox(axisalignedbb1);
            }
        }

        return new Vector(x, y, z);
    }
}
