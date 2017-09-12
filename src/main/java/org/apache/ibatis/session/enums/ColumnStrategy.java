package org.apache.ibatis.session.enums;

/**
 * <p>
 * 字段策略枚举类
 * </p>
 */
public enum ColumnStrategy {
	
	IGNORED(0, "ignored"), 
	
	NOT_NULL(1, "not null"), 
	
	NOT_EMPTY(2, "not empty");

	/** 主键 */
	private final int key;

	/** 描述 */
	private final String desc;

	ColumnStrategy(final int key, final String desc) {
		this.key = key;
		this.desc = desc;
	}

	public int getKey() {
		return this.key;
	}

	public String getDesc() {
		return this.desc;
	}

	public static ColumnStrategy getFieldStrategy(int key) {
		ColumnStrategy[] fss = ColumnStrategy.values();
		for (ColumnStrategy fs : fss) {
			if (fs.getKey() == key) {
				return fs;
			}
		}
		return ColumnStrategy.NOT_NULL;
	}

}
