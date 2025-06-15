package lu.kolja.expandedae.helper.pattern;

import lu.kolja.expandedae.enums.BlockingMode;

public interface ISmartBlocking {
    /**
     * Resets the blocking mode to its default state, in this case, default mod
     */
    void expandedae$resetBlocking();

    /**
     * Sets the blocking mode of the pattern provider
     * @param blockingMode mode to set to
     */
    void expandedae$setBlocking(BlockingMode blockingMode);

    /**
     * @return The current blocking mode
     */
    BlockingMode expandedae$getBlockingMode();

    /**
     * Hides the extra blocking modes button
     */
    void expandedae$hideBlocking();

    /**
     * Shows the extra blocking modes button
     */
    void expandedae$showBlocking();
}
