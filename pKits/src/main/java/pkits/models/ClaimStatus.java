package pkits.models;

public enum ClaimStatus {

    AVAILABLE("available"),
    NO_PERMISSION("no-permission"),
    ON_COOLDOWN("on-cooldown"),
    ALREADY_CLAIMED("already-claimed");

    private final String key;

    ClaimStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
