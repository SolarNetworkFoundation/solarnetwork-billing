
package killbill.snf.migrator.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Database configuration.
 * 
 * @author matt
 */
@Configuration
public class DatabaseConfig {

  private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

  private static DataSourceProperties logConnectionSettings(String name,
      DataSourceProperties props) {
    log.info("{} connection settings: {} @ {}", name, props.getUsername(), props.getUrl());
    return props;
  }

  @Bean
  @ConfigurationProperties("app.datasource.kb")
  public DataSourceProperties kbDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Qualifier("kb")
  public DataSource kbDataSource() {
    return logConnectionSettings("KB", kbDataSourceProperties()).initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
  }

  @Bean
  @Qualifier("kb")
  public JdbcOperations kbJdbc() {
    return new JdbcTemplate(kbDataSource());
  }

  @Primary
  @Bean
  @ConfigurationProperties("app.datasource.sn")
  public DataSourceProperties snDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Primary
  @Bean
  @Qualifier("sn")
  public DataSource snDataSource() {
    return logConnectionSettings("SN", snDataSourceProperties()).initializeDataSourceBuilder()
        .type(HikariDataSource.class).build();
  }

  @Primary
  @Bean
  @Qualifier("sn")
  public JdbcOperations snJdbc() {
    return new JdbcTemplate(snDataSource());
  }

}
