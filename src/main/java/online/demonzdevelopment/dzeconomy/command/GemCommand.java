package online.demonzdevelopment.dzeconomy.command;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

public class GemCommand extends BaseCurrencyCommand {
    public GemCommand(DZEconomy plugin) {
        super(plugin, CurrencyType.GEM, "gem");
    }
}
