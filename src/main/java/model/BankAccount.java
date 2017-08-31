package model;

import java.util.List;

/**
 * Created by Wosin on 28.08.2017.
 */
import static utilities.ParsingUtils.getCurrencyFromBalance;
import static utilities.ParsingUtils.parseDoubleFromBalance;
public class BankAccount {
    private String accountNumber;
    private double totalBalance;
    private double availableBalance;
    private String currency;

    @Override
    public String toString() {
        return "model.BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", totalMoney=" + totalBalance +
                ", availableMoney=" + availableBalance +
                ", currency='" + currency + '\'' +
                '}';
    }

    public BankAccount(List<String> accountDetailsList) {
        assert accountDetailsList.size() == 3;
        assert accountDetailsList.get(0).length() >= 24;
        String accountTotalMoney = accountDetailsList.get(1);
        String accountAvailableMoney = accountDetailsList.get(2);
        String currency = getCurrencyFromBalance(accountTotalMoney);

        assert accountAvailableMoney.contains(currency);

        this.accountNumber = accountDetailsList.get(0);
        this.currency = currency;
        this.totalBalance = parseDoubleFromBalance(accountTotalMoney);
        this.availableBalance = parseDoubleFromBalance(accountAvailableMoney);
    }
}