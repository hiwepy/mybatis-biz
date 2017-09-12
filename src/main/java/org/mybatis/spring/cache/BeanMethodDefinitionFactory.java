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
package org.mybatis.spring.cache;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.config.BeanDefinition;

public class BeanMethodDefinitionFactory {

	protected static final ConcurrentMap<String , BeanMethodDefinition> COMPLIED_BEAN_METHODS = new ConcurrentHashMap<String , BeanMethodDefinition>();
	
	public static BeanMethodDefinition getBeanMethodDefinition(String className) {
		return getBeanMethodDefinition(className, null);
	}
	
	public static BeanMethodDefinition getBeanMethodDefinition(String className,BeanMethodDefinition definition) {
		BeanMethodDefinition ret = COMPLIED_BEAN_METHODS.get(className);
		if (ret != null) {
			return ret;
		} 
		if(definition != null){
			BeanMethodDefinition existing = COMPLIED_BEAN_METHODS.putIfAbsent(className, definition);
			if (existing != null) {
				ret = existing;
			}
		}
		return ret;
	}
	
	public static BeanDefinition getBeanDefinition(String className) {
		BeanMethodDefinition definition = getBeanMethodDefinition(className);
		if(definition == null){
			return null;
		}
		return definition.getBeanDefinition();
	}
	
	/**
	 * String mappedStatementId = className + "." + method.getName();
	 */
	public static Method getMethodDefinition(String mappedStatementId) {
		int index = mappedStatementId.lastIndexOf(".");
		//类名称
		String className = mappedStatementId.substring(0, index);
		//方法名称
		String methodName = mappedStatementId.substring(index + 1);
		
		BeanMethodDefinition definition = getBeanMethodDefinition(className);
		if(definition == null){
			return null;
		}
		return definition.getMethod(methodName);
	}
	
	
	public static BeanMethodDefinition setBeanMethodDefinition(String className,BeanMethodDefinition definition) {
		BeanMethodDefinition ret = definition;
		BeanMethodDefinition existing = COMPLIED_BEAN_METHODS.putIfAbsent(className, definition);
		if (existing != null) {
			ret = existing;
		}
		return ret;
	}
	
	
}
