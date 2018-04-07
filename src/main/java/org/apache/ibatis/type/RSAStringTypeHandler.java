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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RSAStringTypeHandler extends BaseTypeHandler<String> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
		try {
			//parameter = RSAHexCodec.getInstance().encodeByPublicKey(parameter, RSAHexCodec.public_key);
		} catch (Exception e) {
		}
		ps.setString(i, parameter);
	}

	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String returnValue = null;
		try {
			returnValue = rs.getString(columnName);
			if (null != returnValue) {
				//returnValue = RSAHexCodec.getInstance().decodeByPrivateKey(returnValue, RSAHexCodec.private_key);
			}
		} catch (Exception e) {
		}
		return returnValue;
	}

	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String returnValue = null;
		try {
			returnValue = rs.getString(columnIndex);
			if (null != returnValue) {
				//returnValue = RSAHexCodec.getInstance().decodeByPrivateKey(returnValue, RSAHexCodec.private_key);
			}
		} catch (Exception e) {
		}
		return returnValue;
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String returnValue = null;
		try {
			returnValue = cs.getString(columnIndex);
			if (null != returnValue) {
				//returnValue = RSAHexCodec.getInstance().decodeByPrivateKey(returnValue, RSAHexCodec.private_key);
			}
		} catch (Exception e) {
		}
		return returnValue;
	}

}
