/*
 * Copyright (c) 2010-2020, vindell (hnxyhcwdl1003@163.com).
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

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.meta.MetaExecutor;
import org.apache.ibatis.plugin.meta.MetaParameterHandler;
import org.apache.ibatis.plugin.meta.MetaResultSetHandler;
import org.apache.ibatis.plugin.meta.MetaStatementHandler;

/**
 * 
 * *******************************************************************
 * @className	： AbstractInterceptorAdapter
 * @description	：  Mybatis拦截器插件适配器: 执行顺序是: doExecutorIntercept，doParameterIntercept，doStatementIntercept，doResultSetIntercept
 * @author 		： <a href="mailto:hnxyhcwdl1003@163.com">wandalong</a>
 * @date		： 2017年5月11日 下午8:58:10
 * @version 	V1.0 
 * *******************************************************************
 */
public abstract class AbstractInterceptorAdapter extends AbstractInterceptor {

	protected boolean isRequireIntercept(Invocation invocation,Executor executorProxy, MetaExecutor metaExecutor) {
		return true;
	}
	
	protected boolean isRequireIntercept(Invocation invocation, ParameterHandler parameterHandler, MetaParameterHandler metaParameterHandler) {
		return true;
	}
	
	protected boolean isRequireIntercept(Invocation invocation,StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) {
		return true;
	}
	
	protected boolean isRequireIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) {
		return true;
	}
	
	@Override
	public Object doExecutorIntercept(Invocation invocation,Executor executorProxy, MetaExecutor metaExecutor) throws Throwable {
		if (isRequireIntercept(invocation, executorProxy, metaExecutor)) {
			//do some things
		}
		return invocation.proceed();
	}
	
	@Override
	public Object doParameterIntercept(Invocation invocation, ParameterHandler parameterHandler, MetaParameterHandler metaParameterHandler) throws Throwable {
		if (isRequireIntercept(invocation, parameterHandler, metaParameterHandler)) {
			//do some things
		}
		return invocation.proceed();
	}
	
	@Override
	public Object doStatementIntercept(Invocation invocation,StatementHandler statementHandler, MetaStatementHandler metaStatementHandler) throws Throwable {
		if (isRequireIntercept(invocation, statementHandler, metaStatementHandler)) {
			//do some things
		}
		return invocation.proceed();
	}

	@Override
	public Object doResultSetIntercept(Invocation invocation,ResultSetHandler resultSetHandler,MetaResultSetHandler metaResultSetHandler) throws Throwable {
		if (isRequireIntercept(invocation, resultSetHandler, metaResultSetHandler)) {
			//do some things
		}
		return invocation.proceed();
	}
	
	@Override
	public void doDestroyIntercept(Invocation invocation) throws Throwable{
		
	}

}
