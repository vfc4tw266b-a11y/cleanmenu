package com.example.cleanmenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Beispiel, wie eine Funktion das Menue-Schalter-Feld nutzt: Koordinaten oben links. */
final class CoordsHud {

    private CoordsHud() {}

    static void render(GuiGraphicsExtractor g) {
        if (!Modules.COORDS.value) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        String s = "XYZ  " + mc.player.getBlockX() + "  " + mc.player.getBlockY() + "  " + mc.player.getBlockZ();
        int w = mc.font.width(s);
        Ui.rrect(g, 6, 6, w + 14, 18, 6, 0x99000000);
        g.text(mc.font, s, 13, 11, 0xFFFFFFFF, false);
    }
}
