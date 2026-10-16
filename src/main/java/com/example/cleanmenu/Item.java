package com.example.cleanmenu;

/** Ein Eintrag in der Liste (Schalter oder Regler). */
abstract class Item {
    final String id;
    final String name;
    final String desc;
    final Category category;

    // Animationszustand
    float hover;
    float press;

    // Position im letzten Frame (fuer Mausklicks)
    int rx, ry, rw, rh;

    Item(String id, String name, String desc, Category category) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.category = category;
    }

    abstract int height();
}
