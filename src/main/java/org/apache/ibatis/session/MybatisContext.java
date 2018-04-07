/*
 * Copyright (c) 2018 (hnxyhcwdl1003@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.session;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.sql.DataSource;

import org.apache.ibatis.exception.MybatisException;
import org.apache.ibatis.injector.MapperSQLInjector;
import org.apache.ibatis.injector.annotation.AnnotationMapperSQLInjector;
import org.apache.ibatis.jdbc.SqlReservedWords;
import org.apache.ibatis.plugin.meta.MetaObjectHandler;
import org.apache.ibatis.session.enums.ColumnStrategy;
import org.apache.ibatis.session.enums.DatabaseType;
import org.apache.ibatis.session.enums.PKStrategy;
import org.apache.ibatis.utils.IOUtils;
import org.apache.ibatis.utils.JdbcUtils;
import org.apache.ibatis.utils.StringUtils;
import org.apache.ibatis.utils.TableMetadataUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mybatis对象引用缓存
 */
@SuppressWarnings("serial")
public class MybatisContext implements Cloneable, Serializable {
	
	protected static final Logger LOG = LoggerFactory.getLogger(MybatisContext.class);
	/**
	 * 默认参数
	 */
	public static final MybatisContext DEFAULT = new MybatisContext();
	/**
	 * 与线程无关的全局对象信息
	 */
	protected static ConcurrentMap<String, MybatisContext> MYBATIS_GLOBAL_CONFIG = new ConcurrentHashMap<String, MybatisContext>();
	
	// 数据库类型（默认 MySql）
	protected DatabaseType dbType = DatabaseType.MYSQL;
	// 主键类型（默认 ID_WORKER）
	protected PKStrategy idType = PKStrategy.ID_WORKER;
	// 表名、字段名、是否使用下划线命名（默认 false）
	protected boolean dbColumnUnderline = false;
	// SQL注入器
	protected MapperSQLInjector sqlInjector;
	// 元对象字段填充控制器
	protected MetaObjectHandler metaObjectHandler = null;
	// 字段验证策略
	protected ColumnStrategy columnStrategy = ColumnStrategy.NOT_NULL;
	// 是否刷新mapper
	protected boolean isRefresh = false;
	// 是否自动获取DBType
	protected boolean isAutoSetDbType = true;
	// 是否大写命名
	protected boolean isCapitalMode = false;
	// 标识符
	protected String identifierQuote;
	// 缓存当前Configuration的SqlSessionFactory
	protected SqlSessionFactory sqlSessionFactory;
	// 缓存已注入CRUD的Mapper信息
	protected Set<String> mapperRegistryCache = new ConcurrentSkipListSet<String>();
	// 单例重用SqlSession
	protected SqlSession sqlSession;
	// 批量SqlSession
	protected SqlSession sqlsessionBatch;

	@Override
	protected MybatisContext clone() throws CloneNotSupportedException {
		return (MybatisContext) super.clone();
	}

	/**
	 * 获取默认MybatisContext
	 * @return
	 */
	public static MybatisContext defaults() {
		try {
			MybatisContext clone = DEFAULT.clone();
			clone.setSqlInjector(new AnnotationMapperSQLInjector());
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new MybatisException("ERROR: CLONE MybatisContext DEFAULT FAIL !  Cause:" + e);
		}
	}

	/**
	 * <p>
	 * 设置全局设置(以configuration地址值作为Key)
	 * <p/>
	 * @param configuration
	 * @param context
	 * @return
	 */
	public static void bindContext(Configuration configuration, MybatisContext context) {
		if (configuration == null || context == null) {
			new MybatisException("Error: Could not bindContext");
		}
		// 设置全局设置
		MYBATIS_GLOBAL_CONFIG.put(configuration.toString(), context);
	}

	/**
	 * <p>
	 * 标记全局设置 (统一所有入口)
	 * </p>
	 * @param sqlSessionFactory
	 * @return
	 */
	public SqlSessionFactory bindContext(SqlSessionFactory sqlSessionFactory) {
		if (null != sqlSessionFactory) {
			bindContext(sqlSessionFactory.getConfiguration(), this);
		}
		return sqlSessionFactory;
	}
	
	public MybatisContext() {
		// 构造方法
	}

	public MybatisContext(MapperSQLInjector sqlInjector) {
		this.sqlInjector = sqlInjector;
	}
	
	public DatabaseType getDatabaseType() {
		return dbType;
	}

	public void setDatabaseType(String dbType) {
		this.dbType = DatabaseType.getDBType(dbType);
		this.isAutoSetDbType = false;
	}

	public void setDatabaseTypeByJdbcURL(String jdbcUrl) {
		this.dbType = JdbcUtils.getDatabaseType(jdbcUrl);
	}

	public PKStrategy getPKStrategy() {
		return idType;
	}

	public void setPKStrategy(int idType) {
		this.idType = PKStrategy.getPKStrategy(idType);
	}

	public boolean isDbColumnUnderline() {
		return dbColumnUnderline;
	}

	public void setDbColumnUnderline(boolean dbColumnUnderline) {
		this.dbColumnUnderline = dbColumnUnderline;
	}

	public MapperSQLInjector getSqlInjector() {
		return sqlInjector;
	}

	public void setSqlInjector(MapperSQLInjector sqlInjector) {
		this.sqlInjector = sqlInjector;
	}

	public MetaObjectHandler getMetaObjectHandler() {
		return metaObjectHandler;
	}

	public void setMetaObjectHandler(MetaObjectHandler metaObjectHandler) {
		this.metaObjectHandler = metaObjectHandler;
	}

	public ColumnStrategy getColumnStrategy() {
		return columnStrategy;
	}

	public void setColumnStrategy(int columnStrategy) {
		this.columnStrategy = ColumnStrategy.getFieldStrategy(columnStrategy);
	}

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setRefresh(boolean refresh) {
		this.isRefresh = refresh;
	}

	public boolean isAutoSetDbType() {
		return isAutoSetDbType;
	}

	public void setAutoSetDbType(boolean autoSetDbType) {
		this.isAutoSetDbType = autoSetDbType;
	}

	public Set<String> getMapperRegistryCache() {
		return mapperRegistryCache;
	}

	public void setMapperRegistryCache(Set<String> mapperRegistryCache) {
		this.mapperRegistryCache = mapperRegistryCache;
	}

	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
		this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
		this.sqlsessionBatch = new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
	}

	public boolean isCapitalMode() {
		return isCapitalMode;
	}

	public void setCapitalMode(boolean isCapitalMode) {
		this.isCapitalMode = isCapitalMode;
	}

	public String getIdentifierQuote() {
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote) {
		this.identifierQuote = identifierQuote;
	}

	public void setSQLKeywords(String sqlKeywords) {
		if (StringUtils.isNotEmpty(sqlKeywords)) {
			SqlReservedWords.RESERVED_WORDS.addAll(StringUtils.splitWorker(sqlKeywords.toUpperCase(), ",", -1, false));
		}
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public SqlSession getSqlsessionBatch() {
		return sqlsessionBatch;
	}

	/**
	 * 设置元数据相关属性
	 * @param dataSource
	 * @param context
	 */
	public void setMetaData(DataSource dataSource) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			String jdbcUrl = connection.getMetaData().getURL();
			// 设置全局关键字
			this.setSQLKeywords(connection.getMetaData().getSQLKeywords());
			// 自动设置数据库类型
			if (this.isAutoSetDbType()) {
				this.setDatabaseTypeByJdbcURL(jdbcUrl);
			}
		} catch (SQLException e) {
			LOG.warn("Warn: MybatisContext setMetaData Fail !  Cause:" + e);
		} finally {
			IOUtils.closeQuietly(connection);
		}
	}
	
	//########获取与Configuration绑定的相关对象#######################
	
	/**
	 * 获取MybatisContext (统一所有入口)
	 * @param configuration
	 * @return
	 */
	public static MybatisContext getBindContext(Configuration configuration) {
		if (configuration == null) {
			throw new MybatisException("Error: You need Initialize Mybatis Configuration !");
		}
		return getBindContext(configuration.toString());
	}
	
	/**
	 * 获取MybatisContext (统一所有入口)
	 * @param configMark
	 * @return
	 */
	public static MybatisContext getBindContext(String configMark) {
		MybatisContext cache = MYBATIS_GLOBAL_CONFIG.get(configMark);
		if (cache == null) {
			// 没有获取全局配置初始全局配置
			LOG.debug("DeBug: MyBatis Context Initializing !");
			MYBATIS_GLOBAL_CONFIG.put(configMark, DEFAULT);
			return DEFAULT;
		}
		return cache;
	}
	
	/**
	 * 设置元数据相关属性
	 * @param dataSource
	 * @param context
	 */
	public void setMetaData(Configuration configuration) {
		getBindContext(configuration).setMetaData(configuration.getEnvironment().getDataSource());
	}
	
	public static DatabaseType getDatabaseType(Configuration configuration) {
		return getBindContext(configuration).getDatabaseType();
	}

	public static PKStrategy getPKStrategy(Configuration configuration) {
		return getBindContext(configuration).getPKStrategy();
	}

	public static boolean isDbColumnUnderline(Configuration configuration) {
		return getBindContext(configuration).isDbColumnUnderline();
	}

	public static MapperSQLInjector getSQLInjector(Configuration configuration) {
		MybatisContext context = getBindContext(configuration);
		MapperSQLInjector sqlInjector = context.getSqlInjector();
		if (sqlInjector == null) {
			sqlInjector = new AnnotationMapperSQLInjector();
			context.setSqlInjector(sqlInjector);
		}
		return sqlInjector;
	}

	public static MetaObjectHandler getMetaObjectHandler(Configuration configuration) {
		return getBindContext(configuration).getMetaObjectHandler();
	}

	public static ColumnStrategy getColumnStrategy(Configuration configuration) {
		return getBindContext(configuration).getColumnStrategy();
	}

	public static boolean isRefresh(Configuration configuration) {
		return getBindContext(configuration).isRefresh();
	}

	public static boolean isAutoSetDbType(Configuration configuration) {
		return getBindContext(configuration).isAutoSetDbType();
	}

	public static Set<String> getMapperRegistryCache(Configuration configuration) {
		return getBindContext(configuration).getMapperRegistryCache();
	}

	public static String getIdentifierQuote(Configuration configuration) {
		return getBindContext(configuration).getIdentifierQuote();
	}

	public static SqlSession getSqlSession(Configuration configuration) {
		return getBindContext(configuration).getSqlSession();
	}

	public static SqlSession getSqlsessionBatch(Configuration configuration) {
		return getBindContext(configuration).getSqlsessionBatch();
	}
	
	/**
	 * 获取当前的SqlSessionFactory
	 * @param clazz
	 * @return
	 */
	public static SqlSessionFactory getSessionFactory(Class<?> clazz) {
		String configMark = TableMetadataUtils.getTableMetadata(clazz).getConfigMark();
		MybatisContext context = MybatisContext.getBindContext(configMark);
		return context.getSqlSessionFactory();
	}
	
}
