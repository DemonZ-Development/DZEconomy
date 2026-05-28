package online.demonzdevelopment.dzeconomy.command;

import online.demonzdevelopment.dzeconomy.DZEconomy;
import online.demonzdevelopment.dzeconomy.currency.CurrencyType;

public class MoneyCommand extends BaseCurrencyCommand {
    public MoneyCommand(DZEconomy plugin) {
        super(plugin, CurrencyType.MONEY, "money");
    }
}
