package dev.chain.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    MISC("Misc");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
