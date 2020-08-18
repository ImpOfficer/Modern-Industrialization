package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.tools.IWrenchable;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;

public class PipeBlock extends Block implements BlockEntityProvider, IWrenchable {
    public PipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new PipeBlockEntity();
    }



    @Override
    public ActionResult onWrenchUse(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);

        Vec3d hitPos = context.getHitPos();
        for(PipeVoxelShape partShape : pipeEntity.getPartShapes()) {
            Vec3d posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for(Box box : partShape.shape.getBoundingBoxes()) {
                // move slightly towards box center
                Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
                if(box.contains(posInBlock.add(dir))) {
                    if(player != null && player.isSneaking()) {
                        boolean removeBlock = pipeEntity.renderedConnections.size() == 1;
                        if(!world.isClient) {
                            pipeEntity.removePipe(partShape.type);
                        }
                        if(removeBlock) {
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                        }
                        // update adjacent blocks
                        world.updateNeighbors(blockPos, null);
                        // spawn pipe item
                        world.spawnEntity(new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
                        // play break sound
                        BlockSoundGroup group = world.getBlockState(blockPos).getSoundGroup();
                        world.playSound(player, blockPos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F);
                        return ActionResult.success(world.isClient);
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        return super.getDroppedStacks(state, builder); // TODO: drops
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            ((PipeBlockEntity) world.getBlockEntity(pos)).updateConnections();
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean hasDynamicBounds() {
        return true;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        if (entity != null) {
            double[] smallestDistance = new double[]{10000};
            VoxelShape[] closestShape = new VoxelShape[]{null};

            for (PipeVoxelShape pipePartShape : entity.getPartShapes()) {
                VoxelShape partShape = pipePartShape.shape;
                assert (world instanceof ClientWorld);
                float tickDelta = 0; // TODO: fix this
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                Vec3d vec3d = player.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = player.getRotationVec(tickDelta);
                double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = partShape.rayTrace(vec3d, vec3d3, pos);
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    double dist = hit.getPos().distanceTo(vec3d);
                    if (dist < smallestDistance[0]) {
                        smallestDistance[0] = dist;
                        closestShape[0] = partShape;
                    }
                }
            }

            if (closestShape[0] != null) {
                return closestShape[0];
            }
        }

        return PipeBlockEntity.DEFAULT_SHAPE;
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        return entity == null ? PipeBlockEntity.DEFAULT_SHAPE : entity.currentCollisionShape;
    }
}
