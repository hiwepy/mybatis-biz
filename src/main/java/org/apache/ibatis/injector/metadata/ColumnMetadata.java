package org.apache.ibatis.injector.metadata;

import org.apache.ibatis.jdbc.SqlReservedWords;
import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.session.enums.ColumnStrategy;
import org.apache.ibatis.utils.StringUtils;

/**
 * <p>
 * 数据库表字段反射信息
 * </p>
 */
public class ColumnMetadata {

	/**
	 * <p>
	 * 是否有存在字段名与属性名关联
	 * </p>
	 * true , false
	 */
	private boolean related = false;

	/**
	 * 字段名
	 */
	private String column;

	/**
	 * 属性名
	 */
	private String property;

	/**
	 * 属性表达式#{property}, 可以指定jdbcType, typeHandler等
	 */
	private String el;
	/**
	 * 属性类型
	 */
	private String propertyType;

	/**
	 * 字段策略【 默认，自判断 null 】
	 */
	private ColumnStrategy strategy = ColumnStrategy.NOT_NULL;

	/**
	 * <p>
	 * 存在 TableField 注解构造函数
	 * </p>
	 */
	public ColumnMetadata(MybatisContext context, String column, String property, String el,
			ColumnStrategy strategy, String propertyType) {
		if (context.isDbColumnUnderline()) {
			/* 开启字段下划线申明 */
			this.related = true;
		} else if (!column.equals(property)) {
			/* 没有开启下划线申明 但是column与property不等的情况下设置related为true */
			this.related = true;
		}
		this.setColumn(context, column);
		this.property = property;
		this.el = el;
		/*
		 * 优先使用单个字段注解，否则使用全局配置
		 */
		if (strategy != ColumnStrategy.NOT_NULL) {
			this.strategy = strategy;
		} else {
			this.strategy = context.getColumnStrategy();
		}
		this.propertyType = propertyType;
	}

	public ColumnMetadata(MybatisContext context, String column, String propertyType) {
		if (context.isDbColumnUnderline()) {
			/* 开启字段下划线申明 */
			this.related = true;
			this.setColumn(context, StringUtils.camelToUnderline(column));
		} else {
			this.setColumn(context, column);
		}
		this.property = column;
		this.el = column;
		this.strategy = context.getColumnStrategy();
		this.propertyType = propertyType;
	}

	public boolean isRelated() {
		return related;
	}

	public void setRelated(boolean related) {
		this.related = related;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(MybatisContext globalConfig, String column) {
		String temp = SqlReservedWords.convert(globalConfig, column);
		if (globalConfig.isCapitalMode() && !isRelated()) {
			// 全局大写，非注解指定
			temp = temp.toUpperCase();
		}
		this.column = temp;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getEl() {
		return el;
	}

	public void setEl(String el) {
		this.el = el;
	}

	public ColumnStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(ColumnStrategy ColumnStrategy) {
		this.strategy = ColumnStrategy;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}
}
