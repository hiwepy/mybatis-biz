package org.apache.ibatis.jdbc.enums;

/**
 * <p>
 * 	SQL like 枚举
 * </p>
 * 
 * @author Caratacus
 * @Date 2016-12-4
 */
public enum SqlLike {
	/**
	 * LEFT
	 */
	LEFT("left", "左边%"),
	/**
	 * RIGHT
	 */
	RIGHT("right", "右边%"),
	/**
	 * CUSTOM
	 */
	CUSTOM("custom", "定制"),
	/**
	 * DEFAULT
	 */
	DEFAULT("default", "两边%");

	/** 主键 */
	private final String type;

	/** 描述 */
	private final String desc;

	SqlLike(final String type, final String desc) {
		this.type = type;
		this.desc = desc;
	}

	public String getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}

}
