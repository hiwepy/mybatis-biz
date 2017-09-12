package org.apache.ibatis.injector;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

/**
 * <p>
 * SQL 自动注入器接口
 * </p>
 */
public interface MapperSQLInjector {

	/**
	 * 根据mapperClass注入SQL
	 * 
	 * @param builderAssistant
	 * @param mapperClass
	 */
	void inject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass);

	/**
	 * 检查SQL是否注入(已经注入过不再注入)
	 * 
	 * @param builderAssistant
	 * @param mapperClass
	 */
	void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass);

	/**
	 * 注入SqlRunner相关
	 * 
	 * @param configuration
	 * @see org.apache.ibatis.jdbc.MybatisSqlRunner
	 */
	void injectSQLRunner(Configuration configuration);

}
