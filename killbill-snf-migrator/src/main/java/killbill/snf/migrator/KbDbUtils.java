
package killbill.snf.migrator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import killbill.snf.migrator.domain.KbAccount;
import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.InvoiceItemType;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoice;
import net.solarnetwork.central.user.billing.snf.domain.SnfInvoiceItem;
import net.solarnetwork.central.user.billing.snf.domain.UsageInfo;

/**
 * Utility methods for the Kill Bill database.
 * 
 * @author matt
 */
public class KbDbUtils {

  private static final Logger log = LoggerFactory.getLogger(KbDbUtils.class);

  public static List<KbAccount> allAccounts(JdbcOperations jdbc) {
    return jdbc.query("select * from accounts order by record_id", new RowMapper<KbAccount>() {

      @Override
      public KbAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        KbAccount account = new KbAccount();
        account.recordId = rs.getLong("record_id");
        account.id = UUID.fromString(rs.getString("id"));
        account.externalKey = rs.getString("external_key");
        account.email = rs.getString("email");
        account.name = rs.getString("name");
        account.currencyCode = rs.getString("currency");
        account.timeZoneId = rs.getString("time_zone");
        account.locale = rs.getString("locale");

        List<String> addr = new ArrayList<>(2);
        String s = rs.getString("address1");
        if (s != null && !s.isEmpty()) {
          addr.add(s);
          s = rs.getString("address2");
          if (s != null && !s.isEmpty()) {
            addr.add(s);
          }
          account.address = addr.toArray(new String[addr.size()]);
        }

        account.city = rs.getString("city");
        account.state = rs.getString("state_or_province");
        account.country = rs.getString("country");
        account.postalCode = rs.getString("postal_code");
        account.phone = rs.getString("phone");

        return account;
      }

    });
  }

  public static Map<String, Object> usageMetadata(Long nodeId, UsageInfo usageData) {
    Map<String, Object> result = new LinkedHashMap<>(2);
    result.put(SnfInvoiceItem.META_NODE_ID, nodeId);
    if (usageData != null) {
      result.put(SnfInvoiceItem.META_USAGE, usageData.toMetadata());
    }
    return result;
  }

  public static void allInvoiceItems(JdbcOperations jdbc, Long kbAccountId, Account account,
      Consumer<SnfInvoice> consumer) throws SQLException {
    final String sql = Utils.getResource("sql/kb-invoice-items-with-usage-query.sql",
        KbDbUtils.class);
    final ZoneId tz = account.getTimeZone();
    jdbc.execute(new PreparedStatementCreator() {

      @Override
      public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY);
        stmt.setObject(1, kbAccountId);
        return stmt;
      }
    }, new PreparedStatementCallback<Void>() {

      private SnfInvoice currInvoice = null;
      private Long currNodeId = null;
      private SnfInvoiceItem currInvoiceItem = null;
      private UsageInfo currUsageInfo = null;

      private void addCurrItem() {
        if (currInvoice == null || currInvoiceItem == null) {
          return;
        }
        Set<SnfInvoiceItem> items = currInvoice.getItems();
        if (items == null) {
          items = new LinkedHashSet<>(8);
          currInvoice.setItems(items);
        }
        if (currUsageInfo != null) {
          currInvoiceItem.setQuantity(currUsageInfo.getAmount());
          currInvoiceItem.setMetadata(usageMetadata(currNodeId, currUsageInfo));
          currUsageInfo = null;
        }
        items.add(currInvoiceItem);
        currInvoiceItem = null;
      }

      @Override
      public Void doInPreparedStatement(PreparedStatement ps)
          throws SQLException, DataAccessException {
        try (ResultSet rs = ps.executeQuery()) {
          int col = 0;
          while (rs.next()) {
            col = 0;
            final Long invRecordId = rs.getLong(++col);
            final Date invDate = rs.getDate(++col);

            if (currInvoice == null || !currInvoice.getId().getId().equals(invRecordId)) {
              // starting a new invoice
              if (currInvoice != null) {
                addCurrItem();
                consumer.accept(currInvoice);
              }

              currInvoice = new SnfInvoice(invRecordId, account.getUserId(),
                  account.getId().getId(), invDate.toLocalDate().atStartOfDay(tz).toInstant());
              currInvoice.setAddress(account.getAddress());
              currInvoice.setCurrencyCode(account.getCurrencyCode());
            }

            final UUID itemId = UUID.fromString(rs.getString(++col));
            if (currInvoiceItem == null || !currInvoiceItem.getId().equals(itemId)) {
              addCurrItem();

              final String itemTypeName = rs.getString(++col);
              InvoiceItemType itemType;
              try {
                itemType = InvoiceItemType.valueOf(itemTypeName);
              } catch (IllegalArgumentException | NullPointerException e) {
                log.info("Dropping unsupported invoice item type [{}]", itemTypeName);
                currNodeId = null;
                currInvoiceItem = null;
                currUsageInfo = null;
                continue;
              }
              final String itemKey = rs.getString(++col);
              final BigDecimal itemAmount = rs.getBigDecimal(++col);
              currInvoiceItem = new SnfInvoiceItem(itemId, currInvoice.getId().getId(),
                  currInvoice.getCreated());
              currInvoiceItem.setItemType(itemType);
              currInvoiceItem.setKey(itemKey);
              currInvoiceItem.setQuantity(BigDecimal.ZERO);
              currInvoiceItem.setAmount(itemAmount.setScale(2, RoundingMode.HALF_UP));

              final Date itemStartDate = rs.getDate(++col);
              if (itemStartDate != null) {
                LocalDate itemStart = itemStartDate.toLocalDate();
                if (currInvoice.getStartDate() == null
                    || itemStart.isBefore(currInvoice.getStartDate())) {
                  currInvoice.setStartDate(itemStart);
                }
              }
              final Date itemEndDate = rs.getDate(++col);
              if (itemEndDate != null) {
                LocalDate itemEnd = itemEndDate.toLocalDate();
                if (currInvoice.getEndDate() == null || itemEnd.isAfter(currInvoice.getEndDate())) {
                  currInvoice.setEndDate(itemEnd);
                }
              }
            } else {
              col += 5;
            }

            String nodeId = rs.getString(++col);
            if (nodeId != null) {
              currNodeId = Long.valueOf(nodeId);
            }
            final BigDecimal usageValue = rs.getBigDecimal(++col);
            if (usageValue != null) {
              if (currUsageInfo == null) {
                currUsageInfo = new UsageInfo(currInvoiceItem.getKey(), usageValue, null);
              } else {
                currUsageInfo = new UsageInfo(currInvoiceItem.getKey(),
                    currUsageInfo.getAmount().add(usageValue), null);
              }
            }
          }
        }
        return null;
      }

    });
  }

}
