
package org.snf.killbill.migrator;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import net.solarnetwork.central.user.billing.snf.domain.Payment;
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

    final Long userId = kbAccount.snUserId();
    Account account = new Account(userId, null);
    account.setAddress(addr);
    account.setCurrencyCode(kbAccount.currencyCode);
    account.setLocale(kbAccount.locale().toLanguageTag());
    account = SnDbUtils.addAccount(con, account);
    final Long accountId = account.getId().getId();
    log.info("Created SN account {}: {}", accountId, account);

    Map<Long, SnfInvoice> allInvoices = new LinkedHashMap<>(32);
    KbDbUtils.allInvoiceItems(kbJdbc, kbAccount.recordId, account, new Consumer<SnfInvoice>() {

      @Override
      public void accept(SnfInvoice invoice) {
        if (invoice.getItemCount() < 1) {
          log.debug("Ignoring empty invoice {}", invoice);
          return;
        }
        try {
          SnDbUtils.addInvoice(con, invoice);
          allInvoices.put(invoice.getId().getId(), invoice);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        log.info("Migrated invoice {}", invoice);
      }
    });

    KbDbUtils.allPayments(kbJdbc, kbAccount.recordId, account, new Consumer<Payment>() {

      @Override
      public void accept(Payment payment) {
        try {
          Long invoiceId = Long.valueOf(payment.getReference());
          UUID invoicePaymentId = UUID.fromString(payment.getExternalKey());
          payment.setReference(null);
          payment.setExternalKey(null);
          SnDbUtils.addPayment(con, payment);
          BigDecimal invoiceAmount = allInvoices.get(invoiceId).getTotalAmount();
          BigDecimal invoicePayAmount = payment.getAmount();
          if (invoicePayAmount.compareTo(invoiceAmount) > 0) {
            invoicePayAmount = invoiceAmount;
            log.warn("Invoice payment amount {} capped to invoice amount {}", invoicePayAmount,
                invoiceAmount);
          }
          SnDbUtils.addInvoicePayment(con, payment, invoiceId, invoicePaymentId, invoicePayAmount);
          log.info("Applied payment {} to invoice {}", payment, invoiceId);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        log.info("Migrated payment {}", payment);
      }

    });
  }

}
