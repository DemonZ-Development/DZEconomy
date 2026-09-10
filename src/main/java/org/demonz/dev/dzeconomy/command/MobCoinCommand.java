package org.demonz.dev.dzeconomy.command;

import org.demonz.dev.dzeconomy.DZEconomy;
import org.demonz.dev.dzeconomy.currency.CurrencyType;

public class MobCoinCommand extends BaseCurrencyCommand {
    public MobCoinCommand(DZEconomy plugin) {
        super(plugin, CurrencyType.MOBCOIN, "mobcoin");
    }
}
