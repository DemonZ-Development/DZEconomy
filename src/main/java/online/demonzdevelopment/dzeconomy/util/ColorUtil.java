package online.demonzdevelopment.dzeconomy.util;

import online.demonzdevelopment.dzeconomy.adapter.FeatureAdapter;

public class ColorUtil {
    
    public static String translate(String text) {
        if (text == null) return "";
        return FeatureAdapter.get().translateColors(text);
    }
}
