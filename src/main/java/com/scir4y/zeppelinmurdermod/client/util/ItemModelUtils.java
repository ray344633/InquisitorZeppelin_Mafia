package com.scir4y.zeppelinmurdermod.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CLIENT-ONLY utility.
 * Computes a hitbox size (width/height) for a dropped item based on its
 * actual model geometry:
 *  - BlockItem            -> uses the block's collision shape (real block bounds)
 *  - 3D custom item model -> walks the baked quads and measures their bounding box
 *  - flat 2D item model   -> uses the sprite's pixel aspect ratio
 *
 * Results are cached per Item so the (relatively expensive) quad-walking
 * calculation only happens once per item type, not once per frame/tick.
 *
 * IMPORTANT: only call this from client-side code. BakedModel data does not
 * exist on the logical server.
 */
public final class ItemModelUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Fallback size used when a model can't be resolved for any reason.
    private static final EntityDimensions FALLBACK = EntityDimensions.scalable(0.25F, 0.25F);

    // Cache: Item -> computed hitbox dimensions. Cleared automatically if the
    // client resource manager reloads (see clearCache()).
    private static final Map<Item, EntityDimensions> CACHE = new ConcurrentHashMap<>();

    private ItemModelUtils() {
    }

    /**
     * Call this from a resource-reload listener (or on ClientLifecycleEvent /
     * texture-stitch) so stale entries don't survive a resource pack swap.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Returns cached (or freshly computed) hitbox dimensions for the given stack.
     * Caches by Item, so different stack sizes / NBT of the same item share one result.
     */
    public static EntityDimensions getOrComputeDimensions(ItemStack stack) {
        if (stack.isEmpty()) {
            return FALLBACK;
        }
        return CACHE.computeIfAbsent(stack.getItem(), item -> computeDimensions(stack));
    }

    // ---------------------------------------------------------------------
    // Computation
    // ---------------------------------------------------------------------

    private static EntityDimensions computeDimensions(ItemStack stack) {
        try {
            EntityDimensions result;
            if (stack.getItem() instanceof BlockItem blockItem) {
                result = computeBlockDimensions(blockItem);
            } else {
                result = computeItemModelDimensions(stack);
            }
            LOGGER.info("[ItemModelUtils] computed for {}: width={}, height={}",
                    stack.getItem(), result.width(), result.height());
            return result;
        } catch (Exception e) {
            // Never let a bad model crash entity size calculation.
            LOGGER.warn("[ItemModelUtils] FAILED to compute dimensions for {}, using fallback", stack.getItem(), e);
            return FALLBACK;
        }
    }

    private static EntityDimensions computeBlockDimensions(BlockItem blockItem) {
        // getCollisionShape(BlockGetter level, BlockPos pos) tolerates null
        // level/pos for a "default state, no world context" query.
        VoxelShape collisionShape = blockItem.getBlock().defaultBlockState()
                .getCollisionShape(null, null);

        float width;
        float height;

        if (!collisionShape.isEmpty()) {
            AABB bounds = collisionShape.bounds();
            width = (float) Math.max(bounds.getXsize(), bounds.getZsize());
            height = (float) bounds.getYsize();
        } else {
            // Shapeless collision (e.g. torches, flowers) — use a small default footprint.
            width = 0.5F;
            height = 0.5F;
        }

        return EntityDimensions.scalable(clamp(width), clamp(height));
    }

    private static EntityDimensions computeItemModelDimensions(ItemStack stack) {
        BakedModel bakedModel = Minecraft.getInstance().getItemRenderer()
                .getModel(stack, null, null, 0);

        if (bakedModel == null) {
            return FALLBACK;
        }

        if (bakedModel.isGui3d()) {
            AABB bounds = calculateQuadBounds(bakedModel);
            float width = (float) Math.max(bounds.getXsize(), bounds.getZsize());
            float height = (float) bounds.getYsize();
            return EntityDimensions.scalable(clamp(width), clamp(height));
        }

        // Flat 2D sprite (most vanilla items: apples, ingots, tools, etc.)
        var particleIcon = bakedModel.getParticleIcon();
        if (particleIcon != null && particleIcon.contents() != null) {
            int texWidth = particleIcon.contents().width();
            int texHeight = particleIcon.contents().height();
            // Keep the vanilla ~0.25 footprint but scale by the sprite's
            // aspect ratio relative to a standard 16x16 icon.
            float width = 0.25F * (texWidth / 16.0F);
            float height = 0.05F * (texHeight / 16.0F);
            return EntityDimensions.scalable(clamp(width), clamp(height));
        }

        return FALLBACK;
    }

    /**
     * Walks every quad of every facing direction and measures the resulting
     * bounding box in model space (0-1 cube, matching vanilla block/item model units).
     */
    private static AABB calculateQuadBounds(BakedModel model) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        // Fixed seed: we only care about geometry, not random model variants.
        RandomSource random = RandomSource.create(42L);

        // null direction = "no culling", i.e. quads not tied to a specific face.
        Direction[] directionsPlusNull = new Direction[]{
                null, Direction.DOWN, Direction.UP, Direction.NORTH,
                Direction.SOUTH, Direction.WEST, Direction.EAST
        };

        boolean foundAnyVertex = false;

        for (Direction direction : directionsPlusNull) {
            List<BakedQuad> quads = model.getQuads(null, direction, random);
            for (BakedQuad quad : quads) {
                int[] vertexData = quad.getVertices();
                // Vertex format stride is 8 ints per vertex:
                // [x, y, z, color, u, v, lightmap, normal]
                for (int i = 0; i + 2 < vertexData.length; i += 8) {
                    float x = Float.intBitsToFloat(vertexData[i]);
                    float y = Float.intBitsToFloat(vertexData[i + 1]);
                    float z = Float.intBitsToFloat(vertexData[i + 2]);

                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                    foundAnyVertex = true;
                }
            }
        }

        if (!foundAnyVertex) {
            // No geometry found (unusual, but guard against it) — assume a
            // standard full-size model.
            return new AABB(0, 0, 0, 1, 1, 1);
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static float clamp(float value) {
        return Math.max(0.05F, Math.min(2.0F, value));
    }
}