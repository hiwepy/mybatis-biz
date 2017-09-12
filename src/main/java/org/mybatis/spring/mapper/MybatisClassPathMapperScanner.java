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
package org.mybatis.spring.mapper;

import java.util.Set;

import org.apache.ibatis.utils.MybatisUtils;
import org.apache.ibatis.utils.ReflectionUtils;
import org.mybatis.spring.cache.BeanMethodDefinition;
import org.mybatis.spring.cache.BeanMethodDefinitionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

public class MybatisClassPathMapperScanner extends ClassPathMapperScanner {

	protected Logger LOG = LoggerFactory.getLogger(MybatisClassPathMapperScanner.class);
	protected ApplicationContext applicationContext;
	
	public MybatisClassPathMapperScanner(ApplicationContext applicationContext,BeanDefinitionRegistry registry) {
		super(registry);
		this.applicationContext = applicationContext;
	}

	@Override
	public int scan(String... basePackages) {
		int count = 0;
		try {
			count = super.scan(basePackages);
		} catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(),e);
		}
		return count;
	}
	
	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitions) {
			try {
				//获取Spring扫描注入对象的名称
				String beanName = beanDefinitionHolder.getBeanName();
				//获取代理对象
				Object target = MybatisUtils.getTarget(applicationContext.getBean(beanName));
				//获取对象代理接口类型
				Class<?> beanClass = ((Class<?>)ReflectionUtils.getAccessibleField(target, "mapperInterface").get(target));
				//获取接口类名称
				String className = beanClass.getName();
				//缓存对象引用
				BeanMethodDefinition definition = new BeanMethodDefinition(beanName, 
						beanDefinitionHolder.getAliases(), 
						beanDefinitionHolder.getBeanDefinition(), 
						beanClass);
				BeanMethodDefinitionFactory.setBeanMethodDefinition(className, definition );
			} catch (Exception e) {
				LOG.error(e.getLocalizedMessage(),e);
			}
		}
		
		return beanDefinitions;
		
	}
	 

	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName){
		try {
			super.postProcessBeanDefinition(beanDefinition, beanName);
		} catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(),e);
		}
	} 
	
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		try {
			BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
		} catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(),e);
		}
	}
	
}
