package pkits.models;

public enum InputType {

    NAME("name"),
    LORE_ADD("lore"),
    COOLDOWN("cooldown"),
    PERMISSION("permission"),
    ICON("icon"),
    SHULKER_NAME("shulker-name");

    private final String key;

    InputType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
