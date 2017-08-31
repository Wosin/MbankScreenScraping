package webclient;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import model.BankAccount;
import org.apache.log4j.Logger;
import utilities.Constants;
import utilities.ParsingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static utilities.Constants.*;
public class MbankWebClient {

    private static final Logger log = Logger.getLogger(ParsingUtils.class);

    private final WebClient webClient;

    public MbankWebClient() {
        this.webClient = new WebClient(BrowserVersion.FIREFOX_52);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(2000);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
    }

    private HtmlPage performLogin(String username, String passsword) throws IOException {
        HtmlPage page = webClient.getPage("https://m.mbank.pl/");
        HtmlTextInput form = page.getHtmlElementById(Constants.LOGIN_TEXTBOX_ID);
        form.setValueAttribute(username);
        HtmlPasswordInput formPass = page.getHtmlElementById(PASSWORD_TEXTBOX_ID);
        formPass.setValueAttribute(passsword);
        HtmlSubmitInput button = page.getHtmlElementById(SUBMIT_BUTTON_ID);
        return button.click();
    }

    private Optional<HtmlPage> getAccountsListPage(HtmlPage mainPage) throws IOException {
        Optional<HtmlPage> accountsPageOptional = Optional.empty();
        try {
            HtmlAnchor accountsAnchor = mainPage.getAnchorByHref(ACCOUNTS_LIST_ANCHOR_REF);
            HtmlPage accountsPage = accountsAnchor.click();
            accountsPageOptional = Optional.of(accountsPage);
        } catch (ElementNotFoundException exception){
            String mainPageAsString = mainPage.getWebResponse().getContentAsString();
            if(mainPageAsString.contains(LOGIN_FAILED_INFO)) {
                Optional<String> loginFailureReasonOptional = mainPage.getElementsByTagName("p").stream()
                        .map(DomNode::getTextContent)
                        .findFirst();
                log.warn("Login Failed!");
                loginFailureReasonOptional.ifPresent(s -> log.warn(s));
            }else {
                log.error("Bank API is different than the one that was screen scraped. Application is unable to work. Closing application...");
                throw new RuntimeException("Bank API has changed!");
            }
        }
        return accountsPageOptional;
    }

    private List<HtmlElement> getAccountLinks(HtmlPage accountsPage) {
        DomElement listOfAccounts;
        List<DomElement> tagList = new ArrayList<>(accountsPage.getElementsByTagName(LIST_TAG));

        if(tagList.isEmpty()) {
            log.error("Expected tag was not present on the website, this probably means that page has changed!");
            return new ArrayList<>();
        }else if (tagList.size() != 1) {
            log.debug("There were more tags than it was expected, this may mean that page has changed!");
        }

        listOfAccounts = tagList.get(0);

        List<HtmlElement> accountLinks = listOfAccounts.getElementsByTagName(LINK_TAG).stream()
                .filter(htmlElement -> htmlElement.getId().contains(ACCOUNT_DETAILS_LINK))
                .collect(Collectors.toList());

        return accountLinks;
    }

    private List<BankAccount> extractBankAccounts(List<HtmlElement> accountLinks) {
        List<BankAccount> accountsList = new ArrayList<>();
        accountLinks.forEach(accountLink -> {
            try {
                HtmlPage accountsPage = accountLink.click();
                HtmlDivision accountsDiv = accountsPage.getHtmlElementById(ACCOUNTS_DIV);

                List<String> accountDetails = accountsDiv.getElementsByTagName(SPAN_TAG).stream()
                        .map(DomNode::asText)
                        .collect(Collectors.toList());

                BankAccount bankAccount = new BankAccount(accountDetails);
                accountsList.add(bankAccount);
                goToPreviousPage(accountsPage);
            } catch (IOException e) {
                log.warn("Failed to obtain details for account", e);
            }
        });
        return accountsList;
    }

    private void goToPreviousPage(HtmlPage currentPage) throws IOException {
        HtmlElement backButton =  currentPage.getFirstByXPath("//a[@class='fr back']");
        if(backButton != null) {
            backButton.click();
        } else {
            log.warn("Expected button was not found. This probably means that API has changed.");
        }

    }

    public List<BankAccount> getBankAccountsUsingCredentials(String username, String password) {
        List<HtmlElement> accountsLinks = new ArrayList<>();
        try {
            HtmlPage mainPage = performLogin(username, password);

            Optional<HtmlPage> accountsPageOptional = getAccountsListPage(mainPage);
            if(!accountsPageOptional.isPresent()) {
                return new ArrayList<>();
            } else {
                accountsLinks = getAccountLinks(accountsPageOptional.get());
            }
        } catch (IOException ex) {
            log.warn("Failed to obtain data!", ex);
        }

        return extractBankAccounts(accountsLinks);
    }
}
