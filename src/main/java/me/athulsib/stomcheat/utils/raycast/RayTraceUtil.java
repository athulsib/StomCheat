package me.athulsib.stomcheat.utils.raycast;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.block.BlockIterator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class RayTraceUtil {

    // Cache for block collision shapes to improve performance
    private static final Map<Block, List<BoundingBox>> blockCollisionCache = new ConcurrentHashMap<>();

    // Ray trace configuration
    public static class RayTraceConfig {
        private double maxDistance = 100.0;
        private double stepSize = 0.1;
        private boolean ignoreLiquids = true;
        private boolean ignorePassable = true;
        private Predicate<Block> blockFilter = block -> true;
        private Predicate<Entity> entityFilter = entity -> true;
        private boolean includeEntities = true;
        private boolean includeBlocks = true;

        public RayTraceConfig maxDistance(double maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public RayTraceConfig stepSize(double stepSize) {
            this.stepSize = stepSize;
            return this;
        }

        public RayTraceConfig ignoreLiquids(boolean ignoreLiquids) {
            this.ignoreLiquids = ignoreLiquids;
            return this;
        }

        public RayTraceConfig ignorePassable(boolean ignorePassable) {
            this.ignorePassable = ignorePassable;
            return this;
        }

        public RayTraceConfig blockFilter(Predicate<Block> blockFilter) {
            this.blockFilter = blockFilter;
            return this;
        }

        public RayTraceConfig entityFilter(Predicate<Entity> entityFilter) {
            this.entityFilter = entityFilter;
            return this;
        }

        public RayTraceConfig includeEntities(boolean includeEntities) {
            this.includeEntities = includeEntities;
            return this;
        }

        public RayTraceConfig includeBlocks(boolean includeBlocks) {
            this.includeBlocks = includeBlocks;
            return this;
        }
    }

    // Ray trace result
    public static class RayTraceResult {
        private final Point hitPosition;
        private final Block hitBlock;
        private final Entity hitEntity;
        private final BlockFace hitFace;
        private final double distance;

        public RayTraceResult(Point hitPosition, Block hitBlock, Entity hitEntity, BlockFace hitFace, double distance) {
            this.hitPosition = hitPosition;
            this.hitBlock = hitBlock;
            this.hitEntity = hitEntity;
            this.hitFace = hitFace;
            this.distance = distance;
        }

        public Point getHitPosition() {
            return hitPosition;
        }

        public Block getHitBlock() {
            return hitBlock;
        }

        public Entity getHitEntity() {
            return hitEntity;
        }

        public BlockFace getHitFace() {
            return hitFace;
        }

        public double getDistance() {
            return distance;
        }

        public boolean isBlockHit() {
            return hitBlock != null;
        }

        public boolean isEntityHit() {
            return hitEntity != null;
        }
    }

    // Bounding box representation
    public static class BoundingBox {
        private final double minX, minY, minZ;
        private final double maxX, maxY, maxZ;

        public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public boolean intersects(Point start, Vec direction, double maxDistance) {
            // Ray-AABB intersection using slab method
            double tMin = 0.0;
            double tMax = maxDistance;

            // X-axis
            if (Math.abs(direction.x()) < 1e-6) {
                if (start.x() < minX || start.x() > maxX) {
                    return false;
                }
            } else {
                double t1 = (minX - start.x()) / direction.x();
                double t2 = (maxX - start.x()) / direction.x();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return false;
                }
            }

            // Y-axis
            if (Math.abs(direction.y()) < 1e-6) {
                if (start.y() < minY || start.y() > maxY) {
                    return false;
                }
            } else {
                double t1 = (minY - start.y()) / direction.y();
                double t2 = (maxY - start.y()) / direction.y();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return false;
                }
            }

            // Z-axis
            if (Math.abs(direction.z()) < 1e-6) {
                if (start.z() < minZ || start.z() > maxZ) {
                    return false;
                }
            } else {
                double t1 = (minZ - start.z()) / direction.z();
                double t2 = (maxZ - start.z()) / direction.z();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return false;
                }
            }

            return true;
        }

        public Point getIntersectionPoint(Point start, Vec direction) {
            // Calculate the exact intersection point
            double tMin = Double.NEGATIVE_INFINITY;
            double tMax = Double.POSITIVE_INFINITY;

            // X-axis
            if (Math.abs(direction.x()) < 1e-6) {
                if (start.x() < minX || start.x() > maxX) {
                    return null;
                }
            } else {
                double t1 = (minX - start.x()) / direction.x();
                double t2 = (maxX - start.x()) / direction.x();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return null;
                }
            }

            // Y-axis
            if (Math.abs(direction.y()) < 1e-6) {
                if (start.y() < minY || start.y() > maxY) {
                    return null;
                }
            } else {
                double t1 = (minY - start.y()) / direction.y();
                double t2 = (maxY - start.y()) / direction.y();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return null;
                }
            }

            // Z-axis
            if (Math.abs(direction.z()) < 1e-6) {
                if (start.z() < minZ || start.z() > maxZ) {
                    return null;
                }
            } else {
                double t1 = (minZ - start.z()) / direction.z();
                double t2 = (maxZ - start.z()) / direction.z();

                if (t1 > t2) {
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) {
                    return null;
                }
            }

            return start.add(direction.mul(tMin));
        }
    }

    /**
     * Perform a ray trace from start position in the given direction
     */
    public static RayTraceResult rayTrace(Instance instance, Point start, Vec direction, RayTraceConfig config) {
        // Normalize direction vector
        direction = direction.normalize();

        RayTraceResult closestBlockHit = null;
        RayTraceResult closestEntityHit = null;

        // Block tracing
        if (config.includeBlocks) {
            closestBlockHit = traceBlocks(instance, start, direction, config);
        }

        // Entity tracing
        if (config.includeEntities) {
            closestEntityHit = traceEntities(instance, start, direction, config);
        }

        // Return the closest hit
        if (closestBlockHit == null && closestEntityHit == null) {
            return null;
        } else if (closestBlockHit == null) {
            return closestEntityHit;
        } else if (closestEntityHit == null) {
            return closestBlockHit;
        } else {
            return closestBlockHit.distance < closestEntityHit.distance ? closestBlockHit : closestEntityHit;
        }
    }

    /**
     * Trace blocks along the ray
     */
    private static RayTraceResult traceBlocks(Instance instance, Point start, Vec direction, RayTraceConfig config) {
        // Use Minestom's BlockIterator for efficient block traversal
        // Convert start to Vec if needed
        Vec startVec = start instanceof Vec ? (Vec) start : new Vec(start.x(), start.y(), start.z());
        BlockIterator iterator = new BlockIterator(startVec, direction, 0.0, config.maxDistance, false);

        Point previousPos = start;
        RayTraceResult closestHit = null;

        while (iterator.hasNext()) {
            Point blockPos = iterator.next();
            Block block = instance.getBlock(blockPos);

            // Skip if block doesn't pass filter
            if (!config.blockFilter.test(block)) {
                continue;
            }

            // Skip liquids if configured
            if (config.ignoreLiquids && block.isLiquid()) {
                continue;
            }

            // Skip passable blocks if configured
            if (config.ignorePassable) {
                // Check if the block has collision by trying to get its collision boxes
                List<BoundingBox> boxes = getCollisionBoxes(block, blockPos);
                if (boxes.isEmpty()) {
                    continue;
                }
            }

            // Get collision shape for the block
            List<BoundingBox> boxes = getCollisionBoxes(block, blockPos);

            // Check each bounding box
            for (BoundingBox box : boxes) {
                if (box.intersects(start, direction, config.maxDistance)) {
                    Point hitPos = box.getIntersectionPoint(start, direction);
                    if (hitPos == null) continue;

                    double distance = start.distance(hitPos);

                    // Skip if beyond max distance
                    if (distance > config.maxDistance) {
                        continue;
                    }

                    // Calculate hit face
                    BlockFace face = calculateHitFace(blockPos, hitPos, previousPos);

                    // Update closest hit
                    if (closestHit == null || distance < closestHit.distance) {
                        closestHit = new RayTraceResult(hitPos, block, null, face, distance);
                    }
                }
            }

            previousPos = blockPos;
        }

        return closestHit;
    }

    /**
     * Trace entities along the ray
     */
    private static RayTraceResult traceEntities(Instance instance, Point start, Vec direction, RayTraceConfig config) {
        RayTraceResult closestHit = null;

        // Get entities near the ray path
        List<Entity> entities = (List<Entity>) instance.getNearbyEntities(start, config.maxDistance);

        for (Entity entity : entities) {
            // Skip if entity doesn't pass filter
            if (!config.entityFilter.test(entity)) {
                continue;
            }

            // Skip non-living entities if needed
            if (entity.getEntityType() == EntityType.ITEM ||
                    entity.getEntityType() == EntityType.EXPERIENCE_ORB) {
                continue;
            }

            // Create bounding box for entity
            BoundingBox box = new BoundingBox(
                    entity.getPosition().x() - entity.getBoundingBox().width() / 2,
                    entity.getPosition().y(),
                    entity.getPosition().z() - entity.getBoundingBox().depth() / 2,
                    entity.getPosition().x() + entity.getBoundingBox().width() / 2,
                    entity.getPosition().y() + entity.getBoundingBox().height(),
                    entity.getPosition().z() + entity.getBoundingBox().depth() / 2
            );

            // Check intersection
            if (box.intersects(start, direction, config.maxDistance)) {
                Point hitPos = box.getIntersectionPoint(start, direction);
                if (hitPos == null) continue;

                double distance = start.distance(hitPos);

                // Skip if beyond max distance
                if (distance > config.maxDistance) {
                    continue;
                }

                // Update closest hit
                if (closestHit == null || distance < closestHit.distance) {
                    closestHit = new RayTraceResult(hitPos, null, entity, null, distance);
                }
            }
        }

        return closestHit;
    }

    /**
     * Get collision boxes for a block with caching
     */
    private static List<BoundingBox> getCollisionBoxes(Block block, Point position) {
        // Check cache first
        if (blockCollisionCache.containsKey(block)) {
            List<BoundingBox> cached = blockCollisionCache.get(block);
            List<BoundingBox> result = new ArrayList<>(cached.size());

            // Translate cached boxes to block position
            for (BoundingBox box : cached) {
                result.add(new BoundingBox(
                        box.minX + position.x(),
                        box.minY + position.y(),
                        box.minZ + position.z(),
                        box.maxX + position.x(),
                        box.maxY + position.y(),
                        box.maxZ + position.z()
                ));
            }

            return result;
        }

        // Get collision shape from block
        List<BoundingBox> boxes = new ArrayList<>();

        // For air blocks, return empty list
        if (block.isAir()) {
            blockCollisionCache.put(block, boxes);
            return boxes;
        }

        // For full blocks, create a simple bounding box
        // Check if the block has a solid collision shape
        try {
            // Try to determine if the block is solid by checking its properties
            if (block.isSolid()) {
                boxes.add(new BoundingBox(0, 0, 0, 1, 1, 1));
            } else {
                // For non-solid blocks, we'll create a minimal bounding box
                // This is a simplified approach - in a real implementation,
                // you would parse the actual collision shape
                boxes.add(new BoundingBox(0.3, 0, 0.3, 0.7, 0.6, 0.7));
            }
        } catch (Exception e) {
            // If we can't determine, use a full block
            boxes.add(new BoundingBox(0, 0, 0, 1, 1, 1));
        }

        // Cache the result
        blockCollisionCache.put(block, boxes);

        // Translate to block position
        List<BoundingBox> result = new ArrayList<>(boxes.size());
        for (BoundingBox box : boxes) {
            result.add(new BoundingBox(
                    box.minX + position.x(),
                    box.minY + position.y(),
                    box.minZ + position.z(),
                    box.maxX + position.x(),
                    box.maxY + position.y(),
                    box.maxZ + position.z()
            ));
        }

        return result;
    }

    /**
     * Calculate which face of a block was hit
     */
    private static BlockFace calculateHitFace(Point blockPos, Point hitPos, Point previousPos) {
        // Calculate the center of the block
        double centerX = blockPos.x() + 0.5;
        double centerY = blockPos.y() + 0.5;
        double centerZ = blockPos.z() + 0.5;

        // Calculate the direction from the center to the hit position
        double dx = hitPos.x() - centerX;
        double dy = hitPos.y() - centerY;
        double dz = hitPos.z() - centerZ;

        // Determine which axis has the greatest absolute value
        double absX = Math.abs(dx);
        double absY = Math.abs(dy);
        double absZ = Math.abs(dz);

        if (absX > absY && absX > absZ) {
            return dx > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else if (absY > absZ) {
            return dy > 0 ? BlockFace.TOP : BlockFace.BOTTOM;
        } else {
            return dz > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }

    /**
     * Clear the collision shape cache
     */
    public static void clearCache() {
        blockCollisionCache.clear();
    }
}