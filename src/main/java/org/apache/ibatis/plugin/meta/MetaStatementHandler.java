package org.apache.ibatis.plugin.meta;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.utils.MetaObjectUtils;
import org.springframework.util.StringUtils;

public class MetaStatementHandler {

	protected MetaObject metaObject;
	protected Configuration configuration;
	protected ObjectFactory objectFactory;
	protected TypeHandlerRegistry typeHandlerRegistry;
	protected ResultSetHandler resultSetHandler;
	protected ParameterHandler parameterHandler;
	protected Executor executor;
	protected MappedStatement mappedStatement;
	protected MapperProxyFactory<?> mapperProxy;
	protected MapperMethod mapperMethod;
	protected Method method;
	protected RowBounds rowBounds;
	protected BoundSql boundSql;
	
	public MetaStatementHandler(MetaObject metaObject, Configuration configuration,
			ObjectFactory objectFactory,
			TypeHandlerRegistry typeHandlerRegistry,
			ResultSetHandler resultSetHandler,
			ParameterHandler parameterHandler, 
			Executor executor,
			MappedStatement mappedStatement, 
			MapperProxyFactory<?> mapperProxy,
			MapperMethod mapperMethod,
			Method method,
			RowBounds rowBounds,
			BoundSql boundSql) {
		this.metaObject = metaObject;
		this.configuration = configuration;
		this.objectFactory = objectFactory;
		this.typeHandlerRegistry = typeHandlerRegistry;
		this.resultSetHandler = resultSetHandler;
		this.parameterHandler = parameterHandler;
		this.executor = executor;
		this.mappedStatement = mappedStatement;
		this.rowBounds = rowBounds;
		this.boundSql = boundSql;
	}

	public static MetaStatementHandler metaObject(StatementHandler statementHandler) {
		MetaObject metaObject = MetaObjectUtils.forObject(statementHandler);
		if(statementHandler instanceof RoutingStatementHandler){
			Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
			ObjectFactory objectFactory = (ObjectFactory) metaObject.getValue("delegate.objectFactory");
			TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("delegate.typeHandlerRegistry");
			ResultSetHandler resultSetHandler = (ResultSetHandler) metaObject.getValue("delegate.resultSetHandler");
			ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
			Executor executor = (Executor) metaObject.getValue("delegate.executor");
			MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
			RowBounds rowBounds = (RowBounds) metaObject.getValue("delegate.rowBounds");
			BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
			
			// 
			MapperRegistry mapperRegistry = configuration.getMapperRegistry();
			Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
				return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
			}).findFirst();
			MetaObject metaRegistry = MetaObjectUtils.forObject(mapperRegistry);
			
			@SuppressWarnings("unchecked")
			Map<Class<?>, MapperProxyFactory<?>> knownMappers = (Map<Class<?>, MapperProxyFactory<?>>) metaRegistry.getValue("knownMappers");
			MapperProxyFactory<?> mapperProxy = knownMappers.get(firstMapper.get());
			
			Entry<Method, MapperMethod> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
				Method method = entry.getKey();
				String statement = mapperProxy.getMapperInterface().getName() + "." + method.getName();
				return mappedStatement.getId().equalsIgnoreCase(statement);
			}).findFirst().get();
			
			return new MetaStatementHandler(metaObject, configuration, objectFactory, typeHandlerRegistry, resultSetHandler, 
					parameterHandler, executor, mappedStatement, mapperProxy, mapperProxyEntry.getValue(), mapperProxyEntry.getKey(), rowBounds, boundSql);
		}else {
			Configuration configuration = (Configuration) metaObject.getValue("configuration");
			ObjectFactory objectFactory = (ObjectFactory) metaObject.getValue("objectFactory");
			TypeHandlerRegistry typeHandlerRegistry = (TypeHandlerRegistry) metaObject.getValue("typeHandlerRegistry");
			ResultSetHandler resultSetHandler = (ResultSetHandler) metaObject.getValue("resultSetHandler");
			ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("parameterHandler");
			Executor executor = (Executor) metaObject.getValue("executor");
			MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
			RowBounds rowBounds = (RowBounds) metaObject.getValue("rowBounds");
			BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
			
			// 
			MapperRegistry mapperRegistry = configuration.getMapperRegistry();
			Optional<Class<?>> firstMapper  = mapperRegistry.getMappers().stream().filter(mapper -> {
				return StringUtils.startsWithIgnoreCase(mappedStatement.getId(), mapper.getName());
			}).findFirst();
			MetaObject metaRegistry = MetaObjectUtils.forObject(mapperRegistry);
			
			@SuppressWarnings("unchecked")
			Map<Class<?>, MapperProxyFactory<?>> knownMappers = (Map<Class<?>, MapperProxyFactory<?>>) metaRegistry.getValue("knownMappers");
			MapperProxyFactory<?> mapperProxy = knownMappers.get(firstMapper.get());
			
			Entry<Method, MapperMethod> mapperProxyEntry = mapperProxy.getMethodCache().entrySet().stream().filter(entry -> {
				Method method = entry.getKey();
				String statement = mapperProxy.getMapperInterface().getName() + "." + method.getName();
				return mappedStatement.getId().equalsIgnoreCase(statement);
			}).findFirst().get();
			
			return new MetaStatementHandler(metaObject, configuration, objectFactory, typeHandlerRegistry, resultSetHandler, 
					parameterHandler, executor, mappedStatement, mapperProxy, mapperProxyEntry.getValue(), mapperProxyEntry.getKey(), rowBounds, boundSql);
		}
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

	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}

	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public TypeHandlerRegistry getTypeHandlerRegistry() {
		return typeHandlerRegistry;
	}

	public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
		this.typeHandlerRegistry = typeHandlerRegistry;
	}

	public ResultSetHandler getResultSetHandler() {
		return resultSetHandler;
	}

	public void setResultSetHandler(ResultSetHandler resultSetHandler) {
		this.resultSetHandler = resultSetHandler;
	}

	public ParameterHandler getParameterHandler() {
		return parameterHandler;
	}

	public void setParameterHandler(ParameterHandler parameterHandler) {
		this.parameterHandler = parameterHandler;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public MappedStatement getMappedStatement() {
		return mappedStatement;
	}

	public void setMappedStatement(MappedStatement mappedStatement) {
		this.mappedStatement = mappedStatement;
	}
	
	public MapperProxyFactory<?> getMapperProxy() {
		return mapperProxy;
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

	public RowBounds getRowBounds() {
		return rowBounds;
	}

	public void setRowBounds(RowBounds rowBounds) {
		this.rowBounds = rowBounds;
	}

	public BoundSql getBoundSql() {
		return boundSql;
	}

	public void setBoundSql(BoundSql boundSql) {
		this.boundSql = boundSql;
	}
	
}
