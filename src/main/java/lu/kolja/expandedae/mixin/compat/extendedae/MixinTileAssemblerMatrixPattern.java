package lu.kolja.expandedae.mixin.compat.extendedae;

import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixFunction;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lu.kolja.expandedae.block.entity.ExpandedMatrixPatternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TileAssemblerMatrixPattern.class, remap = false)
public class MixinTileAssemblerMatrixPattern {
    @Invoker("<init>")
    private TileAssemblerMatrixPattern invokeConstructor(BlockPos pos, BlockState blockState) {
        return null;
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/glodblock/github/extendedae/common/tileentities/matrix/TileAssemblerMatrixFunction;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private static void extendedae$redirect(TileAssemblerMatrixFunction instance, BlockEntityType type, BlockPos pos, BlockState blockState) {
        if (instance instanceof ExpandedMatrixPatternBlockEntity exp) {
            ((MixinTileAssemblerMatrixPattern) (Object) exp).invokeConstructor(pos, blockState);
        }
    }

    @ModifyReturnValue(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/glodblock/github/glodium/util/GlodUtil;getTileType(Ljava/lang/Class;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/entity/BlockEntityType;"
            )
    )
    private static BlockEntityType<?> expandedae$modifyReturnValue(BlockEntityType<?> type) {

    }
}
