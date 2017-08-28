import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Created by Wosin on 28.08.2017.
 */
public class ScreenScraper {
    public static void main(String[] args) throws IOException {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Provide your MBank Username/Usercode");
        String username = scanner.next();

        System.out.println("Provide your MBank Password");
        String password = scanner.next();
        MbankWebClient mbankWebClient = new MbankWebClient();
        List<BankAccount> bankAccountList = mbankWebClient.getBankAccountsUsingCredentials(username, password);
        bankAccountList.forEach(account -> System.out.println(account));
    }
}
