/*
 *  This file is a part of project QuickShop, the name is CachePerformanceItem.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import com.google.common.cache.CacheStats;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class CachePerformanceItem implements SubPasteItem {
    private final QuickShop plugin = QuickShop.getInstance();

    @Override
    public @NotNull String getTitle() {
        return "Cache Performance";
    }

    @NotNull
    private String buildPAPICacheContent() {
        if (plugin.getPlaceHolderAPI() == null || plugin.getQuickShopPAPI() == null) {
            return "<p>PlaceHolderAPI feature disabled.</p>";
        }
        if (!plugin.getQuickShopPAPI().isRegistered()) {
            return "<p>PlaceHolderAPI feature not registered yet.</p>";
        }
        if (plugin.getQuickShopPAPI().getPapiCache() == null) {
            return "<p>PlaceHolderAPI Cache disabled.</p>";
        }
        CacheStats stats = plugin.getQuickShopPAPI().getPapiCache().getStats();
        return renderTable(stats);
    }

    @NotNull
    private String buildShopCacheContent() {
        if (plugin.getShopCache() == null)
            return "<p>Shop Cache disabled.</p>";
        CacheStats stats = plugin.getShopCache().getStats();
        return renderTable(stats);
    }

    @NotNull
    private String renderTable(@NotNull CacheStats stats) {
        HTMLTable table = new HTMLTable(2, true);
        table.insert("Average Load Penalty", round(stats.averageLoadPenalty()));
        table.insert("Hit Rate", round(stats.hitRate()));
        table.insert("Miss Rate", round(stats.missRate()));
        table.insert("Hit Count", String.valueOf(stats.hitCount()));
        table.insert("Miss Count", String.valueOf(stats.missCount()));
        table.insert("Load Count", String.valueOf(stats.loadCount()));
        table.insert("Load Success Count", String.valueOf(stats.loadSuccessCount()));
        table.insert("Eviction Count", String.valueOf(stats.evictionCount()));
        table.insert("Request Count", String.valueOf(stats.requestCount()));
        table.insert("Total Loading Time", String.valueOf(stats.totalLoadTime()));
        return table.render();
    }

    @NotNull
    private String round(double d) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(3);
        return nf.format(d);
    }

    @Override
    public @NotNull String genBody() {
        return "<h5>Shop Cache</h5>" +
                buildShopCacheContent() +
                "<h5>PlaceHolderAPI Cache</h5>" +
                buildPAPICacheContent();
    }
}
