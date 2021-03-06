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
package org.snaker.engine.entity;

import org.snaker.engine.helper.JsonHelper;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 流程工作单实体类（一般称为流程实例）
 * @author yuqs
 * @since 1.0
 */
public class CallbackLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	private String id;
	/**
	 * 流程实例id
	 */
	private String orderId;
	/**
	 * 请求参数
	 */
	private String variable;
	/**
	 * 回调结果
	 */
    private String result;

    /**
     * 请求时间
     */
    private String createTime;

	/**
	 * 流程定义名称
	 */
	private String processName;

	/**
	 * 申请人姓名
	 */
    private String applyName;

	/**
	 * 处理次数
	 */
	private Integer count;

	/**
	 * 状态：0:未处理；1：处理成功
	 */
	private Integer status;


    /**
     * 流程实例变量map集合
     */

	public String getVariable() {
		return variable;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getVariableMap() {
        Map<String, Object> map = JsonHelper.fromJson(this.variable, Map.class);
        if(map == null) return Collections.emptyMap();
        return map;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Order(id=").append(this.id);
		sb.append(",orderId=").append(this.orderId);
		sb.append(",variable=").append(this.variable);
		sb.append(",createTime").append(this.createTime);
		sb.append(",result=").append(this.result).append(")");
		sb.append(",process_name=").append(this.processName).append(")");
		sb.append(",count=").append(this.count).append(")");
		sb.append(",status=").append(this.status).append(")");
		return sb.toString();
	}

	public CallbackLog(){}

	public CallbackLog(String id,String orderId, String variable, String result, String createTime,
					   String processName, String applyName, Integer count, Integer status) {
		this.id = id;
		this.orderId = orderId;
		this.variable = variable;
		this.result = result;
		this.createTime = createTime;
		this.processName = processName;
		this.applyName = applyName;
		this.status = status;
		this.count = count;
	}
	public CallbackLog(String id,String orderId, String variable, String result, String createTime) {
		this.id = id;
		this.orderId = orderId;
		this.variable = variable;
		this.result = result;
		this.createTime = createTime;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getApplyName() {
		return applyName;
	}

	public void setApplyName(String applyName) {
		this.applyName = applyName;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
