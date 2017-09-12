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

public class BeanMethodDefinition {

	protected BeanDefinition beanDefinition;
	protected String beanName;
	protected String[] aliases;
	protected Class<?> beanClass;
	protected String beanClassName;
	protected static final ConcurrentMap<String , Method> COMPLIED_METHODS = new ConcurrentHashMap<String , Method>();
	
	public BeanMethodDefinition(String beanName, String[] aliases, BeanDefinition beanDefinition, Class<?> beanClass) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
		this.beanClass = beanClass;
		this.beanClassName = beanClass.getName();
	}

	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}

	public String getBeanName() {
		return beanName;
	}

	public String[] getAliases() {
		return aliases;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public String getBeanClassName() {
		return beanClassName;
	}

	public Method getMethod(String methodName) {
		String uid = getBeanClassName() + "." + methodName;
		Method ret = COMPLIED_METHODS.get(uid);
		if (ret != null) {
			return ret;
		}
		synchronized (beanClass) {
			//查找指定的方法
			for (Class<?> superClass = beanClass; superClass != Object.class && superClass != null; superClass = superClass.getSuperclass()) {
				for (Method method : superClass.getDeclaredMethods()) {
					if(method.getName().equals(methodName) ){
						ret = method;
						Method existing = COMPLIED_METHODS.putIfAbsent(uid, ret);
						if (existing != null) {
							ret = existing;
						}
						return ret;
					}
				}
			}
		}
		return ret;
	}
	
	
	
	

}
