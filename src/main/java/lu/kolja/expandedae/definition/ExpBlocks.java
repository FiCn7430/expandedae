package lu.kolja.expandedae.definition;

import appeng.block.crafting.CraftingUnitBlock;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.block.ExpPatternProviderBlock;
import lu.kolja.expandedae.block.ExpPatternProviderBlockItem;
import lu.kolja.expandedae.enums.ExpCraftingCPU;
import lu.kolja.expandedae.item.dummy.DummyCPU;
import lu.kolja.expandedae.item.misc.ExpCPUItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ExpBlocks {
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(Expandedae.MODID);
    private static final Map<ExpCraftingCPU,BlockDefinition<CraftingUnitBlock>> CPUS = new HashMap<>();

    public static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();
    public static final BlockDefinition<ExpPatternProviderBlock> EXP_PATTERN_PROVIDER = block(
            "Expanded Pattern Provider",
            "exp_pattern_provider",
            ExpPatternProviderBlock::new,
            ExpPatternProviderBlockItem::new
    );

    public static BlockDefinition<CraftingUnitBlock> CPU_2 = cpu(
            "2x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_2",
            ExpCraftingCPU.THREADS_2
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_4 = cpu(
            "4x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_4",
            ExpCraftingCPU.THREADS_4
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_8 = cpu(
            "8x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_8",
            ExpCraftingCPU.THREADS_8
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_16 = cpu(
            "16x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_16",
            ExpCraftingCPU.THREADS_16
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_32 = cpu(
            "32x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_32",
            ExpCraftingCPU.THREADS_32
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_64 = cpu(
            "64x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_64",
            ExpCraftingCPU.THREADS_64
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_128 = cpu(
            "128x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_128",
            ExpCraftingCPU.THREADS_128
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_256 = cpu(
            "256x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_256",
            ExpCraftingCPU.THREADS_256
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_512 = cpu(
            "512x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_512",
            ExpCraftingCPU.THREADS_512
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_1K = cpu(
            "1K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_1k",
            ExpCraftingCPU.THREADS_1K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_2K = cpu(
            "2K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_2k",
            ExpCraftingCPU.THREADS_2K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_4K = cpu(
            "4K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_4k",
            ExpCraftingCPU.THREADS_4K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_8K = cpu(
            "8K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_8k",
            ExpCraftingCPU.THREADS_8K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_16K = cpu(
            "16K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_16k",
            ExpCraftingCPU.THREADS_16K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_32K = cpu(
            "32K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_32k",
            ExpCraftingCPU.THREADS_32K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_64K = cpu(
            "64K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_64k",
            ExpCraftingCPU.THREADS_64K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_128K = cpu(
            "128K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_128k",
            ExpCraftingCPU.THREADS_128K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_256K = cpu(
            "256K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_256k",
            ExpCraftingCPU.THREADS_256K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_512K = cpu(
            "512K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_512k",
            ExpCraftingCPU.THREADS_512K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_1M = cpu(
            "1M Crafting Co-Processing Unit",
            "exp_crafting_accelerator_1m",
            ExpCraftingCPU.THREADS_1M
    );

    public static BlockDefinition<CraftingUnitBlock> cpu(
            String englishName, String id, ExpCraftingCPU cpu
    ) {
        var def = block(
                englishName, id, true,
                () -> new CraftingUnitBlock(cpu),
                (block, props) -> cpu.isEnabled()
                        ? new ExpCPUItem(block, props)
                        : new DummyCPU(block, props)
        );
        CPUS.put(cpu, def);
        return def;
    }

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }
    public static Map<ExpCraftingCPU,BlockDefinition<CraftingUnitBlock>> getCPUs() {
        return Collections.unmodifiableMap(CPUS);
    }

    private static <T extends Block> BlockDefinition<T> block(
            String englishName,
            String id,
            Supplier<T> blockSupplier,
            BiFunction<Block, Item.Properties, BlockItem> itemFactory) {
        var block = DR.register(id, blockSupplier);
        var item = ExpItems.DR.register(id, () -> itemFactory.apply(block.get(), new Item.Properties()));

        var definition = new BlockDefinition<>(englishName, block, new ItemDefinition<>(englishName, item));
        BLOCKS.add(definition);
        return definition;
    }

    private static <T extends Block> BlockDefinition<T> block(
            String englishName,
            String id,
            boolean addToTab,
            Supplier<T> blockSupplier,
            BiFunction<Block, Item.Properties, BlockItem> itemFactory) {
        var block = DR.register(id, blockSupplier);
        var item = ExpItems.DR.register(id, () -> itemFactory.apply(block.get(), new Item.Properties()));

        var definition = new BlockDefinition<>(englishName, block, new ItemDefinition<>(englishName, item));
        BLOCKS.add(definition);
        return definition;
    }
}
