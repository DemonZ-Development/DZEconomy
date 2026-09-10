package org.demonz.dev.dzeconomy.command;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;

public class MoneyCommand extends BaseCurrencyCommand {
    public MoneyCommand(DZEconomy plugin) {
        super(plugin, CurrencyType.MONEY, "money");
    }
}
