package org.apache.ibatis.utils;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;

@SuppressWarnings("rawtypes")
public class MyBatisSQLUtils {

	/***
	 * 
	 * @description	：
	 * @param obj		: 参数对象
	 * @param isReplace	: 是否替换语句中的单引号 true：替换 false:不替换
	 * @return
	 */
	public static String getParameterValue(Object obj,boolean isReplace) {
		if(StringUtils.isEmpty(obj)){
        	return "";
        }
		String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        	//value = obj.toString();
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = formatter.format(new Date());
        } else {
            value = String.valueOf(obj);
        }
        return isReplace?value.replace("'", ""):value;
    }
	
	public static String getParameterValue(Object obj) {
		return MyBatisSQLUtils.getParameterValue(obj,false);
	}

	/**
	 * 
	 * @description	： 解析出执行的SQL
	 * @author 		： <a href="https://github.com/vindell">vindell</a>
	 * @date 		：2017年9月12日 下午11:34:03
	 * @param statement
	 * @param parameterObject
	 * @return
	 */
	public static String getRunSQL(MappedStatement statement,Object parameterObject) {
		return getRunSQL(statement,parameterObject,false);
	}

	/**
	 * 
	 * @description	： 解析出执行的SQL
	 * @author 		： <a href="https://github.com/vindell">vindell</a>
	 * @date 		：2017年9月12日 下午11:33:40
	 * @param statement
	 * @param parameterObject
	 * @param isReplace 是否替换语句中的单引号 true：替换 false:不替换
	 * @return
	 */
	public static String getRunSQL(MappedStatement statement,Object parameterObject,boolean isReplace) {
		BoundSql boundSql = statement.getBoundSql(parameterObject);
		// 获得预编译SQL
		String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
		// 获得参数
		Object[] parameters = getParameters(statement, parameterObject);
		if(parameters != null && parameters.length > 0){
			// 替换参数
			for (Object value : parameters) {
				sql = sql.replaceFirst("\\?", getParameterValue(value,isReplace));
			}
		}
		return sql;
	}
	
	
	/**
	 * 
	 * @description	：解析参数
	 * @param statement
	 * @param parameterObject
	 * @return
	 */
	public static Object[] getParameters(MappedStatement statement, Object parameterObject) {
		BoundSql boundSql = statement.getBoundSql(parameterObject);
		/* 解决 MyBatis 物理分页foreach 参数失效 */
		BoundSQLUtils.setBoundSql(boundSql, boundSql);
		Configuration configuration = statement.getConfiguration();
		// Object parameterObject = boundSql.getParameterObject();
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings.size() > 0 && parameterObject != null) {
			TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
			MetaObject metaObject = parameterObject == null ? null : configuration.newMetaObject(parameterObject);
			ParameterMapping parameterMapping = null;
			Object value = null;
			String propertyName = null;
			Object[] parameterArray = new Object[parameterMappings.size()];
			for (int i = 0; i < parameterMappings.size(); i++) {
				parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					propertyName = parameterMapping.getProperty();
					PropertyTokenizer prop = new PropertyTokenizer(propertyName);
					if (parameterObject == null) {
						value = null;
					} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
						value = parameterObject;
					} else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
						value = boundSql.getAdditionalParameter(prop.getName());
						if (value != null) {
							value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
						}
					} else {
						value = metaObject == null ? null : metaObject.getValue(propertyName);
					}
					parameterArray[i] = value;
				}
			}
			return parameterArray;
		}
		return null;
	}

	/** 
	 * @description	：运行期获取MyBatis执行的SQL及参数
	 * @param statementID : Mapper xml 文件里的select Id 
	 * @param parameterObject:  参数
	 * @param sqlSessionFactorys
	 * @param isReplace
	 * @return
	 */
	public static MyBatisSQL getMyBatisSQL(String statementID,Object parameterObject,Collection<SqlSessionFactory> sqlSessionFactorys,boolean isReplace) {
		SqlSessionFactory[] sqlSessionFactoryArray = new SqlSessionFactory[sqlSessionFactorys.size()];
		Iterator<SqlSessionFactory> iterator = sqlSessionFactorys.iterator();
		int index = 0;
		while (iterator.hasNext()) {
			SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) iterator.next();
			sqlSessionFactoryArray[index] = sqlSessionFactory;
			index ++;
		}
		return getMyBatisSQL(statementID, parameterObject,isReplace ,sqlSessionFactoryArray);
	}
	
	public static MyBatisSQL getMyBatisSQL(String statementID,Object parameterObject,boolean isReplace,SqlSessionFactory... sqlSessionFactorys) {
			
		MyBatisSQL ibatisSql = new MyBatisSQL();
		MappedStatement mappedStatement = null;
		for (int i = 0; i < sqlSessionFactorys.length; i++) {
			mappedStatement = sqlSessionFactorys[i].getConfiguration().getMappedStatement(statementID);
			if(mappedStatement != null){
				break;
			}
		}
		if(null != mappedStatement){
			
			BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
			
			// 获得预编译SQL
			String preSQL = boundSql.getSql().replaceAll("[\\s]+", " ");
			
			//设置预编译SQL
			ibatisSql.setPreSQL(preSQL);
			
			// 获得参数
			Object[] parameters = MyBatisSQLUtils.getParameters(mappedStatement, parameterObject);
			if(parameters != null && parameters.length > 0){
				String runSQL = preSQL;
				// 替换参数
				for (Object value : parameters) {
					runSQL = runSQL.replaceFirst("\\?", getParameterValue(value,isReplace));
				}
				//设置最终运行的SQL
				ibatisSql.setRunSQL(runSQL);
			}
			//System.out.println("runSQL:"+runSQL);
			//设置参数
			ibatisSql.setParameters(parameters);
		}
		return ibatisSql;
	}
}
