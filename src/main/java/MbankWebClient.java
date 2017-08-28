import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MbankWebClient {

    public static Logger log = Logger.getLogger(ParsingUtils.class);

    private static final String LOGIN_TEXTBOX_ID = "MasterContentPlaceHolder_LoginControl_LoginBaseControl_AdvancedLoginControl_ExtendedLoginTextBox";
    private static final String PASSWORD_TEXTBOX_ID = "MasterContentPlaceHolder_LoginControl_LoginBaseControl_AdvancedLoginControl_PasswordTextBox";
    private static final String SUBMIT_BUTTON_ID = "MasterContentPlaceHolder_LoginControl_LoginBaseControl_AdvancedLoginControl_LoginButton";
    private static final String ACCOUNTS_LIST_ANCHOR_REF = "Accounts/AccountList.aspx";
    private final WebClient webClient;

    public MbankWebClient() {
        this.webClient = new WebClient(BrowserVersion.FIREFOX_52);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(2000);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
    }

    private HtmlPage performLogin(String username, String passsword) throws IOException {
        HtmlPage page = webClient.getPage("https://m.mbank.pl/");
        HtmlTextInput form = page.getHtmlElementById(LOGIN_TEXTBOX_ID);
        form.setValueAttribute(username);
        HtmlPasswordInput formPass = page.getHtmlElementById(PASSWORD_TEXTBOX_ID);
        formPass.setValueAttribute(passsword);
        HtmlSubmitInput button = page.getHtmlElementById(SUBMIT_BUTTON_ID);
        return button.click();
    }

    private HtmlPage getAccountsListPage(HtmlPage mainPage) throws IOException {
        HtmlAnchor accountsAnchor = mainPage.getAnchorByHref(ACCOUNTS_LIST_ANCHOR_REF);
        return accountsAnchor.click();
    }

    private List<HtmlAnchor> getAccountLinks(HtmlPage accountsPage) throws IOException {
        HtmlUnorderedList accountData = accountsPage.getFirstByXPath("//ul[@class='aList bigAList bordered topBorder']");
        List<HtmlAnchor> accountLinks = accountData.getByXPath("//a[contains(@id, 'AccountDetailsLink')]");
        return accountLinks;
    }

    private List<BankAccount> extractBankAccounts(List<HtmlAnchor> accountLinks) {
        List<BankAccount> accountsList = new ArrayList<>();
        accountLinks.forEach(accountLink -> {
            try {
                HtmlPage accountsPage = accountLink.click();
                HtmlDivision accountsDiv = accountsPage.getHtmlElementById("titleBar");
                List<String> accountDetails = accountsDiv.getElementsByTagName("span").stream()
                        .map(htmlElement -> htmlElement.asText())
                        .collect(Collectors.toList());
                BankAccount bankAccount = new BankAccount(accountDetails);
                accountsList.add(bankAccount);
                ((HtmlElement) accountsPage.getFirstByXPath("//a[@class='fr back']")).click();
            } catch (IOException e) {
                log.warn("Failed to obtain details for account", e);
            }
        });
        return accountsList;
    }

    public List<BankAccount> getBankAccountsUsingCredentials(String username, String password) {
        List<HtmlAnchor> accountsLinks = new ArrayList<>();
        try {
            HtmlPage mainPage = performLogin(username, password);
            HtmlPage accountsPage = getAccountsListPage(mainPage);
            accountsLinks = getAccountLinks(accountsPage);
        } catch (IOException ex) {
            log.warn("Failed to obtain data!", ex);
        }
        List<BankAccount> accountsList = extractBankAccounts(accountsLinks);

        return accountsList;
    }
}
