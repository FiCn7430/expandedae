package lu.kolja.expandedae.enums;

public enum BlockingMode {
    ALL("All"),
    SMART("Smart"),
    DEFAULT("Default");

    private final String name;

    BlockingMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}