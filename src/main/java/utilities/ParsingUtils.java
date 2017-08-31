package utilities;

import org.apache.log4j.Logger;

/**
 * Created by Wosin on 28.08.2017.
 */
public class ParsingUtils {

    private static final Logger log = Logger.getLogger(ParsingUtils.class);

    public static String getCurrencyFromBalance(String balance) {
       String currency =  balance.substring(balance.length()-3);
       if(currency.matches("[A-Za-z]{3}")){
           return currency;
       } else {
           log.debug("No currency code was present, or code was not standarized.");
           return "";
       }
    }

    public static double parseDoubleFromBalance(String balance) {
      String balanceUnified = balance.replaceAll("[A-Za-z ]","").replace(",",".");
      return Double.valueOf(balanceUnified);
    }
}
