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
package org.snaker.engine.handlers.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.SnakerException;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.core.AccessService;
import org.snaker.engine.core.Execution;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.handlers.IHandler;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.SubProcessModel;

/**
 * 结束流程实例的处理器
 * @author yuqs
 * @since 1.0
 */
public class EndProcessHandler implements IHandler {
	/**
	 * 结束当前流程实例，如果存在父流程，则触发父流程继续执行
	 */
	public void handle(Execution execution) {
		SnakerEngine engine = execution.getEngine();
		Order order = execution.getOrder();
		List<Task> tasks = engine.query().getActiveTasks(new QueryFilter().setOrderId(order.getId()));
		for(Task task : tasks) {
			if(task.isMajor()) throw new SnakerException("存在未完成的主办任务,请确认.");
			engine.task().complete(task.getId(), SnakerEngine.AUTO);
		}

		/**
		 * 流程结束回调函数
		 */
		Process pro = execution.getProcess();
		ProcessModel processModel = pro.getModel();
		if (!StringUtils.isEmpty(pro.getCallback())) {
			processModel.setCallbackUrl(pro.getCallback());
			Map<String,Object> params = new HashMap<>();
			//流程id
			params.put("orderId",order.getId());
			//AccessService.STATE_FINISH=0：正常结束 AccessService.STATE_TERMINATION=2:驳回/撤回
			params.put("state", AccessService.STATE_FINISH);
			params.put("auditOpinion","审核完毕");
			params.put("variables", execution.getArgs());
			params.put("processId",pro.getId());
			params.put("tenantId", execution.getArgs().get("tenantId"));
			processModel.getCallbackObject().callback(params, processModel.getCallbackUrl());
		}
		/**
		 * 结束当前流程实例
		 */
		engine.order().complete(order.getId());
		/**
		 * 如果存在父流程，则重新构造Execution执行对象，交给父流程的SubProcessModel模型execute
		 */
		if(StringHelper.isNotEmpty(order.getParentId())) {
			Order parentOrder = engine.query().getOrder(order.getParentId());
			if(parentOrder == null) return;
			Process process = engine.process().getProcessById(parentOrder.getProcessId());
			ProcessModel pm = process.getModel();
			if(pm == null) return;
			SubProcessModel spm = (SubProcessModel)pm.getNode(order.getParentNodeName());
            Execution newExecution = new Execution(engine, process, parentOrder, execution.getArgs());
            newExecution.setChildOrderId(order.getId());
            newExecution.setTask(execution.getTask());
			spm.execute(newExecution);
			/**
			 * SubProcessModel执行结果的tasks合并到当前执行对象execution的tasks列表中
			 */
			execution.addTasks(newExecution.getTasks());
		}
	}
}
