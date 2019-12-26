package org.apache.ibatis.plugin.meta;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StringUtils;

public class MetaParameterHandler {

	protected MetaObject metaObject;
	protected Configuration configuration;
	protected TypeHandlerRegistry typeHandlerRegistry;
	protected MappedStatement mappedStatement;
	protected MapperProxyFactory<?> mapperProxy;
	protected MapperMethod mapperMethod;
	protected Method method;
	protected Object parameterObject;
	protected BoundSql boundSql;
	
	protected MetaParameterHandler(MetaObject metaObject, Configuration configuration,
			TypeHandlerRegistry typeHandlerRegistry,
			MappedStatement mappedStatement,
			MapperProxyFactory<?> mapperProxy,
			MapperMethod mapperMethod,
			Method method,
			Object parameterObject,
			BoundSql boundSql) {
		this.metaObject = metaObject;
		this.configuration = configuration;
		this.typeHandlerRegistry = typeHandlerRegistry;
		this.mappedStatement = mappedStatement;
		this.parameterObject = parameterObject;
		this.boundSql = boundSql;
	}
	
	public static MetaParameterHandler metaObject(ParameterHandler parameterHandler) {
		MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
		TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("typeHandlerRegistry");
		MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
		Object parameterObject = (Object) metaObject.getValue("parameterObject");
		BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
		Configuration configuration = (Configuration) metaObject.getValue("configuration");
		
		// 
		MapperRegistry mapperRegistry = configuration.getMapperRegistry();
		Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
			return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
		}).findFirst();
		MetaObject metaRegistry = SystemMetaObject.forObject(mapperRegistry);
		
		@SuppressWarnings("unchecked")
		Map<Class<?>, MapperProxyFactory<?>> knownMappers = (Map<Class<?>, MapperProxyFactory<?>>) metaRegistry.getValue("knownMappers");
		MapperProxyFactory<?> mapperProxy = knownMappers.get(firstMapper.get());
		
		Entry<Method, MapperMethod> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
			Method method = entry.getKey();
			String statement = mapperProxy.getMapperInterface().getName() + "." + method.getName();
			return mappedStatement.getId().equalsIgnoreCase(statement);
		}).findFirst().get();
	    return new MetaParameterHandler(metaObject, configuration, typeHandlerRegistry, mappedStatement, 
	    		mapperProxy, mapperProxyEntry.getValue(), mapperProxyEntry.getKey(), parameterObject, boundSql);
	}

	public MetaObject getMetaObject() {
		return metaObject;
	}

	public void setMetaObject(MetaObject metaObject) {
		this.metaObject = metaObject;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public TypeHandlerRegistry getTypeHandlerRegistry() {
		return typeHandlerRegistry;
	}

	public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
		this.typeHandlerRegistry = typeHandlerRegistry;
	}

	public MappedStatement getMappedStatement() {
		return mappedStatement;
	}

	public void setMappedStatement(MappedStatement mappedStatement) {
		this.mappedStatement = mappedStatement;
	}

	public void setMapperProxy(MapperProxyFactory<?> mapperProxy) {
		this.mapperProxy = mapperProxy;
	}

	public MapperMethod getMapperMethod() {
		return mapperMethod;
	}

	public void setMapperMethod(MapperMethod mapperMethod) {
		this.mapperMethod = mapperMethod;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
	public Object getParameterObject() {
		return parameterObject;
	}

	public void setParameterObject(Object parameterObject) {
		this.parameterObject = parameterObject;
	}

	public BoundSql getBoundSql() {
		return boundSql;
	}

	public void setBoundSql(BoundSql boundSql) {
		this.boundSql = boundSql;
	}
	
}
