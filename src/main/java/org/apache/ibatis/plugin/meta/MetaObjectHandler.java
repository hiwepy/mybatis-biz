package org.apache.ibatis.plugin.meta;

import org.apache.ibatis.reflection.MetaObject;

/**
 * <p>
 * 元对象字段填充控制器抽象类，实现公共字段自动写入
 * </p>
 */
public interface MetaObjectHandler {

	/**
	 * <p>
	 * 插入元对象字段填充
	 * </p>
	 * @param metaObject 元对象
	 * @return
	 */
	void insertFill(MetaObject metaObject);

}
