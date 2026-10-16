package com.example.cleanmenu;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

public class MenuScreen extends Screen {

    // ---------------------------------------------------------------- Farben (Dark Mode, "Glas")
    private static final int WINDOW_BG = 0xD0101218;   // leicht durchsichtig
    private static final int SIDEBAR_BG = 0x66000000;
    private static final int EDGE = 0x30FFFFFF;
    private static final int CARD_BG = 0x12FFFFFF;
    private static final int CARD_HOVER = 0x26FFFFFF;
    private static final int TEXT = 0xFFEDEFF4;
    private static final int SUB = 0xFF8E96A8;
    private static final int ACCENT = 0xFF6D83FF;
    private static final int ACCENT_HI = 0xFF8FA0FF;
    private static final int TRACK_OFF = 0xFF2B303B;
    private static final int WHITE = 0xFFFFFFFF;

    // ---------------------------------------------------------------- Masse
    private static final int HEADER_H = 54;
    private static final int CAT_TOP = 58;
    private static final int CAT_H = 28;
    private static final int CAT_STEP = 32;
    private static final int CARD_GAP = 8;
    private static final Category[] CATS = Category.values();

    // ---------------------------------------------------------------- Zustand
    private Category current = Category.COMBAT;
    private float openT = 0f;
    private boolean closing = false;
    private float catT = 1f;
    private float selY = Float.NaN;
    private final float[] catHover = new float[CATS.length];

    private float scrollPos = 0f;
    private float scrollVel = 0f;
    private float scrollbarA = 0f;

    private SliderItem dragging = null;
    private long lastNs = System.nanoTime();
    private final long openedNs = System.nanoTime();
    private float fade = 1f;

    // ---------------------------------------------------------------- Layout (pro Frame)
    private int wx, wy, ww, wh, sbW, cx, cw, listY, listH, cardW;
    private float maxScroll;

    public MenuScreen() {
        super(Component.literal("Clean Menu"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        closing = true; // echtes Schliessen nach der Ausblend-Animation (siehe tick)
    }

    @Override
    public void tick() {
        if (closing && openT <= 0f) {
            super.onClose();
        }
    }

    @Override
    public void removed() {
        Modules.save();
        super.removed();
    }

    private static float speed() {
        return Modules.ANIMATIONS.value ? Modules.SPEED.value : 60f;
    }

    // ================================================================ Layout + Update

    private void layout() {
        ww = Math.min(this.width - 40, 560);
        wh = Math.min(this.height - 40, 340);
        wx = (this.width - ww) / 2;
        wy = (this.height - wh) / 2;
        sbW = Math.max(100, Math.min(124, ww / 4));
        cx = wx + sbW + 16;
        cw = ww - sbW - 32;
        cardW = cw - 8;
        listY = wy + HEADER_H;
        listH = wh - HEADER_H - 12;

        List<Item> items = Modules.of(current);
        float total = 8;
        for (Item it : items) {
            total += it.height() + CARD_GAP;
        }
        total -= CARD_GAP;
        maxScroll = Math.max(0f, total + 4 - listH);
    }

    private void update(float dt) {
        float adt = dt * speed();

        openT = Ui.clamp01(openT + (closing ? -adt / 0.20f : adt / 0.30f));
        catT = Ui.clamp01(catT + adt / 0.40f);

        float targetY = wy + CAT_TOP + current.ordinal() * CAT_STEP;
        selY = Float.isNaN(selY) ? targetY : Ui.approach(selY, targetY, adt, 16f);

        // Scrollen mit Traegheit + sanftem Zurueckfedern an den Raendern
        float over = scrollPos < 0f ? scrollPos : (scrollPos > maxScroll ? scrollPos - maxScroll : 0f);
        if (over != 0f) {
            scrollVel -= over * 140f * dt;
            scrollVel *= (float) Math.exp(-dt * 9f);
        } else {
            scrollVel *= (float) Math.exp(-dt * 6f);
        }
        scrollPos += scrollVel * dt;
        if (over == 0f && Math.abs(scrollVel) < 2f) {
            scrollVel = 0f;
        }
        if (over != 0f && Math.abs(scrollVel) < 2f && Math.abs(over) < 0.5f) {
            scrollPos = Math.max(0f, Math.min(maxScroll, scrollPos));
            scrollVel = 0f;
        }
        scrollbarA = Math.max(0f, scrollbarA - dt * 1.6f);
        if (Math.abs(scrollVel) > 8f) {
            scrollbarA = 1f;
        }

        for (Item it : Modules.ALL) {
            it.press = Ui.approach(it.press, 0f, adt, 14f);
            if (it instanceof ToggleItem t) {
                t.anim = Ui.approach(t.anim, t.value ? 1f : 0f, adt, 13f);
            } else if (it instanceof SliderItem s) {
                s.shown = Ui.approach(s.shown, s.norm(), adt, 20f);
            }
        }
    }

    // ================================================================ Rendering

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);

        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastNs) / 1_000_000_000f);
        lastNs = now;

        layout();
        update(dt);

        float e = Ui.easeOutCubic(openT);
        if (e <= 0.001f) {
            return;
        }
        fade = e;
        float adt = dt * speed();
        boolean interactive = openT > 0.9f && !closing;

        if (Modules.DIM.value) {
            g.fill(0, 0, this.width, this.height, Ui.alpha(0xFF000000, 0.35f * fade));
        }

        // Oeffnen/Schliessen: Skalierung von 95 % auf 100 % um die Fenstermitte
        float s = 0.95f + 0.05f * e;
        float pcx = wx + ww / 2f;
        float pcy = wy + wh / 2f;
        g.pose().pushMatrix();
        g.pose().translate(pcx, pcy);
        g.pose().scale(s, s);
        g.pose().translate(-pcx, -pcy);

        drawWindow(g);
        drawSidebar(g, mouseX, mouseY, adt, interactive);
        drawContent(g, mouseX, mouseY, adt, interactive);

        g.pose().popMatrix();
    }

    // ---------------------------------------------------------------- Text-Helfer

    private void text(GuiGraphicsExtractor g, String s, int x, int y, int color, float f) {
        int c = Ui.alpha(color, f);
        if (((c >>> 24) & 0xFF) < 12) {
            return; // sehr transparenter Text wuerde sonst voll sichtbar gezeichnet
        }
        g.text(this.font, s, x, y, c, false);
    }

    private void text(GuiGraphicsExtractor g, Component comp, int x, int y, int color, float f) {
        int c = Ui.alpha(color, f);
        if (((c >>> 24) & 0xFF) < 12) {
            return;
        }
        g.text(this.font, comp, x, y, c, false);
    }

    private static Component bold(String s) {
        return Component.literal(s).withStyle(ChatFormatting.BOLD);
    }

    // ---------------------------------------------------------------- Fenster

    private void drawWindow(GuiGraphicsExtractor g) {
        // Weicher Schatten nur AUSSERHALB des Fensters (Ringe), damit das Glas durchsichtig bleibt
        for (int i = 1; i <= 9; i++) {
            Ui.rrectOutline(g, wx - i, wy - i + 3, ww + 2 * i, wh + 2 * i, 14 + i,
                    Ui.alpha(0xFF000000, 0.11f * (1f - i / 10f) * fade));
        }
        Ui.rrect(g, wx, wy, ww, wh, 14, Ui.alpha(WINDOW_BG, fade));
        Ui.rrectOutline(g, wx, wy, ww, wh, 14, Ui.alpha(EDGE, fade));
    }

    // ---------------------------------------------------------------- Sidebar

    private void drawSidebar(GuiGraphicsExtractor g, int mx, int my, float adt, boolean interactive) {
        Ui.rrect(g, wx + 8, wy + 8, sbW - 8, wh - 16, 10, Ui.alpha(SIDEBAR_BG, fade));

        // Logo
        Ui.rrect(g, wx + 20, wy + 24, 8, 8, 4, Ui.alpha(ACCENT, fade));
        text(g, bold("Clean Menu"), wx + 34, wy + 24, TEXT, fade);

        // Auswahl-Markierung, gleitet weich zur aktiven Kategorie
        int px = wx + 14;
        int pw = sbW - 20;
        int sy = Math.round(selY);
        Ui.rrect(g, px, sy, pw, CAT_H, 8, Ui.alpha(ACCENT, 0.20f * fade));
        Ui.rrect(g, px, sy + 7, 3, CAT_H - 14, 1, Ui.alpha(ACCENT_HI, fade));

        for (int i = 0; i < CATS.length; i++) {
            Category c = CATS[i];
            int y = wy + CAT_TOP + i * CAT_STEP;
            boolean over = interactive && Ui.inside(mx, my, px, y, pw, CAT_H);
            catHover[i] = Ui.approach(catHover[i], over ? 1f : 0f, adt, 16f);
            float h = catHover[i];
            boolean selected = c == current;

            float scale = 1f + 0.035f * h;
            float ccx = px + pw / 2f;
            float ccy = y + CAT_H / 2f;
            g.pose().pushMatrix();
            g.pose().translate(ccx, ccy);
            g.pose().scale(scale, scale);
            g.pose().translate(-ccx, -ccy);

            if (!selected && h > 0.01f) {
                Ui.rrect(g, px, y, pw, CAT_H, 8, Ui.alpha(WHITE, 0.07f * h * fade));
            }
            Ui.rrect(g, px + 12, y + CAT_H / 2 - 3, 6, 6, 3, Ui.alpha(c.color, fade));
            int labelColor = selected ? TEXT : Ui.mix(SUB, TEXT, h);
            text(g, c.label, px + 26, y + (CAT_H - 8) / 2, labelColor, fade);
            String count = String.valueOf(Modules.of(c).size());
            text(g, count, px + pw - 8 - this.font.width(count), y + (CAT_H - 8) / 2, SUB, fade);

            g.pose().popMatrix();
        }

        text(g, "v1.0.0", wx + 20, wy + wh - 24, SUB, fade);
    }

    // ---------------------------------------------------------------- Inhalt

    private void drawContent(GuiGraphicsExtractor g, int mx, int my, float adt, boolean interactive) {
        // Kopfzeile (blendet beim Kategoriewechsel kurz mit ein)
        float ha = Ui.clamp01(catT * 2.2f) * fade;
        text(g, bold(current.label), cx, wy + 16, TEXT, ha);
        List<Item> items = Modules.of(current);
        text(g, items.size() + " Funktionen", cx, wy + 31, SUB, ha);
        String hint = "Esc zum Schliessen";
        text(g, hint, cx + cw - this.font.width(hint), wy + 20, SUB, ha * 0.8f);
        g.fill(cx, wy + HEADER_H - 6, cx + cw, wy + HEADER_H - 5, Ui.alpha(EDGE, fade));

        // Liste (mit Beschneidung)
        float y = listY + 4 - scrollPos;
        g.enableScissor(cx - 6, listY, cx + cw + 6, listY + listH);
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            int h = it.height();
            float a = Ui.clamp01(catT * 1.7f - i * 0.10f);
            float ea = Ui.easeOutCubic(a);
            int iy = Math.round(y + (1f - ea) * 14f);
            it.rx = cx;
            it.ry = iy;
            it.rw = cardW;
            it.rh = h;

            if (iy + h > listY && iy < listY + listH) {
                boolean over = interactive && ea > 0.9f
                        && Ui.inside(mx, my, cx, iy, cardW, h) && my >= listY && my < listY + listH;
                if (it == dragging) {
                    over = true;
                }
                it.hover = Ui.approach(it.hover, over ? 1f : 0f, adt, 15f);
                float f = fade * ea;
                if (it instanceof ToggleItem t) {
                    drawToggle(g, t, cx, iy, cardW, f);
                } else if (it instanceof SliderItem sl) {
                    drawSlider(g, sl, cx, iy, cardW, f);
                }
            }
            y += h + CARD_GAP;
        }
        g.disableScissor();

        // Scrollleiste
        if (maxScroll > 0.5f) {
            float total = maxScroll + listH;
            int barH = Math.max(22, Math.round(listH * listH / total));
            float t = Math.max(0f, Math.min(1f, scrollPos / maxScroll));
            int barY = listY + Math.round((listH - barH) * t);
            int col = Ui.mix(0x40FFFFFF, ACCENT, scrollbarA);
            Ui.rrect(g, wx + ww - 10, barY, 3, barH, 1, Ui.alpha(col, fade * (0.5f + 0.5f * scrollbarA)));
        }
    }

    private void drawToggle(GuiGraphicsExtractor g, ToggleItem t, int x, int y, int w, float f) {
        int h = t.height();
        float scale = 1f + 0.022f * t.hover - 0.03f * t.press;
        float ccx = x + w / 2f;
        float ccy = y + h / 2f;
        g.pose().pushMatrix();
        g.pose().translate(ccx, ccy);
        g.pose().scale(scale, scale);
        g.pose().translate(-ccx, -ccy);

        Ui.rrect(g, x, y, w, h, 9, Ui.alpha(Ui.mix(CARD_BG, CARD_HOVER, t.hover), f));
        if (t.anim > 0.01f) {
            Ui.rrectOutline(g, x, y, w, h, 9, Ui.alpha(ACCENT, 0.6f * t.anim * f));
        }
        text(g, bold(t.name), x + 14, y + 11, TEXT, f);
        text(g, t.desc, x + 14, y + 25, SUB, f);

        // Animierter Schalter
        int tw = 36;
        int th = 18;
        int tx = x + w - 14 - tw;
        int ty = y + (h - th) / 2;
        Ui.rrect(g, tx, ty, tw, th, 9, Ui.alpha(Ui.mix(TRACK_OFF, ACCENT, t.anim), f));
        int knob = 14;
        int kx = tx + 2 + Math.round(t.anim * (tw - knob - 4));
        Ui.rrect(g, kx, ty + 2, knob, knob, 7, Ui.alpha(WHITE, f));

        g.pose().popMatrix();
    }

    private void drawSlider(GuiGraphicsExtractor g, SliderItem s, int x, int y, int w, float f) {
        int h = s.height();
        float scale = 1f + 0.015f * s.hover;
        float ccx = x + w / 2f;
        float ccy = y + h / 2f;
        g.pose().pushMatrix();
        g.pose().translate(ccx, ccy);
        g.pose().scale(scale, scale);
        g.pose().translate(-ccx, -ccy);

        Ui.rrect(g, x, y, w, h, 9, Ui.alpha(Ui.mix(CARD_BG, CARD_HOVER, s.hover), f));
        text(g, bold(s.name), x + 14, y + 10, TEXT, f);
        String val = String.format(Locale.ROOT, "%.2fx", s.value);
        text(g, val, x + w - 14 - this.font.width(val), y + 10, Ui.mix(SUB, ACCENT_HI, s.hover), f);

        int tx = x + 14;
        int tw = w - 28;
        int ty = y + h - 18;
        s.tx = tx;
        s.tw = tw;
        Ui.rrect(g, tx, ty, tw, 4, 2, Ui.alpha(TRACK_OFF, f));
        int fillW = Math.max(4, Math.round(tw * s.shown));
        Ui.rrect(g, tx, ty, fillW, 4, 2, Ui.alpha(ACCENT, f));
        int r = Math.round(Ui.lerp(4f, 6f, s.hover));
        Ui.rrect(g, tx + Math.round(tw * s.shown) - r, ty + 2 - r, r * 2, r * 2, r, Ui.alpha(WHITE, f));

        g.pose().popMatrix();
    }

    // ================================================================ Eingabe

    private void switchCategory(Category c) {
        if (c != current) {
            current = c;
            catT = 0f;
            scrollPos = 0f;
            scrollVel = 0f;
        }
    }

    private void updateSlider(SliderItem s, double mx) {
        if (s.tw <= 0) {
            return;
        }
        float t = Ui.clamp01((float) ((mx - s.tx) / s.tw));
        float v = s.min + (s.max - s.min) * t;
        v = Math.round(v / s.step) * s.step;
        s.value = Math.max(s.min, Math.min(s.max, v));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && openT > 0.9f && !closing) {
            double mx = event.x();
            double my = event.y();

            // Sidebar
            int px = wx + 14;
            int pw = sbW - 20;
            for (int i = 0; i < CATS.length; i++) {
                if (Ui.inside(mx, my, px, wy + CAT_TOP + i * CAT_STEP, pw, CAT_H)) {
                    switchCategory(CATS[i]);
                    return true;
                }
            }

            // Liste
            if (my >= listY && my < listY + listH && mx >= cx && mx < cx + cw) {
                for (Item it : Modules.of(current)) {
                    if (Ui.inside(mx, my, it.rx, it.ry, it.rw, it.rh)) {
                        it.press = 1f;
                        if (it instanceof ToggleItem t) {
                            t.value = !t.value;
                        } else if (it instanceof SliderItem s) {
                            dragging = s;
                            updateSlider(s, mx);
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            updateSlider(dragging, event.x());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0f || scrollPos != 0f) {
            float over = scrollPos < 0f ? -scrollPos : (scrollPos > maxScroll ? scrollPos - maxScroll : 0f);
            float damp = 1f / (1f + over / 30f); // am Rand wird der Schub schwaecher (Gummiband)
            scrollVel += -(float) scrollY * 520f * damp;
            scrollbarA = 1f;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Dieselbe Taste schliesst das Menue wieder (kurze Sperre, damit gehaltene Taste nicht sofort schliesst)
        if (System.nanoTime() - openedNs > 350_000_000L
                && Keys.matches(CleanMenuClient.openKey, event, CleanMenuClient.DEFAULT_KEY)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}
