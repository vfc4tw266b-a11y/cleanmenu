package com.example.cleanmenu;

/** Die Kategorien der Sidebar. */
enum Category {
    COMBAT("Combat", 0xFFFF6B7A),
    VISUALS("Visuals", 0xFF9B7BFF),
    UTILITY("Utility", 0xFF4FD1A5),
    SETTINGS("Settings", 0xFFFFB454);

    final String label;
    final int color;

    Category(String label, int color) {
        this.label = label;
        this.color = color;
    }
}
