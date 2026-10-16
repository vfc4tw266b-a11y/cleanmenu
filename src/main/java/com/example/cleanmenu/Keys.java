package com.example.cleanmenu;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;

import java.lang.reflect.Method;

/** Prueft, ob ein Tastendruck zur (frei belegbaren) Menue-Taste gehoert. */
final class Keys {

    private static boolean resolved = false;
    private static Method matchesMethod;

    private Keys() {}

    static boolean matches(KeyMapping mapping, KeyEvent event, int fallbackKey) {
        try {
            if (!resolved) {
                resolved = true;
                try {
                    matchesMethod = KeyMapping.class.getMethod("matches", KeyEvent.class);
                } catch (Throwable t) {
                    matchesMethod = null;
                }
            }
            if (matchesMethod != null) {
                return (Boolean) matchesMethod.invoke(mapping, event);
            }
        } catch (Throwable ignored) {
        }
        return event.key() == fallbackKey;
    }
}
