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
package org.apache.ibatis.builder.annotation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.MybatisXMLMapperBuilder;
import org.apache.ibatis.injector.mapper.BaseMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.utils.ReflectionUtils;

/**
 * <p>
 * 继承 MapperAnnotationBuilder 没有XML配置文件注入基础CRUD方法
 * </p>
 */
public class MybatisMapperAnnotationBuilder extends MapperAnnotationBuilder {

	protected Configuration configuration_;
	protected MapperBuilderAssistant assistant_;
	protected Class<?> type_;

	public MybatisMapperAnnotationBuilder(Configuration configuration, Class<?> type) {
		super(configuration, type);
		this.assistant_ = (MapperBuilderAssistant) ReflectionUtils.getField("assistant", this);
		this.configuration_ = configuration;
		this.type_ = type;
	}

	public void parse() {
		try {
			String resource = type_.toString();
			if (!configuration_.isResourceLoaded(resource)) {
				boolean existXml = loadXmlResource();
				configuration_.addLoadedResource(resource);
				assistant_.setCurrentNamespace(type_.getName());
				//parseCache();
				//parseCacheRef();
				ReflectionUtils.getMethod(getClass(), "parseCache").invoke(this);
				ReflectionUtils.getMethod(getClass(), "parseCacheRef").invoke(this);
				Method[] methods = type_.getMethods();
				// TODO 注入存在 xxMapper.xml CURD (应该在注解之前注入)
				inspectInject(existXml);
				for (Method method : methods) {
					try {
						// issue #237
						if (!method.isBridge()) {
							parseStatement(method);
						}
					} catch (IncompleteElementException e) {
						configuration_.addIncompleteMethod(new MethodResolver(this, method));
					}
				}

			}
			//parsePendingMethods();
			ReflectionUtils.getMethod(getClass(), "parsePendingMethods").invoke(this);
		} catch (Exception e) {
			// ignore
		}
	}

	/*
	 * 注入 CURD 动态 SQL(XML不存在时注入)
	 */
	private void inspectInject(boolean flag) {
		if (!flag && BaseMapper.class.isAssignableFrom(type_)) {
			MybatisContext.getSQLInjector(configuration_).inspectInject(assistant_, type_);
		}
	}

	/**
	 * 是否存在XML(该方法并不能客观的判断resource的路径,只是Mybatis默认认为的xml路径)
	 * @return boolean ture存在,flase不存在
	 */
	// 该方法改为返回 boolean 判断是否存在 xxxMapper.xml
	protected boolean loadXmlResource() {
		boolean flag = true;
		// Spring may not know the real resource name so we check a flag
		// to prevent loading again a resource twice
		// this flag is set at MybatisXMLMapperBuilder#bindMapperForNamespace
		if (!configuration_.isResourceLoaded("namespace:" + type_.getName())) {
			String xmlResource = type_.getName().replace('.', '/') + ".xml";
			InputStream inputStream = null;
			try {
				inputStream = Resources.getResourceAsStream(type_.getClassLoader(), xmlResource);
			} catch (IOException e) {
				// ignore, resource is not required
				flag = false;
			}
			if (inputStream != null) {
				MybatisXMLMapperBuilder xmlParser = new MybatisXMLMapperBuilder(inputStream, assistant_.getConfiguration(),
						xmlResource, configuration_.getSqlFragments(), type_.getName());
				xmlParser.parse();
			}
		}
		return flag;
	}

}