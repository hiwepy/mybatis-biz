package org.apache.ibatis.jdbc.wrap;

import org.apache.ibatis.utils.StringUtils;

/**
 * <p>
 * 条件查询构造器
 * </p>
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class ConditionWrapper extends Wrapper {

	/**
	 * 构建一个Empty条件构造 避免传递参数使用null
	 */
	public static ConditionWrapper Empty() {
		return ConditionWrapper.instance();
	}

	/**
	 * 获取实例
	 */
	public static ConditionWrapper instance() {
		return new ConditionWrapper();
	}

	/**
	 * SQL 片段
	 */
	@Override
	public String getSqlSegment() {
		/*
		 * 无条件
		 */
		String sqlWhere = transactSQL.toString();
		if (StringUtils.isEmpty(sqlWhere)) {
			return null;
		}
		return sqlWhere;
	}

}
