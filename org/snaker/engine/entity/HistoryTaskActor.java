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

import java.io.Serializable;

/**
 * 历史任务参与者实体类
 * @author yuqs
 * @since 1.0
 */
public class HistoryTaskActor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -998098931519373599L;
	/**
	 * 关联的任务ID
	 */
    private String taskId;
    /**
     * 关联的参与者ID（参与者可以为用户、部门、角色）
     */
    private String actorId;
	/**
	 * 参与者类型
	 * TerritoryId(1,"岗位"),
	 * 	 *     USERID(2,"用户id"),
	 * 	 *     NUM(3,"工号"),
	 * 	 *     ROLEID(4,"角色id");
	 */
	private Integer actorType;
	/**
	 * 是否是实际操作者 是：1 否：0
	 */
	private Integer isOperator;
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getActorId() {
		return actorId;
	}
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public Integer getActorType() {
		return actorType;
	}

	public void setActorType(Integer actorType) {
		this.actorType = actorType;
	}

	public Integer getIsOperator() {
		return isOperator;
	}

	public void setIsOperator(Integer isOperator) {
		this.isOperator = isOperator;
	}

	public HistoryTaskActor(TaskActor taskActor) {
		this.taskId = taskActor.getTaskId();
		this.actorId = taskActor.getActorId();
		this.actorType = taskActor.getActorType();
	}
	public HistoryTaskActor(){}

}
