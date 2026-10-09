package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TrackerStore {
    private static final int BOOK_FADE_MAX = 40;
    private static final int BOOK_FADE_IN_STEP = 10;
    private static final int BOOK_FADE_OUT_STEP = 1;

    private final List<ActiveGain> active = new ArrayList<>();
    private final List<ActiveGain> view = Collections.unmodifiableList(active);
    private int bookFade;

    public void add(GainEntry entry) {
        active.add(new ActiveGain(entry));
    }

    public void tick() {
        active.removeIf(ActiveGain::expireAfterTick);
        bookFade = active.isEmpty() ? Math.max(0, bookFade - BOOK_FADE_OUT_STEP) : Math.min(BOOK_FADE_MAX, bookFade + BOOK_FADE_IN_STEP);
    }

    public void clear() {
        active.clear();
        bookFade = 0;
    }

    public List<ActiveGain> entries() {
        return view;
    }

    public boolean isEmpty() {
        return active.isEmpty();
    }

    public int bookFade() {
        return bookFade;
    }

    public float bookAlpha() {
        return bookFade / (float) BOOK_FADE_MAX;
    }
}
