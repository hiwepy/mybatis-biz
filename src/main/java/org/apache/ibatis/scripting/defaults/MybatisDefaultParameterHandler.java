/*
 * Copyright (c) 2010-2020, vindell (hnxyhcwdl1003@163.com).
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
package org.apache.ibatis.scripting.defaults;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.injector.metadata.TableMetadata;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.meta.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.session.enums.PKStrategy;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.utils.IDWorker;
import org.apache.ibatis.utils.MapUtils;
import org.apache.ibatis.utils.StringUtils;
import org.apache.ibatis.utils.TableMetadataUtils;

/**
 * <p>
 * 自定义 ParameterHandler 重装构造函数，填充插入方法主键 ID
 * </p>
 */
public class MybatisDefaultParameterHandler extends DefaultParameterHandler {

	/**
	 * @see org.apache.ibatis.mapping.BoundSql
	 */
	private static Field additionalParametersField;
	private final TypeHandlerRegistry typeHandlerRegistry;
	private final MappedStatement mappedStatement;
	private final Object parameterObject;
	private BoundSql boundSql;
	private Configuration configuration;
	static {
		try {
			additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
			additionalParametersField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			// ignored, Because it will never happen.
		}
	}

	public MybatisDefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
		super(mappedStatement, processBatch(mappedStatement, parameterObject), boundSql);
		this.mappedStatement = mappedStatement;
		this.configuration = mappedStatement.getConfiguration();
		this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		this.parameterObject = parameterObject;
		this.boundSql = boundSql;
	}

	/**
	 * <p>
	 * 批量（填充主键 ID）
	 * </p>
	 * 
	 * @param ms
	 * @param parameterObject 插入数据库对象
	 * @return
	 */
	protected static Object processBatch(MappedStatement ms, Object parameterObject) {
		if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
			/**
			 * 只处理插入操作
			 */
			Collection<Object> parameters = getParameters(parameterObject);
			if (null != parameters) {
				List<Object> objList = new ArrayList<Object>();
				for (Object parameter : parameters) {
					TableMetadata tableInfo = TableMetadataUtils.getTableMetadata(parameter.getClass());
					if (null != tableInfo) {
						objList.add(populateKeys(tableInfo, ms, parameter));
					} else {
						/*
						 * 非表映射类不处理
						 */
						objList.add(parameter);
					}
				}
				return objList;
			} else {
				TableMetadata tableInfo = TableMetadataUtils.getTableMetadata(parameterObject.getClass());
				return populateKeys(tableInfo, ms, parameterObject);
			}
		}
		return parameterObject;
	}

	/**
	 * <p>
	 * 处理正常批量插入逻辑
	 * </p>
	 * <p>
	 * org.apache.ibatis.session.defaults.DefaultSqlSession$StrictMap 该类方法
	 * wrapCollection 实现 StrictMap 封装逻辑
	 * </p>
	 * 
	 * @param parameter
	 *            插入数据库对象
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static Collection<Object> getParameters(Object parameter) {
		Collection<Object> parameters = null;
		if (parameter instanceof Collection) {
			parameters = (Collection) parameter;
		} else if (parameter instanceof Map) {
			Map parameterMap = (Map) parameter;
			if (parameterMap.containsKey("collection")) {
				parameters = (Collection) parameterMap.get("collection");
			} else if (parameterMap.containsKey("list")) {
				parameters = (List) parameterMap.get("list");
			} else if (parameterMap.containsKey("array")) {
				parameters = Arrays.asList((Object[]) parameterMap.get("array"));
			}
		}
		return parameters;
	}

	/**
	 * <p>
	 * 填充主键 ID
	 * </p>
	 * 
	 * @param metadata
	 * @param ms
	 * @param parameterObject
	 *            插入数据库对象
	 * @return
	 */
	protected static Object populateKeys(TableMetadata metadata, MappedStatement ms, Object parameterObject) {
		if (null != metadata && StringUtils.isNotEmpty(metadata.getKeyProperty()) && null != metadata.getPkStrategy()
				&& metadata.getPkStrategy().getKey() >= 2) {
			MetaObject metaObject = ms.getConfiguration().newMetaObject(parameterObject);
			Object idValue = metaObject.getValue(metadata.getKeyProperty());
			/* 自定义 ID */
			if (StringUtils.checkValNull(idValue)) {
				if (metadata.getPkStrategy() == PKStrategy.ID_WORKER) {
					metaObject.setValue(metadata.getKeyProperty(), IDWorker.getId());
				} else if (metadata.getPkStrategy() == PKStrategy.UUID) {
					metaObject.setValue(metadata.getKeyProperty(), IDWorker.get32UUID());
				}
			}
			/* 自定义元对象填充控制器 */
			MetaObjectHandler metaObjectHandler = MybatisContext.getMetaObjectHandler(ms.getConfiguration());
			if (null != metaObjectHandler) {
				metaObjectHandler.insertFill(metaObject);
			}
			return metaObject.getOriginalObject();
		}
		/*
		 * 不处理
		 */
		return parameterObject;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setParameters(PreparedStatement ps) {
		// 反射获取动态参数
		Map<String, Object> additionalParameters = null;
		try {
			additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
		} catch (IllegalAccessException e) {
			// ignored, Because it will never happen.
		}
		ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			for (int i = 0; i < parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					Object value;
					String propertyName = parameterMapping.getProperty();
					if (boundSql.hasAdditionalParameter(propertyName)) { // issue
																			// #448
																			// ask
																			// first
																			// for
																			// additional
																			// params
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else {
						MetaObject metaObject = configuration.newMetaObject(parameterObject);
						value = metaObject.getValue(propertyName);
						if (value == null && MapUtils.isNotEmpty(additionalParameters)) {
							// issue #138
							value = additionalParameters.get(propertyName);
						}
					}
					TypeHandler typeHandler = parameterMapping.getTypeHandler();
					JdbcType jdbcType = parameterMapping.getJdbcType();
					if (value == null && jdbcType == null) {
						jdbcType = configuration.getJdbcTypeForNull();
					}
					try {
						typeHandler.setParameter(ps, i + 1, value, jdbcType);
					} catch (TypeException e) {
						throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
					} catch (SQLException e) {
						throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
					}
				}
			}
		}
	}
}
