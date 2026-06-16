package ughdhdh.bigtires.content.blocks.dye_station;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import ughdhdh.bigtires.index.BigTiresBlockEntityTypes;

/**
 * Блок покрасочной станции для колёс.
 * Открывает GUI ({@link WheelDyeStationScreen}) при правом клике.
 */
public class WheelDyeStationBlock extends BaseEntityBlock {

    public static final MapCodec<WheelDyeStationBlock> CODEC = simpleCodec(WheelDyeStationBlock::new);

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 14, 16);

    public WheelDyeStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends WheelDyeStationBlock> codec() {
        return CODEC;
    }

    // ── BlockEntity ───────────────────────────────────────────────────────────

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WheelDyeStationBlockEntity(BigTiresBlockEntityTypes.WHEEL_DYE_STATION.get(), pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Взаимодействие ────────────────────────────────────────────────────────

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof WheelDyeStationBlockEntity dyeStation && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(dyeStation);
        }
        return InteractionResult.CONSUME;
    }

    // ── Форма ─────────────────────────────────────────────────────────────────

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ── Выпадение предметов при разрушении ────────────────────────────────────

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof WheelDyeStationBlockEntity be) {
                // Выбрасываем содержимое на землю
                for (int i = 0; i < be.getContainerSize(); i++) {
                    var stack = be.getItem(i);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
