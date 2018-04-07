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
package org.apache.ibatis.executor.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

@SuppressWarnings({"unchecked","rawtypes"})
public class MapResultHandler implements ResultHandler<String> {

	private final List<Map<String, String>> list;

	public MapResultHandler() {
		list = new ArrayList<Map<String, String>>();
	}

	public MapResultHandler(ObjectFactory objectFactory) {
		list = objectFactory.create(List.class);
	}
	
	public void handleResult(ResultContext<? extends String> context) {
		Object object = context.getResultObject();
		if (object instanceof Map) {
			list.add((Map) object);
		} else {
			try {
				list.add(BeanUtils.describe(object));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<Map<String, String>> getResultList() {
		return list;
	}


}
