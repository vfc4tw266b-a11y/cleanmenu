package com.example.cleanmenu;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class CleanMenuClient implements ClientModInitializer {

    /** Standard: rechte Umschalt-Taste. Aenderbar unter Optionen > Steuerung > Tastenbelegung. */
    static final int DEFAULT_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
    static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        Modules.load();

        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("cleanmenu", "main"));

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.cleanmenu.open",
                InputConstants.Type.KEYSYM,
                DEFAULT_KEY,
                category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreenAndShow(new MenuScreen());
                }
            }
        });

        // Beispiel fuer eine echte Funktion: Koordinaten-HUD
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath("cleanmenu", "coords_hud"),
                (graphics, deltaTracker) -> CoordsHud.render(graphics));
    }
}
