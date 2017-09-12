package org.apache.ibatis.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.annotation.Column;
import org.apache.ibatis.annotation.UID;
import org.apache.ibatis.annotation.Table;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.exception.MybatisException;
import org.apache.ibatis.injector.metadata.ColumnMetadata;
import org.apache.ibatis.injector.metadata.TableMetadata;
import org.apache.ibatis.jdbc.MybatisSqlRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.enums.ColumnStrategy;
import org.apache.ibatis.session.enums.PKStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @className	： TableMetadataUtils
 * @description	：实体类反射表辅助类
 * @author 		： <a href="mailto:hnxyhcwdl1003@163.com">wandalong</a>
 * @date		： Jan 26, 2016 3:12:35 PM
 * @version 	V1.0
 */
public class TableMetadataUtils {

	protected static final Logger LOG = LoggerFactory.getLogger(TableMetadataUtils.class);
	
	/**
	 * 缓存反射类表信息
	 */
	private static final Map<String, TableMetadata> tableInfoCache = new ConcurrentHashMap<String, TableMetadata>();
	/**
	 * 默认表主键
	 */
	private static final String DEFAULT_ID_NAME = "id";

	/**
	 * <p>
	 * 获取实体映射表信息
	 * <p>
	 *
	 * @param clazz
	 *            反射实体类
	 * @return
	 */
	public static TableMetadata getTableMetadata(Class<?> clazz) {
		return tableInfoCache.get(clazz.getName());
	}

	/**
	 * <p>
	 * 实体类反射获取表信息【初始化】
	 * <p>
	 *
	 * @param clazz
	 *            反射实体类
	 * @return
	 */
	public synchronized static TableMetadata initTableMetadata(MapperBuilderAssistant builderAssistant, Class<?> clazz) {
		TableMetadata ti = tableInfoCache.get(clazz.getName());
		if (ti != null) {
			return ti;
		}
		TableMetadata tableInfo = new TableMetadata();
		MybatisContext context = null;
		if (null != builderAssistant) {
			tableInfo.setCurrentNamespace(builderAssistant.getCurrentNamespace());
			tableInfo.setConfigMark(builderAssistant.getConfiguration());
			context = MybatisContext.getBindContext(builderAssistant.getConfiguration());
		} else {
			// 兼容测试场景
			context = MybatisContext.DEFAULT;
		}
		/* 表名 */
		Table table = clazz.getAnnotation(Table.class);
		String tableName = clazz.getSimpleName();
		if (table != null && StringUtils.isNotEmpty(table.value())) {
			tableName = table.value();
		} else {
			// 开启字段下划线申明
			if (context.isDbColumnUnderline()) {
				tableName = StringUtils.camelToUnderline(tableName);
			}
			// 大写命名判断
			if (context.isCapitalMode()) {
				tableName = tableName.toUpperCase();
			} else {
				// 首字母小写
				tableName = StringUtils.firstToLowerCase(tableName);
			}
		}
		tableInfo.setTableName(tableName);
		/* 表结果集映射 */
		if (table != null && StringUtils.isNotEmpty(table.resultMap())) {
			tableInfo.setResultMap(table.resultMap());
		}
		List<ColumnMetadata> fieldList = new ArrayList<ColumnMetadata>();
		List<Field> list = getAllFields(clazz);
		boolean existTableId = existTableId(list);
		for (Field field : list) {

			/**
			 * 主键ID 初始化
			 */
			if (existTableId) {
				if (initTableId(context, tableInfo, field, clazz)) {
					continue;
				}
			} else if (initFieldId(context, tableInfo, field, clazz)) {
				continue;
			}

			/**
			 * 字段初始化
			 */
			if (initTableField(context, fieldList, field, clazz)) {
				continue;
			}

			/**
			 * 字段, 使用 camelToUnderline 转换驼峰写法为下划线分割法, 如果已指定 TableField , 便不会执行这里
			 */
			fieldList.add(new ColumnMetadata(context, field.getName(), field.getType().getName()));
		}

		/* 字段列表 */
		tableInfo.setFieldList(fieldList);
		/*
		 * 未发现主键注解，提示警告信息
		 */
		if (StringUtils.isEmpty(tableInfo.getKeyColumn())) {
			LOG.warn(String.format("Warn: Could not find @TableId in Class: %s.", clazz.getName()));
		}
		/*
		 * 注入
		 */
		tableInfoCache.put(clazz.getName(), tableInfo);
		return tableInfo;
	}

	/**
	 * <p>
	 * 判断主键注解是否存在
	 * </p>
	 *
	 * @param list
	 *            字段列表
	 * @return
	 */
	public static boolean existTableId(List<Field> list) {
		boolean exist = false;
		for (Field field : list) {
			UID tableId = field.getAnnotation(UID.class);
			if (tableId != null) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	/**
	 * <p>
	 * 主键属性初始化
	 * </p>
	 *
	 * @param tableInfo
	 * @param field
	 * @param clazz
	 * @return true 继续下一个属性判断，返回 continue;
	 */
	private static boolean initTableId(MybatisContext context, TableMetadata tableInfo, Field field, Class<?> clazz) {
		UID tableId = field.getAnnotation(UID.class);
		if (tableId != null) {
			if (StringUtils.isEmpty(tableInfo.getKeyColumn())) {
				/*
				 * 主键策略（ 注解 > 全局 > 默认 ）
				 */
				if (PKStrategy.INPUT != tableId.type()) {
					tableInfo.setPkStrategy(tableId.type());
				} else {
					tableInfo.setPkStrategy(context.getPKStrategy());
				}
				/* 字段 */
				String column = field.getName();
				if (StringUtils.isNotEmpty(tableId.value())) {
					column = tableId.value();
					tableInfo.setKeyRelated(true);
				} else {
					// 开启字段下划线申明
					if (context.isDbColumnUnderline()) {
						column = StringUtils.camelToUnderline(column);
					}
					// 全局大写命名
					if (context.isCapitalMode()) {
						column = column.toUpperCase();
					}
				}
				tableInfo.setKeyColumn(column);
				tableInfo.setKeyProperty(field.getName());
				return true;
			} else {
				throwExceptionId(clazz);
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 主键属性初始化
	 * </p>
	 *
	 * @param tableInfo
	 * @param field
	 * @param clazz
	 * @return true 继续下一个属性判断，返回 continue;
	 */
	private static boolean initFieldId(MybatisContext context, TableMetadata tableInfo, Field field, Class<?> clazz) {
		String column = field.getName();
		if (context.isCapitalMode()) {
			column = column.toUpperCase();
		}
		if (DEFAULT_ID_NAME.equalsIgnoreCase(column)) {
			if (StringUtils.isEmpty(tableInfo.getKeyColumn())) {
				tableInfo.setPkStrategy(context.getPKStrategy());
				tableInfo.setKeyColumn(column);
				tableInfo.setKeyProperty(field.getName());
				return true;
			} else {
				throwExceptionId(clazz);
			}
		}
		return false;
	}

	/**
	 * <p>
	 * 发现设置多个主键注解抛出异常
	 * </p>
	 */
	private static void throwExceptionId(Class<?> clazz) {
		StringBuffer errorMsg = new StringBuffer();
		errorMsg.append("There must be only one, Discover multiple @TableId annotation in ");
		errorMsg.append(clazz.getName());
		throw new MybatisException(errorMsg.toString());
	}

	/**
	 * <p>
	 * 字段属性初始化
	 * </p>
	 *
	 * @param fieldList
	 * @param clazz
	 * @return true 继续下一个属性判断，返回 continue;
	 */
	private static boolean initTableField(MybatisContext context, List<ColumnMetadata> fieldList, Field field, Class<?> clazz) {
		/* 获取注解属性，自定义字段 */
		Column tableField = field.getAnnotation(Column.class);
		if (tableField != null) {
			String columnName = field.getName();
			if (StringUtils.isNotEmpty(tableField.value())) {
				columnName = tableField.value();
			}

			Class<?> fieldType = field.getType();
			ColumnStrategy validate = tableField.validate();
			/* 字符串类型默认 FieldStrategy.NOT_EMPTY */
			if (String.class.isAssignableFrom(fieldType) && ColumnStrategy.NOT_NULL.equals(validate)) {
				validate = ColumnStrategy.NOT_EMPTY;
			}

			/*
			 * el 语法支持，可以传入多个参数以逗号分开
			 */
			String el = field.getName();
			if (StringUtils.isNotEmpty(tableField.el())) {
				el = tableField.el();
			}
			String[] columns = columnName.split(";");
			String[] els = el.split(";");
			if (null != columns && null != els && columns.length == els.length) {
				for (int i = 0; i < columns.length; i++) {
					fieldList.add(new ColumnMetadata(context, columns[i], field.getName(), els[i], validate, field.getType().getName()));
				}
			} else {
				String errorMsg = "Class: %s, Field: %s, 'value' 'el' Length must be consistent.";
				throw new MybatisException(String.format(errorMsg, clazz.getName(), field.getName()));
			}

			return true;
		}

		return false;
	}

	/**
	 * 获取该类的所有属性列表
	 *
	 * @param clazz
	 *            反射类
	 * @return
	 */
	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fieldList = ReflectionUtils.getFieldList(clazz);
		if (CollectionUtils.isNotEmpty(fieldList)) {
			Iterator<Field> iterator = fieldList.iterator();
			while (iterator.hasNext()) {
				Field field = iterator.next();
				/* 过滤注解非表字段属性 */
				Column tableField = field.getAnnotation(Column.class);
				if (tableField != null && !tableField.exist()) {
					iterator.remove();
				}
			}
		}
		return fieldList;
	}

	/**
	 * 初始化SqlSessionFactory (供Mybatis原生调用)
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	public static void initSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		Configuration configuration = sqlSessionFactory.getConfiguration();
		MybatisContext context = MybatisContext.getBindContext(configuration);
		// SqlRunner
		MybatisSqlRunner.FACTORY = sqlSessionFactory;
		if (context == null) {
			MybatisContext defaults = MybatisContext.defaults();
			defaults.setSqlSessionFactory(sqlSessionFactory);
			MybatisContext.bindContext(configuration, defaults);
		} else {
			context.setSqlSessionFactory(sqlSessionFactory);
		}
	}

}
