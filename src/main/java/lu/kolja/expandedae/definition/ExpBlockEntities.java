package lu.kolja.expandedae.definition;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.BlockDefinition;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.block.entity.ExpPatternProviderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ExpBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> DR =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Expandedae.MODID);
    private static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashMap<>();
    public static final Supplier<BlockEntityType<ExpPatternProviderBlockEntity>> EXP_PATTERN_PROVIDER = create(
            "exp_pattern_provider",
            ExpPatternProviderBlockEntity.class,
            ExpPatternProviderBlockEntity::new,
            ExpBlocks.EXP_PATTERN_PROVIDER
    );

    public static final Supplier<BlockEntityType<CraftingBlockEntity>> EXP_CPUS = create(
            "exp_cpus",
            CraftingBlockEntity.class,
            CraftingBlockEntity::new,
            ExpBlocks.UNIT,
            ExpBlocks.CPU_2,
            ExpBlocks.CPU_4,
            ExpBlocks.CPU_8,
            ExpBlocks.CPU_16,
            ExpBlocks.CPU_32,
            ExpBlocks.CPU_64,
            ExpBlocks.CPU_128,
            ExpBlocks.CPU_256,
            ExpBlocks.CPU_512,
            ExpBlocks.CPU_1K,
            ExpBlocks.CPU_2K,
            ExpBlocks.CPU_4K,
            ExpBlocks.CPU_8K,
            ExpBlocks.CPU_16K,
            ExpBlocks.CPU_32K,
            ExpBlocks.CPU_64K,
            ExpBlocks.CPU_128K,
            ExpBlocks.CPU_256K,
            ExpBlocks.CPU_512K,
            ExpBlocks.CPU_1M
    );

    public static Map<ResourceLocation, BlockEntityType<?>> getBlockEntityTypes() {
        return Collections.unmodifiableMap(BLOCK_ENTITY_TYPES);
    }

    @SafeVarargs
    private static <T extends AEBaseBlockEntity> Supplier<BlockEntityType<T>> create(
            String id,
            Class<T> entityClass,
            BlockEntityFactory<T> factory,
            BlockDefinition<? extends AEBaseEntityBlock<?>>... blockDefs) {
        if (blockDefs.length == 0) {
            throw new IllegalArgumentException();
        }

        return DR.register(id, () -> {
            var blocks = Arrays.stream(blockDefs).map(BlockDefinition::block).toArray(AEBaseEntityBlock[]::new);

            var typeHolder = new AtomicReference<BlockEntityType<T>>();
            var type = BlockEntityType.Builder.of((pos, state) -> factory.create(typeHolder.get(), pos, state), blocks)
                    .build(null);
            typeHolder.set(type);

            AEBaseBlockEntity.registerBlockEntityItem(type, blockDefs[0].asItem());

            for (var block : blocks) {
                block.setBlockEntity(entityClass, type, null, null);
            }

            return type;
        });
    }

    public interface BlockEntityFactory<T extends AEBaseBlockEntity> {
        T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
    }
}
