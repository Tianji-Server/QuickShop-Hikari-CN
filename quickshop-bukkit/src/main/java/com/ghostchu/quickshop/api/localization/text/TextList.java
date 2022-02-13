package com.ghostchu.quickshop.api.localization.text;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TextList {

    /**
     * Getting the text that use specify locale
     *
     * @param locale The minecraft locale code (like en_us)
     * @return The text
     */
    @NotNull List<Component> forLocale(@NotNull String locale);

    /**
     * Getting the text for player locale
     *
     * @return Getting the text for player locale
     */
    @NotNull List<Component> forLocale();

    /**
     * Send text to the player
     */
    void send();
}
