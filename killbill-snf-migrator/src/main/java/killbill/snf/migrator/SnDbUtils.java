
package killbill.snf.migrator;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import net.solarnetwork.central.user.billing.snf.domain.Account;
import net.solarnetwork.central.user.billing.snf.domain.Address;
import net.solarnetwork.central.user.domain.UserLongPK;

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

}
