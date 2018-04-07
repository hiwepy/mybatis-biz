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
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.injector.mapper.BaseMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.utils.ReflectionUtils;

/**
 * <p>
 * org.apache.ibatis.builder.xml.XMLMapperBuilder
 * </p>
 * <p>
 * injector curdSql
 * </p>
 */
public class MybatisXMLMapperBuilder extends XMLMapperBuilder {

	protected XPathParser parser_;
	protected MapperBuilderAssistant builderAssistant_;
	protected String resource_;

	@Deprecated
	public MybatisXMLMapperBuilder(Reader reader, Configuration configuration, String resource,
								   Map<String, XNode> sqlFragments, String namespace) {
		this(reader, configuration, resource, sqlFragments);
	}

	@Deprecated
	public MybatisXMLMapperBuilder(Reader reader, Configuration configuration, String resource,
			Map<String, XNode> sqlFragments) {
		super(reader, configuration, resource, sqlFragments);
		this.builderAssistant_ = (MapperBuilderAssistant) ReflectionUtils.getField("builderAssistant", this);
		this.parser_ = (XPathParser) ReflectionUtils.getField("parser", this);;
		this.resource_ = resource;
	}

	public MybatisXMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource,
			Map<String, XNode> sqlFragments, String namespace) {
		this(inputStream, configuration, resource, sqlFragments);
		this.builderAssistant_.setCurrentNamespace(namespace);
	}

	public MybatisXMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource,
			Map<String, XNode> sqlFragments) {
		super(inputStream,  configuration,  resource, sqlFragments);
		this.builderAssistant_ = (MapperBuilderAssistant) ReflectionUtils.getField("builderAssistant", this);
		this.parser_ = (XPathParser) ReflectionUtils.getField("parser", this);;
		this.resource_ = resource;
	}

	public void parse() {
		try {
			if (!configuration.isResourceLoaded(resource_)) {
				//configurationElement(parser_.evalNode("/mapper"));
				ReflectionUtils.getMethod(getClass(), "configurationElement").invoke(this, parser_.evalNode("/mapper"));
				configuration.addLoadedResource(resource_);
				bindMapperForNamespace();
			}
			//parsePendingResultMaps();
			//parsePendingChacheRefs();
			//parsePendingStatements();
			ReflectionUtils.getMethod(getClass(), "parsePendingResultMaps").invoke(this);
			ReflectionUtils.getMethod(getClass(), "parsePendingChacheRefs").invoke(this);
			ReflectionUtils.getMethod(getClass(), "parsePendingStatements").invoke(this);
		} catch (Exception e) {
			// ignore
		}
	}
  
	protected void bindMapperForNamespace() {
		String namespace = builderAssistant_.getCurrentNamespace();
		if (namespace != null) {
			Class<?> boundType = null;
			try {
				boundType = Resources.classForName(namespace);
			} catch (ClassNotFoundException e) {
				// ignore, bound type is not required
			}
			if (boundType != null) {
				if (!configuration.hasMapper(boundType)) {
					// Spring may not know the real resource name so we set a
					// flag
					// to prevent loading again this resource from the mapper
					// interface
					// look at MapperAnnotationBuilder#loadXmlResource
					configuration.addLoadedResource("namespace:" + namespace);
					configuration.addMapper(boundType);
				}
				//TODO 注入 CURD 动态 SQL
				if (BaseMapper.class.isAssignableFrom(boundType)) {
					MybatisContext.getSQLInjector(configuration).inspectInject(builderAssistant_, boundType);
				}
			}
		}
	}

}
