package com.example.cleanmenu;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Hier stehen alle Funktionen des Menues. Neue Funktion = eine Zeile hinzufuegen.
 * Im eigenen Code kannst du dann z. B. {@code Modules.COORDS.value} abfragen.
 */
final class Modules {

    static final List<Item> ALL = new ArrayList<>();

    // ---- Combat (Platzhalter)
    static final ToggleItem TOTEM = add(new ToggleItem("totem_highlight", "Totem Highlight",
            "Markiert Totems nach einem Pop", Category.COMBAT, true));
    static final ToggleItem HIT_COUNTER = add(new ToggleItem("hit_counter", "Hit Counter",
            "Zaehlt deine Treffer im Kampf", Category.COMBAT, false));
    static final ToggleItem CPS = add(new ToggleItem("cps_display", "CPS Display",
            "Zeigt deine Klicks pro Sekunde", Category.COMBAT, false));
    static final ToggleItem REACH = add(new ToggleItem("reach_display", "Reach Display",
            "Zeigt die Distanz deines letzten Treffers", Category.COMBAT, false));
    static final ToggleItem COMBO = add(new ToggleItem("combo_counter", "Combo Counter",
            "Zeigt deine aktuelle Trefferserie", Category.COMBAT, false));
    static final ToggleItem ARMOR = add(new ToggleItem("armor_hud", "Armor HUD",
            "Haltbarkeit deiner Ruestung im HUD", Category.COMBAT, false));
    static final ToggleItem POTIONS = add(new ToggleItem("potion_timers", "Potion Timers",
            "Restzeit aktiver Effekte", Category.COMBAT, false));

    // ---- Visuals
    /** Diese Funktion ist schon echt: zeigt deine Koordinaten oben links (siehe CoordsHud). */
    static final ToggleItem COORDS = add(new ToggleItem("coords_hud", "Coordinates HUD",
            "Zeigt deine Koordinaten oben links", Category.VISUALS, false));
    static final ToggleItem FPS = add(new ToggleItem("fps_display", "FPS Display",
            "Zeigt deine Bilder pro Sekunde", Category.VISUALS, false));
    static final ToggleItem ZOOM = add(new ToggleItem("zoom", "Zoom",
            "Per Taste ran- und rauszoomen", Category.VISUALS, false));
    static final ToggleItem LOW_FIRE = add(new ToggleItem("low_fire", "Low Fire",
            "Niedrigeres Feuer-Overlay", Category.VISUALS, false));
    static final ToggleItem CROSSHAIR = add(new ToggleItem("custom_crosshair", "Custom Crosshair",
            "Eigenes Fadenkreuz", Category.VISUALS, false));
    static final ToggleItem HURT_CAM = add(new ToggleItem("hurt_cam", "Hurt Cam Reduction",
            "Weniger Kameraruck bei Schaden", Category.VISUALS, false));

    // ---- Utility
    static final ToggleItem TOGGLE_SPRINT = add(new ToggleItem("toggle_sprint", "Toggle Sprint",
            "Sprinten ohne Taste halten", Category.UTILITY, false));
    static final ToggleItem CHAT_HIGHLIGHT = add(new ToggleItem("chat_highlight", "Chat Highlight",
            "Hebt Nachrichten mit deinem Namen hervor", Category.UTILITY, false));
    static final ToggleItem DEATH_MARKER = add(new ToggleItem("death_marker", "Death Marker",
            "Merkt dir den Ort deines letzten Todes", Category.UTILITY, false));
    static final ToggleItem WAYPOINTS = add(new ToggleItem("waypoints", "Waypoints",
            "Orte speichern und wiederfinden", Category.UTILITY, false));
    static final ToggleItem SHOT_HELPER = add(new ToggleItem("screenshot_helper", "Screenshot Helper",
            "Praktische Screenshot-Funktionen", Category.UTILITY, false));

    // ---- Settings (steuern das Menue selbst)
    static final ToggleItem ANIMATIONS = add(new ToggleItem("anim_enabled", "Animationen",
            "Weiche Uebergaenge im Menue", Category.SETTINGS, true));
    static final SliderItem SPEED = add(new SliderItem("anim_speed", "Animationsgeschwindigkeit",
            "Wie schnell das Menue animiert", Category.SETTINGS, 0.5f, 2.0f, 0.05f, 1.0f));
    static final ToggleItem DIM = add(new ToggleItem("bg_dim", "Hintergrund abdunkeln",
            "Dunkelt das Spiel hinter dem Menue ab", Category.SETTINGS, true));

    private Modules() {}

    private static <T extends Item> T add(T item) {
        ALL.add(item);
        return item;
    }

    static List<Item> of(Category category) {
        List<Item> out = new ArrayList<>();
        for (Item i : ALL) {
            if (i.category == category) {
                out.add(i);
            }
        }
        return out;
    }

    // ------------------------------------------------------------ Speichern / Laden

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("cleanmenu.properties");
    }

    static void load() {
        Path f = file();
        if (!Files.isRegularFile(f)) {
            return;
        }
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f)) {
            p.load(r);
        } catch (IOException e) {
            return;
        }
        for (Item i : ALL) {
            String v = p.getProperty(i.id);
            if (v == null) {
                continue;
            }
            try {
                if (i instanceof ToggleItem t) {
                    t.value = Boolean.parseBoolean(v);
                    t.anim = t.value ? 1f : 0f;
                } else if (i instanceof SliderItem s) {
                    s.value = Math.max(s.min, Math.min(s.max, Float.parseFloat(v)));
                    s.shown = s.norm();
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    static void save() {
        Properties p = new Properties();
        for (Item i : ALL) {
            if (i instanceof ToggleItem t) {
                p.setProperty(i.id, Boolean.toString(t.value));
            } else if (i instanceof SliderItem s) {
                p.setProperty(i.id, String.format(Locale.ROOT, "%.2f", s.value));
            }
        }
        try (Writer w = Files.newBufferedWriter(file())) {
            p.store(w, "Clean Menu");
        } catch (IOException ignored) {
        }
    }
}
