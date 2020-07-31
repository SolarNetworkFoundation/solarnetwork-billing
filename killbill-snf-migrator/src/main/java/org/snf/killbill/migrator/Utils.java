
package org.snf.killbill.migrator;

import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.util.FileCopyUtils;

/**
 * General utilities.
 * 
 * @author matt
 */
public class Utils {

  public static String getResource(String resourceName, Class<?> myClass) {
    try {
      return FileCopyUtils
          .copyToString(new InputStreamReader(myClass.getResourceAsStream(resourceName), "UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
