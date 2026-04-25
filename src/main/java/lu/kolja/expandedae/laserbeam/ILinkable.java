package lu.kolja.expandedae.laserbeam;

import net.minecraft.core.BlockPos;

import java.util.Set;

/**
 * 可以被激光绑定器连接的方块实体接口
 * 
 * 实现此接口的方块实体可以被激光绑定工具连接，
 * 用于建立两个激光线缆之间的AE网络连接
 */
public interface ILinkable {
    
    /**
     * 添加连接目标
     * 
     * @param target 目标方块位置
     */
    void addLink(BlockPos target);
    
    /**
     * 移除连接目标
     * 
     * @param target 目标方块位置
     */
    void removeLink(BlockPos target);
    
    /**
     * 获取所有连接目标
     * 
     * @return 不可修改的连接目标位置集合
     */
    Set<BlockPos> getLinks();
}
