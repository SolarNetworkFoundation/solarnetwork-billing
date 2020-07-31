
package org.snf.killbill.migrator;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snf.killbill.migrator.config.DatabaseConfig;
import org.snf.killbill.migrator.domain.KbAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.util.StreamUtils;

import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;

/**
 * Main Kill Bill to SNF migration application.
 * 
 * @author matt
 */
@SpringBootApplication(scanBasePackageClasses = DatabaseConfig.class)
public class MigratorTool implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(MigratorTool.class);

  public static void main(String[] args) {
    new SpringApplicationBuilder().sources(MigratorTool.class).web(WebApplicationType.NONE)
        .logStartupInfo(false).build().run(args);
  }

  @Autowired
  @Qualifier("kb")
  private JdbcOperations kbJdbc;

  @Autowired
  @Qualifier("sn")
  private JdbcOperations snJdbc;

  private boolean forReals = true;

  private void showHelp() {
    try (InputStream in = getClass().getResourceAsStream("/help.txt")) {
      StreamUtils.copy(in, System.out);
    } catch (IOException e) {
      System.err.println("Error printing help: " + e);
    }
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    boolean wantHelp = args.getOptionNames().contains("help");
    if (wantHelp) {
      showHelp();
      return;
    }
    forReals = !args.getOptionNames().contains("dry-run");

    List<KbAccount> kbAccounts = KbDbUtils.allAccounts(kbJdbc).stream()
        .filter(e -> e.snUserId() != null).collect(toList());
    log.info("Discovered {} KB accounts", kbAccounts.size());

    snJdbc.execute(new ConnectionCallback<Void>() {

      @Override
      public Void doInConnection(Connection con) throws SQLException, DataAccessException {
        con.setAutoCommit(false);
        for (KbAccount account : kbAccounts) {
          processAccount(con, account);
        }
        if (forReals) {
          log.info("COMMIT: finishing transaction.");
          con.commit();
        } else {
          log.info("DRY RUN: rolling back transaction.");
          con.rollback();
        }
        return null;
      }

    });
  }

  private void processAccount(Connection con, KbAccount kbAccount) throws SQLException {
    log.info("Migrating {}", kbAccount);
    // create address + account
    Address addr = new Address();
    addr.setName(kbAccount.name);
    addr.setEmail(kbAccount.email);
    addr.setTimeZoneId(kbAccount.timeZoneId);
    addr.setStreet(kbAccount.address);
    addr.setLocality(kbAccount.city);
    addr.setStateOrProvince(kbAccount.state);
    addr.setPostalCode(kbAccount.postalCode);
    addr.setCountry(kbAccount.country);
    addr = SnDbUtils.addAddress(con, addr);
    log.info("Created SN address {}: {}", addr.getId(), addr);

    Account account = new Account(kbAccount.snUserId(), null);
    account.setAddress(addr);
    account.setCurrencyCode(kbAccount.currencyCode);
    account.setLocale(kbAccount.locale().toLanguageTag());
    account = SnDbUtils.addAccount(con, account);
    log.info("Created SN account {}: {}", account.getId(), account);

    KbDbUtils.allInvoiceItems(kbJdbc, kbAccount.recordId, account, new Consumer<SnfInvoice>() {

      @Override
      public void accept(SnfInvoice invoice) {
        if (invoice.getItemCount() < 1) {
          log.debug("Ignoring empty invoice {}", invoice);
          return;
        }
        try {
          SnDbUtils.addInvoice(con, invoice);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        log.info("Migrated invoice {}", invoice);
      }
    });
  }

}
