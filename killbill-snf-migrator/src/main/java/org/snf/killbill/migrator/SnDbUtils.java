
package org.snf.killbill.migrator;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.billing.snf.domain.Payment;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.domain.UserLongPK;
import net.solarnetwork.util.JsonUtils;

/**
 * Utility methods for the SolarNetwork database.
 * 
 * @author matt
 */
public class SnDbUtils {

  public static Address addAddress(Connection con, Address addr) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(
        "insert into solarbill.bill_address (disp_name,email,country,time_zone,state_prov,locality,postal_code,address) "
            + "VALUES (?,?,?,?,?,?,?,?) RETURNING id,created",
        Statement.RETURN_GENERATED_KEYS)) {
      int col = 0;
      stmt.setString(++col, addr.getName());
      stmt.setString(++col, addr.getEmail());
      stmt.setString(++col, addr.getCountry());
      stmt.setString(++col, addr.getTimeZoneId());
      stmt.setString(++col, addr.getStateOrProvince());
      stmt.setString(++col, addr.getLocality());
      stmt.setString(++col, addr.getPostalCode());
      if (addr.getStreet() != null && addr.getStreet().length > 0) {
        Array street = con.createArrayOf("text", addr.getStreet());
        stmt.setArray(++col, street);
      } else {
        stmt.setNull(++col, Types.ARRAY);
      }
      stmt.executeUpdate();
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        rs.next();
        Long id = rs.getLong(1);
        Timestamp ts = rs.getTimestamp(2);
        Address result = new Address(id, Instant.ofEpochMilli(ts.getTime()));
        result.setName(addr.getName());
        result.setEmail(addr.getEmail());
        result.setCountry(addr.getCountry());
        result.setTimeZoneId(addr.getTimeZoneId());
        result.setStateOrProvince(addr.getStateOrProvince());
        result.setLocality(addr.getLocality());
        result.setPostalCode(addr.getPostalCode());
        return result;
      }
    }
  }

  public static Account addAccount(Connection con, Account account) throws SQLException {
    try (PreparedStatement stmt = con
        .prepareStatement("insert into solarbill.bill_account (user_id,addr_id,currency,locale) "
            + "VALUES (?,?,?,?) RETURNING id,created", Statement.RETURN_GENERATED_KEYS)) {
      int col = 0;
      stmt.setObject(++col, account.getUserId());
      stmt.setObject(++col, account.getAddress().getId());
      stmt.setString(++col, account.getCurrencyCode());
      stmt.setString(++col, account.getLocale());
      stmt.executeUpdate();
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        rs.next();
        Long id = rs.getLong(1);
        Timestamp ts = rs.getTimestamp(2);
        Account result = new Account(new UserLongPK(account.getUserId(), id),
            Instant.ofEpochMilli(ts.getTime()));
        result.setAddress(account.getAddress());
        result.setCurrencyCode(account.getCurrencyCode());
        result.setLocale(account.getLocale());
        return result;
      }
    }
  }

  public static void addInvoice(Connection con, SnfInvoice invoice) throws SQLException {
    try (PreparedStatement invoiceStmt = con.prepareStatement(
        "insert into solarbill.bill_invoice (id,created,acct_id,addr_id,date_start,date_end,currency) "
            + "VALUES (?,?,?,?,?,?,?)");
        PreparedStatement itemStmt = con.prepareStatement(
            "insert into solarbill.bill_invoice_item (inv_id,id,created,item_type,amount,quantity,item_key,jmeta) "
                + "VALUES (?,?::uuid,?,?,?,?,?,?::jsonb)")) {
      // first invoice
      int col = 0;
      invoiceStmt.setObject(++col, invoice.getId().getId());
      invoiceStmt.setTimestamp(++col, new Timestamp(invoice.getCreated().toEpochMilli()));
      invoiceStmt.setObject(++col, invoice.getAccountId());
      invoiceStmt.setObject(++col, invoice.getAddress().getId());
      invoiceStmt.setDate(++col, Date.valueOf(invoice.getStartDate()));
      invoiceStmt.setDate(++col, Date.valueOf(invoice.getEndDate()));
      invoiceStmt.setString(++col, invoice.getCurrencyCode());
      invoiceStmt.execute();

      // now items
      if (invoice.getItemCount() < 1) {
        return;
      }

      itemStmt.setObject(1, invoice.getId().getId());
      for (SnfInvoiceItem item : invoice.getItems()) {
        col = 1;
        itemStmt.setString(++col, item.getId().toString());
        itemStmt.setTimestamp(++col,
            new Timestamp(item.getCreated() != null ? item.getCreated().toEpochMilli()
                : invoice.getCreated().toEpochMilli()));
        itemStmt.setInt(++col, item.getItemType().getCode());
        itemStmt.setBigDecimal(++col, item.getAmount());
        itemStmt.setBigDecimal(++col, item.getQuantity());
        itemStmt.setString(++col, item.getKey());
        if (item.getMetadata() != null) {
          itemStmt.setString(++col, JsonUtils.getJSONString(item.getMetadata(), null));
        } else {
          itemStmt.setNull(++col, Types.VARCHAR);
        }
        itemStmt.execute();
      }
    }
  }

  public static void addPayment(Connection con, Payment payment) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(
        "insert into solarbill.bill_payment (id,created,acct_id,pay_type,amount,currency) "
            + "VALUES (?::uuid,?,?,?,?,?)")) {
      int col = 0;
      stmt.setString(++col, payment.getId().getId().toString());
      stmt.setTimestamp(++col, new Timestamp(payment.getCreated().toEpochMilli()));
      stmt.setObject(++col, payment.getAccountId());
      stmt.setInt(++col, payment.getPaymentType().getCode());
      stmt.setBigDecimal(++col, payment.getAmount());
      stmt.setString(++col, payment.getCurrencyCode());
      stmt.execute();
    }
  }

  public static void addInvoicePayment(Connection con, Payment payment, Long invoiceId,
      UUID invoicePaymentId, BigDecimal payAmount) throws SQLException {
    try (PreparedStatement stmt = con.prepareStatement(
        "insert into solarbill.bill_invoice_payment (id,created,acct_id,pay_id,inv_id,amount) "
            + "VALUES (?::uuid,?,?,?::uuid,?,?)")) {
      int col = 0;
      stmt.setString(++col, invoicePaymentId.toString());
      stmt.setTimestamp(++col, new Timestamp(payment.getCreated().toEpochMilli()));
      stmt.setObject(++col, payment.getAccountId());
      stmt.setString(++col, payment.getId().getId().toString());
      stmt.setObject(++col, invoiceId);
      stmt.setBigDecimal(++col, payAmount);
      stmt.execute();
    }
  }

}
