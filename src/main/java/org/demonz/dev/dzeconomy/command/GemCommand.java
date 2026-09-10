package org.demonz.dev.dzeconomy.command;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;

public class GemCommand extends BaseCurrencyCommand {
    public GemCommand(DZEconomy plugin) {
        super(plugin, CurrencyType.GEM, "gem");
    }
}
