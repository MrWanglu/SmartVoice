/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.fintecher.pangolin.common.model;

import com.aliyuncs.AcsResponse;
import com.aliyuncs.transform.UnmarshallerContext;
import lombok.Data;

/**
 * Created by  gaobeibei.
 * Description: 阿里云短信发送
 * Date: 2017-11-02
 */
@Data
public class SendSmsResponse extends AcsResponse {

	private String requestId;

	private String bizId;

	private String code;

	private String message;

	@Override
	public SendSmsResponse getInstance(UnmarshallerContext context) {
		return	SendSmsResponseUnmarshaller.unmarshall(this, context);
	}
}
