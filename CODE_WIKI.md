# Expanded AE - Code Wiki

## 项目概述

**Expanded AE** 是一个 Minecraft NeoForge 模组（1.21.1），作为 Applied Energistics 2 (AE2) 的扩展模组，添加了多种增强功能和生活质量改进。该模组主要移植了 GT New Horizons AE2、AE2-UEL 和 Neeve's AE2 等旧版本 AE2 的附加功能。

### 基本信息

| 属性 | 值 |
|------|-----|
| 模组ID | `expandedae` |
| 版本 | 2.1.1 |
| Minecraft版本 | 1.21.1 |
| NeoForge版本 | 21.1.170 |
| Java版本 | 21 |
| 许可证 | GNU LGPL v3.0 |

---

## 目录

1. [项目架构](#项目架构)
2. [核心模块](#核心模块)
3. [定义层 (Definitions)](#定义层-definitions)
4. [方块与实体](#方块与实体)
5. [物品系统](#物品系统)
6. [终端与菜单](#终端与菜单)
7. [Mixin 系统](#mixin-系统)
8. [跨模组兼容](#跨模组兼容)
9. [客户端渲染](#客户端渲染)
10. [配置系统](#配置系统)
11. [依赖关系](#依赖关系)
12. [构建与运行](#构建与运行)

---

## 项目架构

```
lu.kolja.expandedae/
├── Expandedae.java                 # 主模组入口类
├── ExpConfig.java                  # 配置系统
├── block/                          # 方块相关
│   ├── block/                      # 方块定义
│   ├── entity/                     # 方块实体
│   └── ExpBlockBaseScreen.java     # 方块基础屏幕类
├── client/                         # 客户端代码
│   ├── button/                     # 自定义按钮
│   ├── gui/                        # GUI组件
│   ├── render/                     # 渲染相关
│   └── ExpandedaeClient.java       # 客户端入口
├── datagen/                        # 数据生成
├── definition/                     # 注册定义
│   ├── ExpBlocks.java              # 方块注册
│   ├── ExpItems.java               # 物品注册
│   ├── ExpBlockEntities.java       # 方块实体注册
│   ├── ExpMenus.java               # 菜单注册
│   ├── ExpCreativeTab.java         # 创造标签页
│   ├── ExpUpgrades.java            # 升级卡注册
│   └── ...
├── enums/                          # 枚举定义
│   ├── ADDONS.java                 # 支持的附加模组
│   ├── ExpTiers.java               # CPU等级
│   └── BlockingMode.java           # 阻塞模式
├── helper/                         # 辅助类
│   ├── misc/                       # 杂项辅助
│   └── pattern/                    # 模式提供器辅助
├── item/                           # 物品
│   ├── abstracts/                  # 抽象物品类
│   ├── dummy/                      # 虚拟物品
│   ├── misc/                       # 杂项物品
│   └── part/                       # 部件物品
├── menu/                           # 菜单
├── mixin/                          # Mixin注入
│   ├── accessor/                   # 访问器
│   ├── compat/                     # 兼容性Mixin
│   ├── cpu/                        # CPU相关
│   ├── patternprovider/            # 模式提供器
│   └── terminal/                   # 终端
├── part/                           # 部件
├── screen/                         # 屏幕
├── terminal/                       # 终端实现
│   └── wtlib/                      # 无线终端库集成
├── util/                           # 工具类
└── xmod/                           # 跨模组集成
    ├── advancedae/                 # Advanced AE
    ├── ae2wtlib/                   # AE2无线终端库
    ├── appflux/                    # AppFlux
    ├── emi/                        # EMI集成
    ├── extendedae/                 # Extended AE
    └── megacells/                  # Mega Cells
```

---

## 核心模块

### 1. 主入口类 [Expandedae.java](src/main/java/lu/kolja/expandedae/Expandedae.java)

模组的主入口类，负责初始化所有注册系统和能力系统。

**关键方法：**

| 方法 | 说明 |
|------|------|
| `Expandedae(IEventBus, ModContainer)` | 构造函数，注册所有DeferredRegister |
| `makeId(String)` | 创建模组资源ID的静态工具方法 |
| `commonSetup(FMLCommonSetupEvent)` | 通用设置，初始化跨模组兼容 |
| `initCapabilities(RegisterCapabilitiesEvent)` | 注册方块实体能力 |
| `initPartCapabilities(RegisterPartCapabilitiesEvent)` | 注册部件能力 |

**注册系统：**
```java
ExpItems.DR.register(modEventBus);        // 物品注册
ExpBlocks.DR.register(modEventBus);       // 方块注册
ExpBlockEntities.DR.register(modEventBus); // 方块实体注册
ExpMenus.DR.register(modEventBus);        // 菜单注册
ExpCreativeTab.DR.register(modEventBus);  // 创造标签页
ExpCodecs.CONDITIONAL_CODECS.register(modEventBus); // 条件编解码器
```

---

## 定义层 (Definitions)

### 2. 方块定义 [ExpBlocks.java](src/main/java/lu/kolja/expandedae/definition/ExpBlocks.java)

负责注册所有自定义方块。

**主要方块：**

| 方块ID | 类 | 说明 |
|--------|-----|------|
| `exp_pattern_provider` | ExpPatternProviderBlock | 扩展模式提供器（72格） |
| `exp_io_port` | ExpIOPortBlock | 扩展IO端口 |
| `exp_crafting_unit` | CraftingUnitBlock | 扩展合成单元 |
| `exp_crafting_accelerator_*` | CraftingUnitBlock | 各种等级的CPU加速器 |
| `miku` / `teto` | PlushieBlock | 装饰性玩偶方块 |

**CPU等级系统：**
- 基础等级：2x, 4x, 8x, 16x, 32x, 64x, 128x, 256x, 512x
- 高级等级：1K, 2K, 4K, 8K, 16K, 32K, 64K, 128K, 256K, 512K, 1M

### 3. 物品定义 [ExpItems.java](src/main/java/lu/kolja/expandedae/definition/ExpItems.java)

注册所有自定义物品。

**主要物品：**

| 物品ID | 类 | 说明 |
|--------|-----|------|
| `exp_pattern_provider_part` | PartItem | 扩展模式提供器部件 |
| `exp_encoding_terminal` | PartItem | 扩展模式编码终端 |
| `exp_pattern_provider_upgrade` | ExpPatternProviderUpgradeItem | 升级物品 |
| `auto_complete_card` | UpgradeCardItem | 自动完成卡 |
| `pattern_refiller_card` | UpgradeCardItem | 模式补充卡 |
| `greater_accel_card` | UpgradeCardItem | 高级加速卡 |
| `wireless_exp_encoding_terminal` | ItemWT | 无线扩展编码终端 |

### 4. 方块实体定义 [ExpBlockEntities.java](src/main/java/lu/kolja/expandedae/definition/ExpBlockEntities.java)

注册方块实体类型。

| 实体ID | 类 | 关联方块 |
|--------|-----|----------|
| `exp_pattern_provider` | ExpPatternProviderBlockEntity | EXP_PATTERN_PROVIDER |
| `exp_io_port` | ExpIOPortBlockEntity | EXP_IO_PORT |
| `exp_cpus` | CraftingBlockEntity | 所有CPU方块 |

### 5. 菜单定义 [ExpMenus.java](src/main/java/lu/kolja/expandedae/definition/ExpMenus.java)

注册容器菜单类型。

| 菜单ID | 类 | 宿主类型 |
|--------|-----|----------|
| `exp_pattern_provider` | ExpPatternProviderMenu | PatternProviderLogicHost |
| `exp_encoding_terminal` | ExpEncodingTerminalMenu | IPatternTerminalMenuHost |
| `exp_io_port` | IOPortMenu | ExpIOPortBlockEntity |

---

## 方块与实体

### 6. 扩展模式提供器方块 [ExpPatternProviderBlock.java](src/main/java/lu/kolja/expandedae/block/block/ExpPatternProviderBlock.java)

扩展的AE2模式提供器，提供72个模式槽位（原版只有9个）。

**特性：**
- 72个模式槽位
- 支持方向推送设置
- 支持扳手旋转

**关键方法：**
```java
public static PatternProviderLogic createLogic(IManagedGridNode mainNode, PatternProviderLogicHost host)
    // 创建支持72格的逻辑实例
```

### 7. 扩展模式提供器实体 [ExpPatternProviderBlockEntity.java](src/main/java/lu/kolja/expandedae/block/entity/ExpPatternProviderBlockEntity.java)

**继承关系：**
```
PatternProviderBlockEntity -> AEBaseBlockEntity
        ↑
ExpPatternProviderBlockEntity
```

### 8. 扩展IO端口实体 [ExpIOPortBlockEntity.java](src/main/java/lu/kolja/expandedae/block/entity/ExpIOPortBlockEntity.java)

增强的IO端口，支持更多升级和更快的传输速度。

**特性：**
- 6个存储单元槽位
- 支持速度卡和高级加速卡
- 指数级速度提升

**速度计算：**
```java
itemsToMove *= speed > 0 ? (long) Math.pow(2, (speed * 2) - 1) : 1;
itemsToMove *= greater > 0 ? (long) Math.pow(2, (greater * 3) - 1) : 1;
```

---

## 物品系统

### 9. 升级物品 [ExpPatternProviderUpgradeItem.java](src/main/java/lu/kolja/expandedae/item/misc/ExpPatternProviderUpgradeItem.java)

允许将普通模式提供器升级为扩展模式提供器，保留原有内容。

**支持的升级：**
- 方块形式：PatternProviderBlock → ExpPatternProviderBlock
- 部件形式：PatternProviderPart → ExpPatternProviderPart

### 10. CPU物品 [ExpCPUItem.java](src/main/java/lu/kolja/expandedae/item/misc/ExpCPUItem.java)

用于各种等级的合成协处理单元。

---

## 终端与菜单

### 11. 扩展编码终端菜单 [ExpEncodingTerminalMenu.java](src/main/java/lu/kolja/expandedae/terminal/ExpEncodingTerminalMenu.java)

增强的模式编码终端，支持模式修改和自动补充功能。

**特性：**
- 模式乘除修改（支持批量修改）
- 模式补充卡自动补充空白模式
- 编码后自动将模式添加到玩家背包

**关键方法：**
```java
public void modifyPattern(Integer data)    // 修改模式数量（正数乘，负数除）
public void movePattern(Boolean data)      // 移动模式到背包
public void encode()                       // 编码并触发补充逻辑
```

### 12. 扩展模式提供器菜单 [ExpPatternProviderMenu.java](src/main/java/lu/kolja/expandedae/menu/ExpPatternProviderMenu.java)

简单的菜单类，继承自AE2的PatternProviderMenu。

---

## Mixin 系统

### 13. 模式提供器逻辑 Mixin [MixinPatternProviderLogic.java](src/main/java/lu/kolja/expandedae/mixin/patternprovider/MixinPatternProviderLogic.java)

核心Mixin，扩展模式提供器的功能。

**添加的功能：**
- 升级槽位系统（1个槽位）
- 阻塞模式设置
- 自动完成卡功能
- 智能阻塞模式
- 目标缓存优化

**升级卡效果：**
- `AUTO_COMPLETE_CARD`：当CPU库存为空时自动取消任务
- 支持多种阻塞模式

### 14. 其他重要 Mixin

| Mixin 类 | 目标类 | 功能 |
|----------|--------|------|
| `MixinPatternProviderScreen` | PatternProviderScreen | UI修改，添加升级槽位 |
| `MixinPatternProviderMenu` | PatternProviderMenu | 菜单功能扩展 |
| `MixinCraftingCPUCluster` | CraftingCPUCluster | CPU集群修改 |
| `MixinCPUSelectionList` | CPUSelectionList | CPU选择列表增强 |
| `MixinCraftConfirmScreen` | CraftConfirmScreen | 合成确认屏幕修改 |
| `MixinPatternEncodingTermScreen` | PatternEncodingTermScreen | 编码终端屏幕增强 |
| `MixinProcessingEncodingPanel` | ProcessingEncodingPanel | 处理编码面板修改 |
| `MixinControllerValidator` | ControllerValidator | 控制器验证修改 |

### 15. 访问器 (Accessors)

| 访问器 | 目标 | 用途 |
|--------|------|------|
| `AccessorIOPortBlockEntity` | IOPortBlockEntity | 访问私有字段和方法 |
| `AccessorPatternEncodingTermMenu` | PatternEncodingTermMenu | 访问编码终端菜单 |
| `AccessorCraftingCpuLogic` | CraftingCpuLogic | 访问CPU逻辑 |
| `AccessorBuiltInModelHooks` | BuiltInModelHooks | 访问模型钩子 |

---

## 跨模组兼容

### 16. 兼容模组枚举 [ADDONS.java](src/main/java/lu/kolja/expandedae/enums/ADDONS.java)

```java
public enum ADDONS {
    EXT("extendedae"),      // Extended AE
    MEGA("megacells"),      // Mega Cells
    APPFLUX("appflux"),     // AppFlux
    ADV("advanced_ae"),     // Advanced AE
    APPMEK("appmek"),       // Applied Mekanistics
    ARSENG("arseng"),       // Ars Energistique
    APPEX("appex");         // Applied Experienced
}
```

### 17. XMod 管理器 [XMod.java](src/main/java/lu/kolja/expandedae/xmod/XMod.java)

自动检测并初始化已加载的兼容模组。

### 18. 具体集成

| 模组 | 集成类 | 功能 |
|------|--------|------|
| Advanced AE | AdvancedAE.java | 高级CPU支持 |
| AE2WTLib | WTLibIntegration.java | 无线终端支持 |
| AppFlux | AppFlux.java | 能量单元支持 |
| Extended AE | ExtendedAE.java | 扩展接口支持 |
| Mega Cells | MegaCells.java | 大型存储单元 |
| EMI | ExpandedaeEmiPlugin.java | EMI配方浏览器 |

---

## 客户端渲染

### 19. 客户端入口 [ExpandedaeClient.java](src/main/java/lu/kolja/expandedae/client/ExpandedaeClient.java)

客户端专用初始化类。

**功能：**
- 注册合成单元模型
- 注册菜单屏幕
- 初始化渲染系统

### 20. 合成单元模型提供器 [ExpCraftingUnitModelProvider.java](src/main/java/lu/kolja/expandedae/client/render/ExpCraftingUnitModelProvider.java)

为不同等级的CPU提供自定义模型。

---

## 配置系统

### 21. 配置类 [ExpConfig.java](src/main/java/lu/kolja/expandedae/ExpConfig.java)

模组配置文件定义。

**配置项：**

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `maxControllerSize` | int | 7 | 控制器最大尺寸 |
| `ignoreControllerRules` | boolean | false | 忽略控制器形成规则 |

---

## 依赖关系

### 22. 必需依赖

| 模组 | 版本 | 用途 |
|------|------|------|
| Applied Energistics 2 | 19.2.10 | 核心API和功能 |
| NeoForge | 21.1.170 | 模组加载器 |

### 23. 可选依赖

| 模组 | 用途 |
|------|------|
| ExtendedAE | 扩展模式提供器支持 |
| Mega Cells | 大型存储单元支持 |
| Advanced AE | 高级合成CPU |
| AppFlux | 能量单元升级槽 |
| AE2WTLib | 无线终端 |
| EMI | 配方浏览器集成 |
| JEI | 配方浏览器集成 |

### 24. 开发依赖

| 模组 | 用途 |
|------|------|
| GuideME | 游戏内指南 |
| Glodium | 库依赖 |

---

## 构建与运行

### 25. Gradle 任务

```bash
# 构建项目
./gradlew build

# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer

# 运行数据生成
./gradlew runData

# 运行游戏测试
./gradlew runGameTestServer

# 生成指南
./gradlew guide
```

### 26. 开发环境设置

**要求：**
- Java 21+
- Gradle 8.x

**IDE导入：**
```bash
# Eclipse
./gradlew eclipse

# IntelliJ IDEA
./gradlew idea
```

### 27. 项目属性

| 属性 | 值 |
|------|-----|
| Group ID | `lu.kolja` |
| Artifact ID | `expandedae` |
| 版本 | `2.1.1` |
| Java版本 | `21` |

---

## 关键类与函数速查

### 28. 工具类

| 类 | 功能 |
|-----|------|
| `NumberUtil` | 数字格式化（支持科学计数法） |
| `Maths` | 数学计算工具 |
| `KeybindUtil` | 按键绑定工具 |
| `ExpReflection` | 反射工具 |
| `GuiUtil` | GUI工具 |

### 29. 接口定义

| 接口 | 功能 |
|------|------|
| `IPatternProvider` | 模式提供器接口 |
| `IPatternProviderLogic` | 模式提供器逻辑接口 |
| `IUpgradableMenu` | 可升级菜单接口 |
| `ISmartBlocking` | 智能阻塞接口 |

### 30. 枚举定义

| 枚举 | 功能 |
|------|------|
| `ExpTiers` | CPU等级定义（2x-1M） |
| `BlockingMode` | 阻塞模式 |
| `ADDONS` | 支持的附加模组 |

---

## 数据生成

### 31. 数据生成器 [ExpDataGen.java](src/main/java/lu/kolja/expandedae/datagen/ExpDataGen.java)

自动生成模组资源文件。

**生成内容：**
- 语言文件（en_us）
- 模型文件
- 配方文件
- 战利品表

---

## 文件结构

```
src/
├── main/
│   ├── java/lu/kolja/expandedae/     # 主代码
│   └── resources/
│       ├── META-INF/
│       │   ├── neoforge.mods.toml    # 模组元数据
│       │   └── accesstransformer.cfg # 访问转换器
│       └── assets/
│           ├── ae2/                  # AE2资源覆盖
│           └── expandedae/           # 模组资源
│               ├── ae2guide/         # 游戏内指南
│               ├── blockstates/      # 方块状态
│               ├── lang/             # 语言文件
│               ├── models/           # 模型
│               └── textures/         # 纹理
└── generated/resources/              # 生成资源
```

---

## 贡献指南

### 代码规范
- 使用Lombok简化代码
- 遵循AE2的编码风格
- 所有新功能需要Mixin时优先使用注入而非覆盖

### 添加新方块/物品流程
1. 在对应的definition类中注册
2. 创建实现类
3. 添加数据生成（如需要）
4. 添加语言条目
5. 更新此Wiki文档

---

*文档版本: 1.0*
*最后更新: 2026-05-20*
