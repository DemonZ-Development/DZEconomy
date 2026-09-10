package org.demonz.dev.dzeconomy.util;

import org.demonz.dev.dzeconomy.adapter.FeatureAdapter;

public class ColorUtil {
    
    public static String translate(String text) {
        if (text == null) return "";
        return FeatureAdapter.get().translateColors(text);
    }
}
