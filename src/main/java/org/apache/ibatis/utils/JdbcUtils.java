package org.apache.ibatis.utils;

import org.apache.ibatis.session.enums.DatabaseType;

/**
 * <p>
 * JDBC 工具类
 * </p>
 */
public class JdbcUtils {

	/**
	 * <p>
	 * 根据连接地址判断数据库类型
	 * </p>
	 * @param jdbcUrl 连接地址
	 * @return
	 */
	public static DatabaseType getDatabaseType(String jdbcUrl) {
		if (StringUtils.isEmpty(jdbcUrl)) {
			return DatabaseType.MYSQL;
		}
		if (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:cobar:") || jdbcUrl.startsWith("jdbc:log4jdbc:mysql:")) {
			return DatabaseType.MYSQL;
		} else if (jdbcUrl.startsWith("jdbc:oracle:") || jdbcUrl.startsWith("jdbc:log4jdbc:oracle:")) {
			return DatabaseType.ORACLE;
		} else if (jdbcUrl.startsWith("jdbc:microsoft:") || jdbcUrl.startsWith("jdbc:log4jdbc:microsoft:")) {
			return DatabaseType.SQLSERVER;
		} else if (jdbcUrl.startsWith("jdbc:sqlserver:") || jdbcUrl.startsWith("jdbc:log4jdbc:sqlserver:")) {
			return DatabaseType.SQLSERVER;
		} else if (jdbcUrl.startsWith("jdbc:postgresql:") || jdbcUrl.startsWith("jdbc:log4jdbc:postgresql:")) {
			return DatabaseType.POSTGRE;
		} else if (jdbcUrl.startsWith("jdbc:hsqldb:") || jdbcUrl.startsWith("jdbc:log4jdbc:hsqldb:")) {
			return DatabaseType.HSQL;
		} else if (jdbcUrl.startsWith("jdbc:db2:")) {
			return DatabaseType.DB2;
		} else if (jdbcUrl.startsWith("jdbc:sqlite:")) {
			return DatabaseType.SQLITE;
		} else if (jdbcUrl.startsWith("jdbc:h2:") || jdbcUrl.startsWith("jdbc:log4jdbc:h2:")) {
			return DatabaseType.H2;
		} else {
			return DatabaseType.OTHER;
		}
	}

}
