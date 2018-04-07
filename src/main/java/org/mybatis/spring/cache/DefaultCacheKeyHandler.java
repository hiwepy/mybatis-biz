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
package org.mybatis.spring.cache;

import java.util.List;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.utils.MetaObjectUtils;
import org.apache.ibatis.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCacheKeyHandler implements CacheKeyHandler {
	
	protected static Logger LOG = LoggerFactory.getLogger(DefaultCacheKeyHandler.class);
	
	public CacheKey createCacheKey(MappedStatement mappedStatement,Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
		Configuration configuration = mappedStatement.getConfiguration();
		CacheKey cacheKey = new CacheKey();
		cacheKey.update(mappedStatement.getId());
		cacheKey.update(rowBounds.getOffset());
		cacheKey.update(rowBounds.getLimit());
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		
		// 解决自动生成SQL，SQL语句为空导致key生成错误的bug
		if ( /*mappedStatement.getId().matches(getPaginationID()) && */StringUtils.isEmpty(boundSql.getSql())) {
			String newSql = null;
			try {
				if(SqlCommandType.SELECT.ordinal() == mappedStatement.getSqlCommandType().ordinal()){
					//newSql = builder.buildQuerySQL(parameterObject);
				}
				SqlSource sqlSource = buildSqlSource(configuration, newSql,parameterObject.getClass());
				parameterMappings = sqlSource.getBoundSql(parameterObject).getParameterMappings();
				cacheKey.update(newSql);
			} catch (Exception e) {
				LOG.error("Update cacheKey error.", e);
			}
		}else {
			cacheKey.update(boundSql.getSql());
		}

		MetaObject metaObject = MetaObjectUtils.forObject(parameterObject);
		if (parameterMappings.size() > 0 && parameterObject != null) {
			TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
			if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
				cacheKey.update(parameterObject);
			} else {
				for (ParameterMapping parameterMapping : parameterMappings) {
					String propertyName = parameterMapping.getProperty();
					if (metaObject.hasGetter(propertyName)) {
						cacheKey.update(metaObject.getValue(propertyName));
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						cacheKey.update(boundSql.getAdditionalParameter(propertyName));
					}
				}
			}
		}
		return cacheKey;
	}

	public SqlSource buildSqlSource(Configuration configuration, String originalSql, Class<?> parameterType) {
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        return  builder.parse(originalSql, parameterType, null);
    }
	
}
