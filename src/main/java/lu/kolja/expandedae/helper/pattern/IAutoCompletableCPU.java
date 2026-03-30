package lu.kolja.expandedae.helper.pattern;

import appeng.api.crafting.IPatternDetails;

/**
 * 可被自动完成的CPU接口
 * 用于统一处理不同类型的Crafting CPU的自动完成逻辑
 */
public interface IAutoCompletableCPU {
    /**
     * 检查指定的合成任务是否应该被自动完成
     *
     * @param details 合成配方详情
     * @return 如果应该自动完成返回true
     */
    boolean expandedae$shouldAutoComplete(IPatternDetails details);
}
