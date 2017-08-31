import model.BankAccount;
import org.apache.log4j.Logger;
import webclient.MbankWebClient;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Wosin on 28.08.2017.
 */
public class ScreenScraper {
    public static void main(String[] args) throws IOException {
        Logger.getLogger("com.gargoylesoftware").setLevel(org.apache.log4j.Level.ERROR);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Provide your MBank Username/Usercode");
        String username = scanner.next();

        System.out.println("Provide your MBank Password");
        String password = scanner.next();
        MbankWebClient mbankWebClient = new MbankWebClient();
        List<BankAccount> bankAccountList = mbankWebClient.getBankAccountsUsingCredentials(username, password);
        bankAccountList.forEach(System.out::println);
    }
}
