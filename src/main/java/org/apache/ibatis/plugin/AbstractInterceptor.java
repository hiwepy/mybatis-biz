/*
 * Copyright (c) 2018 (hnxyhcwdl1003@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.plugin;

import java.util.Properties;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.meta.MetaExecutor;
import org.apache.ibatis.plugin.meta.MetaParameterHandler;
import org.apache.ibatis.plugin.meta.MetaResultSetHandler;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.utils.MybatisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInterceptor implements Interceptor {

	protected static Logger LOG = LoggerFactory.getLogger(AbstractInterceptor.class);
	
	/**
	 * <pre>
	 *  统一调用抽象方法：使用metaExecutor可获取以下对象：
	 *  MetaObject metaObject = metaExecutor.getMetaObject();
	 * 	Transaction transaction = metaExecutor.getTransaction();
 	 * 	Configuration configuration = metaExecutor.getConfiguration();
	 * </pre>
	 */
	public abstract Object doExecutorIntercept(Invocation invocation,Executor executorProxy,MetaExecutor metaExecutor) throws Throwable;
	  
	/**
	 * <pre>
	 *  统一调用抽象方法：使用metaParameterHandler可获取以下对象：
	 *  MetaObject metaObject = metaParameterHandler.getMetaObject();
	 * 	Configuration configuration = metaParameterHandler.getConfiguration();
	 * 	TypeHandlerRegistry typeHandlerRegistry = metaParameterHandler.getTypeHandlerRegistry();
	 *  MappedStatement mappedStatement = metaParameterHandler.getMappedStatement();
	 * 	Object parameterObject = metaParameterHandler.getParameterObject();
	 * 	BoundSql boundSql = metaParameterHandler.getBoundSql();
	 * </pre>
	 */
	public abstract Object doParameterIntercept(Invocation invocation,ParameterHandler parameterHandler,MetaParameterHandler metaParameterHandler) throws Throwable;

	/**
	 * <pre>
	 *  统一调用抽象方法：期中metaStatementHandler可以使用如下方法获取对象：
	 *  MetaObject metaObject = metaStatementHandler.getMetaObject();
	 * 	Configuration configuration = metaStatementHandler.getConfiguration();
 	 * 	ObjectFactory objectFactory = metaStatementHandler.getObjectFactory();
  	 * 	TypeHandlerRegistry typeHandlerRegistry = metaStatementHandler.getTypeHandlerRegistry();
 	 * 	ResultSetHandler resultSetHandler = metaStatementHandler.getResultSetHandler();
  	 * 	ParameterHandler parameterHandler = metaStatementHandler.getParameterHandler();
 	 * 	Executor executor = metaStatementHandler.getExecutor();
 	 *  MappedStatement mappedStatement = metaStatementHandler.getMappedStatement();
  	 * 	RowBounds rowBounds = metaStatementHandler.getRowBounds();
  	 * 	BoundSql boundSql = metaStatementHandler.getBoundSql();
	 * </pre>
	 */
	public abstract Object doStatementIntercept(Invocation invocation,StatementHandler statementHandler,MetaStatementHandler metaStatementHandler) throws Throwable;
	
	/**
	 * <pre>
	 *  统一调用抽象方法：使用metaResultSetHandler可获取以下对象：
	 *  MetaObject metaObject = metaResultSetHandler.getMetaObject();
 	 * 	Executor executor = metaResultSetHandler.getExecutor();
 	 * 	Configuration configuration = metaResultSetHandler.getConfiguration();
 	 *  MappedStatement mappedStatement = metaResultSetHandler.getMappedStatement();
  	 * 	RowBounds rowBounds = metaResultSetHandler.getRowBounds();
  	 * 	ParameterHandler parameterHandler = metaResultSetHandler.getParameterHandler();
  	 * 	ResultHandler<?> resultHandler = metaResultSetHandler.getResultHandler();
  	 * 	BoundSql boundSql = metaResultSetHandler.getBoundSql();
  	 * 	TypeHandlerRegistry typeHandlerRegistry = metaResultSetHandler.getTypeHandlerRegistry();
  	 *  ObjectFactory objectFactory = metaResultSetHandler.getObjectFactory();
  	 *  ReflectorFactory reflectorFactory = metaResultSetHandler.getReflectorFactory();
	 * </pre>
	 */
	public abstract Object doResultSetIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) throws Throwable;
	
	/**
	 * 扩展拦截方法，用于所有拦截执行完成后的资源释放操作
	 * @param invocation
	 * @throws Throwable
	 */
	public abstract void doDestroyIntercept(Invocation invocation) throws Throwable;
	
	/**
	 * 
	 * @description	： （Executor，ParameterHandler，ResultSetHandler，StatementHandler）等对象
	 * <pre>
	 *	创建顺序是:Executor，StatementHandler，ParameterHandler，ResultSetHandler
	 *	执行顺序是:Executor，ParameterHandler，StatementHandler，ResultSetHandler
	 * </pre>
	 * @author 		：<a href="https://github.com/vindell">vindell</a>
	 * @date 		：2017年9月12日 下午11:35:29
	 * @param invocation
	 * @return
	 * @throws Throwable
	 * @see org.apache.ibatis.session.Configuration
	 */
	public Object intercept(Invocation invocation) throws Throwable {
		try {
			// 通过invocation获取代理的目标对象
			Object target = invocation.getTarget();
			// 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
			target = MybatisUtils.getTarget(target);
			
			if(target instanceof Executor){
				Executor executor = (Executor)target;
				// Executor的MetaObject处理
				MetaExecutor meta = MetaExecutor.metaObject(executor);
				// 调用抽象接口，执行子类的代码
				return this.doExecutorIntercept(invocation, executor, meta );
			}
			
			else if(target instanceof ParameterHandler){
			    // 是通过Interceptor的plugin方法进行包裹的，所以我们这里拦截到的目标对象肯定是ParameterHandler对象。
				ParameterHandler parameterHandler = (ParameterHandler) target;
				// ParameterHandler的MetaObject处理
				MetaParameterHandler meta = MetaParameterHandler.metaObject(parameterHandler);
				// 调用抽象接口，执行子类的代码
				return this.doParameterIntercept(invocation, parameterHandler, meta );
			}
			
			/*
			 * 对于StatementHandler其实只有两个实现类，
			 * 一个是RoutingStatementHandler，另一个是抽象类BaseStatementHandler，
			 * BaseStatementHandler有三个子类：
			 * 分别是SimpleStatementHandler，PreparedStatementHandler和CallableStatementHandler，
			 * SimpleStatementHandler是用于处理Statement的，
			 * PreparedStatementHandler是处理PreparedStatement的，
			 * CallableStatementHandler是 处理CallableStatement的。
			 * Mybatis在进行Sql语句处理的时候都是建立的RoutingStatementHandler，而在RoutingStatementHandler里面拥有一个   StatementHandler类型的delegate属性，
			 * RoutingStatementHandler会依据Statement的不同建立对应的BaseStatementHandler，即SimpleStatementHandler、  PreparedStatementHandler或CallableStatementHandler，
			 * 在RoutingStatementHandler里面所有StatementHandler接口方法的实现都是调用的delegate对应的方法。 
			 * 我们在PageInterceptor类上已经用@Signature标记了该Interceptor只拦截StatementHandler接口的prepare方法，又因为Mybatis只有在建立RoutingStatementHandler的时候  
			 * 是通过Interceptor的plugin方法进行包裹的，所以我们这里拦截到的目标对象肯定是RoutingStatementHandler对象。  
			*/
			else if(target instanceof StatementHandler){
			    // 是通过Interceptor的plugin方法进行包裹的，所以我们这里拦截到的目标对象肯定是RoutingStatementHandler对象。
				StatementHandler statementHandler = (StatementHandler) target;
				// StatementHandler的MetaObject处理
				MetaStatementHandler meta = MetaStatementHandler.metaObject(statementHandler);
				//调用抽象接口，执行子类的代码
				return this.doStatementIntercept(invocation, statementHandler, meta );
			}
			
			// 暂时ResultSetHandler只有FastResultSetHandler这一种实现
			else if (target instanceof ResultSetHandler) {
				// 获取ResultSetHandler的实现
				ResultSetHandler resultSetHandler = (ResultSetHandler) target;
				// ResultSetHandler的MetaObject处理
				MetaResultSetHandler meta = MetaResultSetHandler.metaObject(resultSetHandler);
				//调用抽象接口，执行子类的代码
				return this.doResultSetIntercept(invocation, resultSetHandler, meta );
			} 
			// 将执行权交给下一个拦截器  
			return invocation.proceed();
			
		} finally  {
			// 执行完成后的销毁操作
			this.doDestroyIntercept(invocation);
		}
	}
	
	public abstract void setInterceptProperties(Properties properties);
	
	public void setProperties(Properties properties) {
		//调用抽象接口，执行子类的代码
		this.setInterceptProperties(properties);
	}
	
	public SqlSource buildSqlSource(Configuration configuration, String originalSql, Class<?> parameterType) {
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        return  builder.parse(originalSql, parameterType, null);
    }
	
}
