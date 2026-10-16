package com.example.cleanmenu;

/** Ein Schieberegler von min bis max. */
final class SliderItem extends Item {
    final float min;
    final float max;
    final float step;
    float value;
    float shown; // animierter Wert 0..1

    // Position der Leiste im letzten Frame
    int tx, tw;

    SliderItem(String id, String name, String desc, Category category,
               float min, float max, float step, float def) {
        super(id, name, desc, category);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = def;
        this.shown = norm();
    }

    float norm() {
        return (value - min) / (max - min);
    }

    @Override
    int height() {
        return 54;
    }
}
