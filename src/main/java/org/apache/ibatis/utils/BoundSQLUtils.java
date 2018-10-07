/*
 * Copyright (c) 2018, vindell (https://github.com/vindell).
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
package org.apache.ibatis.utils;

import java.lang.reflect.Field;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;


/**
 * @className	： BoundSqlUtils
 * @description	： BoundSql对象操作工具
 * @version 	V1.0 
 */
public class BoundSQLUtils {
	
	/**
	 * 
	 * @description	：解决 MyBatis 物理分页foreach 参数失效 
	 * <pre>
	 * 	场景：MyBatis 物理分页，查询条件中需要用到foreach ，参数失效，查不到结果
	 * 	分析：把java.sql的debug打开，sql语句正常，参数也正常。debug物理分页代码，setParameters时，
	 * 	   boundSql.getAdditionalParameter(propertyName)获取值始终是null，没有拿到参数。但是BoundSql的metaParameters中可以看到相关的参数值。
	 * 	解决方法：
	 * 		BoundSql countBS = new BoundSql(configuration, sql, boundSql.getParameterMappings(), parameterObject);
     *      Field metaParamsField = ReflectUtil.getFieldByFieldName(boundSql, "metaParameters");
     *      if (metaParamsField != null) {
     *           MetaObject mo = (MetaObject) ReflectUtil.getValueByFieldName(boundSql, "metaParameters");
     *           ReflectUtil.setValueByFieldName(countBS, "metaParameters", mo);
     *      }
     *      setParameters(prepStat, configuration, countBS, parameterObject);
     * </pre>
	 * @param sourceBoundSql
	 * @param targetBoundSql
	 */
	public static void setBoundSql(BoundSql sourceBoundSql,BoundSql targetBoundSql){
		Field metaParamsField = ReflectionUtils.getAccessibleField(sourceBoundSql, "metaParameters");
		if (metaParamsField != null) {
	       try {
				MetaObject metaParameters = (MetaObject) ReflectionUtils.getField("metaParameters",sourceBoundSql);
				ReflectionUtils.setField("metaParameters", targetBoundSql, metaParameters);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
