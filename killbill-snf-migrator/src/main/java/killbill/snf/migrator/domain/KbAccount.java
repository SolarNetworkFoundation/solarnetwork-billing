
package killbill.snf.migrator.domain;

import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

/**
 * Kill Bill account data.
 * 
 * @author matt
 */
public class KbAccount {

  public Long recordId;
  public UUID id;
  public String externalKey;
  public String email;
  public String name;
  public String currencyCode;
  public String timeZoneId;
  public String locale;
  public String[] address;
  public String city;
  public String state;
  public String country;
  public String postalCode;
  public String phone;

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("KbAccount [id=");
    builder.append(id);
    builder.append(", externalKey=");
    builder.append(externalKey);
    builder.append(", email=");
    builder.append(email);
    builder.append("]");
    return builder.toString();
  }

  public ZoneId timeZone() {
    return ZoneId.of(timeZoneId);
  }

  public Locale locale() {
    String[] components = locale.split("_", 2);
    return new Locale(components[0], components[1]);
  }

  public Long snUserId() {
    if (externalKey != null && externalKey.startsWith("SN_")) {
      return Long.valueOf(externalKey.substring(3));
    }
    return null;
  }

}
