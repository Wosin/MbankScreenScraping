import java.util.List;

/**
 * Created by Wosin on 28.08.2017.
 */
public class BankAccount {
    String accountNumber;
    double totalBalance;
    double availableBalance;
    String currency;

    @Override
    public String toString() {
        return "BankAccount{" +
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
        String currency = ParsingUtils.getCurrencyFromBalance(accountTotalMoney);

        assert accountAvailableMoney.contains(currency);

        this.accountNumber = accountDetailsList.get(0);
        this.currency = currency;
        this.totalBalance = ParsingUtils.parseDoubleFromBalance(accountTotalMoney);
        this.availableBalance = ParsingUtils.parseDoubleFromBalance(accountAvailableMoney);
    }
}