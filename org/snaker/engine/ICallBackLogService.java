/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine;

import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.CallbackLog;

import java.util.List;

/**
 * 任务业务类，包括以下服务：
 * 1、查询列表
 * @author yuqs
 * @since 1.0
 */
public interface ICallBackLogService {
	/**
	 * 根据已有任务id、任务类型、参与者创建新的任务
	 * @return List<CallbackLog> 创建任务集合
	 */
	List<CallbackLog> getCallBackLogList(Page<CallbackLog> page, QueryFilter filter);
}
