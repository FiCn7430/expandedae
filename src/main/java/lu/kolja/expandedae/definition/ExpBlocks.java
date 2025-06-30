package lu.kolja.expandedae.definition;

import appeng.block.AEBaseBlockItem;
import appeng.block.crafting.CraftingUnitBlock;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import lu.kolja.expandedae.Expandedae;
import lu.kolja.expandedae.block.block.ExpDriveBlock;
import lu.kolja.expandedae.block.block.ExpPatternProviderBlock;
import lu.kolja.expandedae.enums.ExpTiers;
import lu.kolja.expandedae.item.misc.ExpCPUItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ExpBlocks {
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(Expandedae.MODID);

    public static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();

    public static final BlockDefinition<ExpPatternProviderBlock> EXP_PATTERN_PROVIDER = block(
            "ME Expanded Pattern Provider",
            "exp_pattern_provider",
            ExpPatternProviderBlock::new,
            AEBaseBlockItem::new
    );

    public static final BlockDefinition<ExpDriveBlock> EXP_DRIVE = block(
            "ME Expanded Drive",
            "exp_drive",
            ExpDriveBlock::new,
            AEBaseBlockItem::new
    );

    public static BlockDefinition<CraftingUnitBlock> UNIT = block(
            "Expanded Crafting Unit",
            "exp_crafting_unit",
            () -> new CraftingUnitBlock(ExpTiers.UNIT),
            AEBaseBlockItem::new
    );

    public static BlockDefinition<CraftingUnitBlock> CPU_2 = cpu(
            "2x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_2",
            ExpTiers.TIER_2
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_4 = cpu(
            "4x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_4",
            ExpTiers.TIER_4
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_8 = cpu(
            "8x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_8",
            ExpTiers.TIER_8
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_16 = cpu(
            "16x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_16",
            ExpTiers.TIER_16
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_32 = cpu(
            "32x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_32",
            ExpTiers.TIER_32
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_64 = cpu(
            "64x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_64",
            ExpTiers.TIER_64
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_128 = cpu(
            "128x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_128",
            ExpTiers.TIER_128
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_256 = cpu(
            "256x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_256",
            ExpTiers.TIER_256
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_512 = cpu(
            "512x Crafting Co-Processing Unit",
            "exp_crafting_accelerator_512",
            ExpTiers.TIER_512
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_1K = cpu(
            "1K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_1k",
            ExpTiers.TIER_1K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_2K = cpu(
            "2K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_2k",
            ExpTiers.TIER_2K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_4K = cpu(
            "4K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_4k",
            ExpTiers.TIER_4K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_8K = cpu(
            "8K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_8k",
            ExpTiers.TIER_8K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_16K = cpu(
            "16K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_16k",
            ExpTiers.TIER_16K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_32K = cpu(
            "32K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_32k",
            ExpTiers.TIER_32K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_64K = cpu(
            "64K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_64k",
            ExpTiers.TIER_64K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_128K = cpu(
            "128K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_128k",
            ExpTiers.TIER_128K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_256K = cpu(
            "256K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_256k",
            ExpTiers.TIER_256K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_512K = cpu(
            "512K Crafting Co-Processing Unit",
            "exp_crafting_accelerator_512k",
            ExpTiers.TIER_512K
    );
    public static BlockDefinition<CraftingUnitBlock> CPU_1M = cpu(
            "1M Crafting Co-Processing Unit",
            "exp_crafting_accelerator_1m",
            ExpTiers.TIER_1M
    );

    public static BlockDefinition<CraftingUnitBlock> cpu(
            String englishName, String id, ExpTiers cpu
    ) {
        return block(
                englishName, id,
                () -> new CraftingUnitBlock(cpu),
                (a, b) -> new ExpCPUItem(a, b, AEItems.ADVANCED_CARD) //TODO: FIX ITEM SYSTEM
        );
    }

    public static List<BlockDefinition<?>> getBlocks() {
        return Collections.unmodifiableList(BLOCKS);
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
}
