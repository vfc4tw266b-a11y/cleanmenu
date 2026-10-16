package com.example.cleanmenu;

/** Ein Ein/Aus-Schalter. {@code value} ist der Zustand, {@code anim} die weiche Animation 0..1. */
final class ToggleItem extends Item {
    boolean value;
    float anim;

    ToggleItem(String id, String name, String desc, Category category, boolean def) {
        super(id, name, desc, category);
        this.value = def;
        this.anim = def ? 1f : 0f;
    }

    @Override
    int height() {
        return 46;
    }
}
