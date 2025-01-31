package net.mehvahdjukaar.moonlight.api.block;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MehVahdJukaar
 * Used for blocks that can be lit up. Implement for best compatibility. Do not modify
 */
public interface ILightable {

    TagKey<Item> FLINT_AND_STEELS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/flint_and_steel"));

    default boolean isLitUp(BlockState state, BlockGetter level, BlockPos pos) {
        return isLitUp(state);
    }

    default void setLitUp(BlockState state, LevelAccessor world, BlockPos pos, boolean lit) {
        world.setBlock(pos, toggleLitState(state, lit), 3);
    }

    @Deprecated(forRemoval = true)
    default boolean isLitUp(BlockState state) {
        return false;
    }

    @Deprecated(forRemoval = true)
    default BlockState toggleLitState(BlockState state, boolean lit) {
        return state;
    }

    default boolean lightUp(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world, FireSourceType fireSourceType) {
        if (!isLitUp(state, world, pos)) {
            if (!world.isClientSide()) {
                this.setLitUp(state, world, pos, true);
                playLightUpSound(world, pos, fireSourceType);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    default boolean extinguish(@Nullable Entity player, BlockState state, BlockPos pos, LevelAccessor world) {
        if (this.isLitUp(state, world, pos)) {
            if (!world.isClientSide()) {
                playExtinguishSound(world, pos);
                this.setLitUp(state, world, pos, false);
            } else {
                spawnSmokeParticles(state, pos, world);
            }
            world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }


    //true on state change
    default boolean interactWithProjectile(Level level, BlockState state, Projectile projectile, BlockPos pos) {
        if (projectile.isOnFire()) {
            Entity entity = projectile.getOwner();
            if (entity == null || entity instanceof Player || PlatHelper.isMobGriefingOn(level, entity)) {
                if (lightUp(projectile, state, pos, level, FireSourceType.FLAMING_ARROW)) {
                    return true;
                }
            }
        }
        // Now handled by mixin since it needs bigger radius
        /*
        else if (projectile instanceof ThrownPotion potion && PotionUtils.getPotion(potion.getItem()) == Potions.WATER) {
            Entity entity = projectile.getOwner();
            boolean flag = entity == null || entity instanceof Player || PlatHelper.isMobGriefingOn(level, entity);
            if (flag && extinguish(projectile, state, pos, level)) {
                return true;
            }
        }*/
        return false;
    }

    //call on use
    default InteractionResult interactWithPlayer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if (Utils.mayPerformBlockAction(player, pos, stack)) {
            if (!this.isLitUp(state, level, pos)) {
                Item item = stack.getItem();
                if (item instanceof FlintAndSteelItem || stack.is(FLINT_AND_STEELS)) {
                    if (lightUp(player, state, pos, level, FireSourceType.FLINT_AND_STEEL)) {
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else if (item instanceof FireChargeItem) {
                    if (lightUp(player, state, pos, level, FireSourceType.FIRE_CHANGE)) {
                        stack.hurtAndBreak(1, player, (playerIn) -> playerIn.broadcastBreakEvent(handIn));
                        if (!player.isCreative()) stack.shrink(1);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            } else if (this.canBeExtinguishedBy(stack)) {
                if (extinguish(player, state, pos, level)) {
                    if (!(stack.getItem() instanceof BrushItem)) {
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    default boolean canBeExtinguishedBy(ItemStack item) {
        return item.getItem() instanceof ShovelItem || item.getItem() instanceof BrushItem;
    }

    default void playLightUpSound(LevelAccessor world, BlockPos pos, FireSourceType type) {
        type.play(world, pos);
    }

    default void playExtinguishSound(LevelAccessor world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.5F);
    }

    default void spawnSmokeParticles(BlockState state, BlockPos pos, LevelAccessor world) {
        RandomSource random = world.getRandom();
        for (int i = 0; i < 10; ++i) {
            //particle offset
            world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25f + random.nextFloat() * 0.5f, pos.getY() + 0.35f + random.nextFloat() * 0.5f, pos.getZ() + 0.25f + random.nextFloat() * 0.5f, 0, 0.005, 0);
        }
    }

    //TODO: rename in 1.21
    @FunctionalInterface
    interface FireSourceType {
        FireSourceType FLINT_AND_STEEL = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        FireSourceType FIRE_CHANGE = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);

        FireSourceType FLAMING_ARROW = (level, pos) ->
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);

        void play(LevelAccessor level, BlockPos pos);

    }

}
