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
package org.snaker.engine.core;

import java.util.*;

import net.sf.cglib.core.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.snaker.engine.*;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.*;
import org.snaker.engine.entity.Process;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.impl.GeneralAccessStrategy;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TaskModel.PerformType;
import org.snaker.engine.model.TaskModel.TaskType;

/**
 * 任务执行业务类
 * @author yuqs
 * @since 1.0
 */
public class TaskService extends AccessService implements ITaskService {
	private static final String START = "start";

    //访问策略接口
	private TaskAccessStrategy strategy = null;
	/**
	 * 完成指定任务
	 */
	public Task complete(String taskId) {
		return complete(taskId, null, null);
	}

	/**
	 * 完成指定任务
	 */
	public Task complete(String taskId, String operator) {
		return complete(taskId, operator, null);
	}
	
	/**
	 * 完成指定任务
	 * 该方法仅仅结束活动任务，并不能驱动流程继续执行
	 * @see SnakerEngineImpl#executeTask(String, String, java.util.Map)
	 */
	public Task complete(String taskId, String operator, Map<String, Object> args) {
		Task task = access().getTask(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		task.setVariable(JsonHelper.toJson(args));
		if(!isAllowed(task, operator, args)) {
			throw new SnakerException("当前参与者[" + operator + "]不允许执行任务[taskId=" + taskId + "]");
		}
		HistoryTask history = new HistoryTask(task);
		history.setFinishTime(DateHelper.getTime());
		if (null != args && null != args.get("method")) {
			history.setTaskState(Integer.valueOf(args.get("method").toString()));
		} else {
			history.setTaskState(STATE_FINISH);
		}
		history.setOperator(operator);
		List<TaskActor> actors = access().getTaskActorsByTaskId(task.getId());
		List<HistoryTaskActor> historyTaskActorList = new ArrayList<>(actors.size()+1);
		for(int i = 0; i < actors.size(); i++) {
			HistoryTaskActor historyTaskActor = new HistoryTaskActor(actors.get(i));
			historyTaskActor.setIsOperator(0);
			historyTaskActorList.add(historyTaskActor);
		}
		if (null != args && null != args.get("territoryId")) {
			HistoryTaskActor historyTaskActor = new HistoryTaskActor();
			historyTaskActor.setActorId(args.get("territoryId").toString());
			historyTaskActor.setActorType(1);
			historyTaskActor.setIsOperator(1);
			historyTaskActor.setTaskId(taskId);
			historyTaskActorList.add(historyTaskActor);
		}
		history.setActorList(historyTaskActorList);
		access().saveHistory(history);
		access().deleteTask(task);
        Completion completion = getCompletion();
        if(completion != null) {
            completion.complete(history);
        }
		return task;
	}
	
	/**
	 * 提取指定任务，设置完成时间及操作人，状态不改变
	 */
	public Task take(String taskId, String operator, Map<String, Object> args) {
		Task task = access().getTask(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(!isAllowed(task, operator, args)) {
			throw new SnakerException("当前参与者[" + operator + "]不允许提取任务[taskId=" + taskId + "]");
		}
		task.setOperator(operator);
		task.setFinishTime(DateHelper.getTime());
		access().updateTask(task);
		return task;
	}

    /**
     * 唤醒指定的历史任务
     */
    public Task resume(String taskId, String operator) {
        HistoryTask histTask = access().getHistTask(taskId);
        AssertHelper.notNull(histTask, "指定的历史任务[id=" + taskId + "]不存在");
        boolean isAllowed = true;
        if(StringHelper.isNotEmpty(histTask.getOperator())) {
            isAllowed = histTask.getOperator().equals(operator);
        }
        if(isAllowed) {
            Task task = histTask.undoTask();
            task.setId(StringHelper.getPrimaryKey());
            task.setCreateTime(DateHelper.getTime());
            access().saveTask(task);
            assignTask(task.getId(), task.getOperator());
            return task;
        } else {
            throw new SnakerException("当前参与者[" + operator + "]不允许唤醒历史任务[taskId=" + taskId + "]");
        }
    }
	
	/**
	 * 向指定任务添加参与者
	 */
	public void addTaskActor(String taskId, String... actors) {
		addTaskActor(taskId, null, actors);
	}
	
	/**
	 * 向指定任务添加参与者
	 * 该方法根据performType类型判断是否需要创建新的活动任务
	 */
	public void addTaskActor(String taskId, Integer performType, String... actors) {
		Task task = access().getTask(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(!task.isMajor()) return;
		if(performType == null) performType = task.getPerformType();
		if(performType == null) performType = 0;
		switch(performType) {
		case 0:
			assignTask(task.getId(), actors);
			break;
		case 1:
			try {
				for(String actor : actors) {
					Task newTask = (Task)task.clone();
					newTask.setId(StringHelper.getPrimaryKey());
					newTask.setCreateTime(DateHelper.getTime());
					newTask.setOperator(actor);
					access().saveTask(newTask);
					assignTask(newTask.getId(), actor);
				}
			} catch(CloneNotSupportedException ex) {
				throw new SnakerException("任务对象不支持复制", ex.getCause());
			}
			break;
		default :
			break;
		}
	}
	
	/**
	 * 向指定任务移除参与者
	 */
	public void removeTaskActor(String taskId, String... actors) {
		Task task = access().getTask(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(task.isMajor()) {
			access().removeTaskActor(task.getId(), actors);
		}
	}
	
	/**
	 * 撤回指定的任务
	 */
	public Task withdrawTask(String taskId, String operator) {
		HistoryTask hist = access().getHistTask(taskId);
		AssertHelper.notNull(hist, "指定的历史任务[id=" + taskId + "]不存在");
		List<Task> tasks = null;
		if(hist.isPerformAny()) {
			tasks = access().getNextActiveTasks(hist.getId());
		} else {
			tasks = access().getNextActiveTasks(hist.getOrderId(), 
					hist.getTaskName(), hist.getParentTaskId());
		}
		if(tasks == null || tasks.isEmpty()) {
			throw new SnakerException("后续活动任务已完成或不存在，无法撤回.");
		}
		for(Task task : tasks) {
			access().deleteTask(task);
		}
		
		Task task = hist.undoTask();
		task.setId(StringHelper.getPrimaryKey());
		task.setCreateTime(DateHelper.getTime());
		access().saveTask(task);
		assignTask(task.getId(), task.getOperator());
		return task;
	}
	
	/**
	 * 驳回任务
	 */
	public Task rejectTask(ProcessModel model, Task currentTask) {
		String parentTaskId = currentTask.getParentTaskId();
		if(StringHelper.isEmpty(parentTaskId) || parentTaskId.equals(START)) {
			throw new SnakerException("上一步任务ID为空，无法驳回至上一步处理");
		}
		NodeModel current = model.getNode(currentTask.getTaskName());
		HistoryTask history = access().getHistTask(parentTaskId);
		NodeModel parent = model.getNode(history.getTaskName());
		if(!current.canRejected(parent)) {
			throw new SnakerException("无法驳回至上一步处理，请确认上一步骤并非fork、join、suprocess以及会签任务");
		}

		Task task = history.undoTask();
		task.setId(StringHelper.getPrimaryKey());
		task.setCreateTime(DateHelper.getTime());
		task.setOperator(history.getOperator());
		access().saveTask(task);
		assignTask(task.getId(), task.getOperator());
		return task;
	}

	/**
	 * 对指定的任务分配参与者。参与者可以为用户、部门、角色
	 * @param taskId 任务id
	 * @param actorIds 参与者id集合
	 */
	private void assignTask(String taskId, String... actorIds) {
		if(actorIds == null || actorIds.length == 0) return;
		for(String actorId : actorIds) {
			//修复当actorId为null的bug
			if(StringHelper.isEmpty(actorId)) continue;
			TaskActor taskActor = new TaskActor();
			taskActor.setTaskId(taskId);
			if (actorId.indexOf("@")!=-1) {
				String[] s = actorId.split("@");
				taskActor.setActorId(s[1]);
				taskActor.setActorType(Integer.valueOf(s[0]));
			} else {
				taskActor.setActorType(0);
				taskActor.setActorId(actorId);
			}
			access().saveTaskActor(taskActor);
		}
	}
	
	/**
	 * 根据已有任务、任务类型、参与者创建新的任务
	 * 适用于转派，动态协办处理
	 */
	public List<Task> createNewTask(String taskId, int taskType, String... actors) {
		Task task = access().getTask(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		List<Task> tasks = new ArrayList<Task>();
		try {
			Task newTask = (Task)task.clone();
			newTask.setTaskType(taskType);
			newTask.setCreateTime(DateHelper.getTime());
			newTask.setParentTaskId(taskId);
			tasks.add(saveTask(newTask, PerformType.ANY.ordinal(), actors));
		} catch (CloneNotSupportedException e) {
			throw new SnakerException("任务对象不支持复制", e.getCause());
		}
		return tasks;
	}

    /**
     * 获取任务模型
     * @param taskId 任务id
     * @return TaskModel
     */
    public TaskModel getTaskModel(String taskId) {
        Task task = access().getTask(taskId);
        AssertHelper.notNull(task);
        Order order = access().getOrder(task.getOrderId());
        AssertHelper.notNull(order);
        Process process = ServiceContext.getEngine().process().getProcessById(order.getProcessId());
        ProcessModel model = process.getModel();
        NodeModel nodeModel = model.getNode(task.getTaskName());
        AssertHelper.notNull(nodeModel, "任务id无法找到节点模型.");
        if(nodeModel instanceof TaskModel) {
            return (TaskModel)nodeModel;
        } else {
            throw new IllegalArgumentException("任务id找到的节点模型不匹配");
        }
    }

    /**
	 * 由DBAccess实现类创建task，并根据model类型决定是否分配参与者
	 * @param taskModel 模型
	 * @param execution 执行对象
	 * @return List<Task> 任务列表
	 */
	public List<Task> createTask(TaskModel taskModel, Execution execution) {
		List<Task> tasks = new ArrayList<Task>();
		
		Map<String, Object> args = execution.getArgs();
		if(args == null) args = new HashMap<String, Object>();
		Date expireDate = DateHelper.processTime(args, taskModel.getExpireTime());
		Date remindDate = DateHelper.processTime(args, taskModel.getReminderTime());
		String form = (String)args.get(taskModel.getForm());
		String actionUrl = StringHelper.isEmpty(form) ? taskModel.getForm() : form;
		
		String[] actors = getTaskActors(taskModel, execution);
		if (null == actors || actors.length == 0 || StringUtils.isEmpty(actors[0])) {
			throw new SnakerException(taskModel.getDisplayName()+"没有找到相应的审核人，提交失败");
		}
		//该组织架构没有这级审核人，跳过这个任务节点
		if (actors.length == 1 && "skip".equals(actors[0])) {
			if (null == args.get("result")) {
				args.put("result", "agree");
			}
			taskModel.execute(execution);
			return execution.getTasks();
		}
		Task task = createTaskBase(taskModel, execution);
		task.setActionUrlType(taskModel.getFormType());
		task.setActionUrl(actionUrl);
		task.setExpireDate(expireDate);
		task.setExpireTime(DateHelper.parseTime(expireDate));
        task.setVariable(JsonHelper.toJson(args));
		
		if(taskModel.isPerformAny()) {
			//任务执行方式为参与者中任何一个执行即可驱动流程继续流转，该方法只产生一个task
			task = saveTask(task, PerformType.ANY.ordinal(), actors);
			task.setRemindDate(remindDate);
			tasks.add(task);
			//判断当前任务（流转到的新任务节点）的审核人中是不是以前审核过该流程实例的任务(并且当前新任务和刚执行的任务审核页面相同)
			String operator = nextChecker(actors,taskModel,execution);
			if (taskModel.getCanSkip().equals("Y") && !StringUtils.isEmpty(operator)) {
				//自动执行这个任务
				args.put("result", "agree");
				complete(task.getId(), operator, (Map) args);
				execution.setTask(task);
				taskModel.execute(execution);
			}

		} else if(taskModel.isPerformAll()){
			//任务执行方式为参与者中每个都要执行完才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
			for(String actor : actors) {
                Task singleTask = null;
                try {
                    singleTask = (Task) task.clone();
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = saveTask(singleTask, PerformType.ALL.ordinal(), actor);
                singleTask.setRemindDate(remindDate);
                tasks.add(singleTask);
				//判断当前任务（流转到的新任务节点）的审核人中是不是以前审核过该流程实例的任务(并且当前新任务和刚执行的任务审核页面相同)
				String[] aa = new String[]{actor};
				String operator = nextChecker(aa,taskModel,execution);
				if (taskModel.getCanSkip().equals("Y") && !StringUtils.isEmpty(operator)) {
					//自动执行这个任务
					args.put("result", "agree");
					complete(singleTask.getId(), operator, (Map) args);
					execution.setTask(singleTask);
					taskModel.execute(execution);
				}
			}
		}

		return tasks;
	}

	/**
	 * 当前新任务和刚执行的任务审核页面相同
	 * @param task
	 * @return
	 */
	private boolean sameForm(Task task) {
		if (null != task && !StringUtils.isEmpty(task.getParentTaskId())) {
			Task preTask = access.getTask(task.getParentTaskId());
			if (null != preTask) {
				if (task.getActionUrl().equals(preTask.getActionUrl())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断当前任务（流转到的新任务节点）的审核人中是不是在前边节点已经审核过
	 * @param actors
	 * @param taskModel
	 * @param execution
	 * @return 匹配的审核人userId
	 */
	private String nextChecker(String[] actors, TaskModel taskModel, Execution execution) {
		Order order = execution.getOrder();
		if (null == order || StringUtils.isEmpty(order.getId())) {
			return null;
		}
		ProcessModel model = execution.getModel();
		if (null == model) {
			return null;
		}
		List<TaskModel> taskModels = model.getTaskModels();
		if (null == taskModels || taskModels.size() == 0) {
			return null;
		}
		List<String> taskNameList = new ArrayList<>();
		for (TaskModel taskModel1 :taskModels) {
			if (taskModel1.getName().equals(taskModel.getName())) {
				break;
			}
			taskNameList.add(taskModel1.getName());
		}
		List<String> realOperatorList = access.getRealOperatorList(order.getId(), taskNameList);
		return strategy.canSkip(realOperatorList,actors,getTenantId(execution));
	}

	/**
	 * 获取租户信息
	 * @param execution
	 * @return
	 */
	private String getTenantId(Execution execution) {
		Map<String, Object> args = execution.getArgs();
		if (null == args || null == args.get("tenantId")) {
			return null;
		}
		return args.get("tenantId").toString();
	}
	/**
	 * 根据模型、执行对象、任务类型构建基本的task对象
	 * @param model 模型
	 * @param execution 执行对象
	 * @return
	 */
	private Task createTaskBase(TaskModel model, Execution execution) {
		Task task = new Task();
		task.setOrderId(execution.getOrder().getId());
		task.setTaskName(model.getName());
		task.setDisplayName(model.getDisplayName());
		task.setCreateTime(DateHelper.getTime());
		if(model.isMajor()) {
			task.setTaskType(TaskType.Major.ordinal());
		} else {
			task.setTaskType(TaskType.Aidant.ordinal());
		}
		task.setParentTaskId(execution.getTask() == null ? 
				START : execution.getTask().getId());
		task.setModel(model);
		return task;
	}
	
	/**
	 * 由DBAccess实现类持久化task对象
	 */
	private Task saveTask(Task task, int performType, String... actors) {
		task.setId(StringHelper.getPrimaryKey());
		task.setPerformType(PerformType.ANY.ordinal());
		access().saveTask(task);
		assignTask(task.getId(), actors);
		task.setActorIds(actors);
		return task;
	}

	/**
	 * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
	 * @param model 模型
	 * @param execution 执行对象
	 * @return
	 */
	private String[] getTaskActors(TaskModel model, Execution execution) {
		Object assigneeObject = null;
        AssignmentHandler handler = model.getAssignmentHandlerObject();
		if (null != execution.getArgs().get("nextOperator")){
			assigneeObject = execution.getArgs().get("nextOperator");
		}  else if(StringHelper.isNotEmpty(model.getAssigneeText())) {
			assigneeObject = getGroupPersons(model.getAssigneeText());
		} else if(StringHelper.isNotEmpty(model.getAssignee())) {
			assigneeObject = execution.getArgs().get(model.getAssignee());
		} else if(handler != null) {
            if(handler instanceof Assignment) {
                assigneeObject = ((Assignment)handler).assign(model, execution);
                if (assigneeObject instanceof Boolean) {
					if((Boolean) assigneeObject) {
						return new String[]{"skip"};
					} else {
						throw new SnakerException(model.getDisplayName()+"任务节点未找到相应的审核人");
					}
				}
            } else {
                assigneeObject = handler.assign(execution);
            }
		}
		return getTaskActors(assigneeObject == null ? model.getAssignee() : assigneeObject);
	}

	private String getGroupPersons(String groupId) {
		List<WfGroupPerson> personList = access().getPersonByGoupId(groupId);
		StringBuffer buffer = new StringBuffer();
		if (null != personList) {
			for (WfGroupPerson person: personList) {
				buffer.append(person.getType());
				buffer.append("@");
				buffer.append(person.getName());
				buffer.append(",");
			}
		}
		if (buffer.length() > 0) {
			buffer.substring(0,buffer.length()-1);
		}
		return buffer.toString();
	}

	/**
	 * 根据taskmodel指定的assignee属性，从args中取值
	 * 将取到的值处理为String[]类型。
	 * @param actors
	 * @return
	 */
	private String[] getTaskActors(Object actors) {
		if(actors == null) return null;
		String[] results = null;
		if(actors instanceof String) {
			//如果值为字符串类型，则使用逗号,分隔，并解析为Long类型
			String[] actorStrs = ((String)actors).split(",");
			results = new String[actorStrs.length];
			for(int i = 0; i < actorStrs.length; i++) {
				results[i] = actorStrs[i];
			}
			return results;
        } else if(actors instanceof List){
            //jackson会把stirng[]转成arraylist，此处增加arraylist的逻辑判断,by 红豆冰沙2014.11.21
            results= (String[])((List)actors).toArray();
            return results;

		} else if(actors instanceof Long) {
			//如果为Long类型，则返回1个元素的String[]
			results = new String[1];
			results[0] = String.valueOf((Long)actors);
			return results;
		} else if(actors instanceof Integer) {
			//如果为Long类型，则返回1个元素的String[]
			results = new String[1];
			results[0] = String.valueOf((Integer)actors);
			return results;
		} else if(actors instanceof String[]) {
			//如果为String[]类型，则直接返回
			return (String[])actors;
		} else {
			//其它类型，抛出不支持的类型异常
			throw new SnakerException("任务参与者对象[" + actors + "]类型不支持."
					+ "合法参数示例:Long,new String[]{},'10000,20000'");
		}
	}

	/**
	 * 判断当前操作人operator是否允许执行taskId指定的任务
	 */
	public boolean isAllowed(Task task, String operator, Map<String, Object> args) {
		if(StringHelper.isNotEmpty(operator)) {
			if(SnakerEngine.ADMIN.equalsIgnoreCase(operator)
					|| SnakerEngine.AUTO.equalsIgnoreCase(operator)) {
				return true;
			}
			if(StringHelper.isNotEmpty(task.getOperator())) {
				return operator.equals(task.getOperator());
			}
		}
		List<TaskActor> actors = access().getTaskActorsByTaskId(task.getId());
		if(actors == null || actors.isEmpty()) return true;
		if(StringHelper.isEmpty(operator)) return false;
		return getStrategy().isAllowed(operator, actors ,args);
	}

	public void setStrategy(TaskAccessStrategy strategy) {
		this.strategy = strategy;
	}

	public TaskAccessStrategy getStrategy() {
		if(strategy == null) {
			strategy = ServiceContext.find(TaskAccessStrategy.class);
		}
		if(strategy == null) {
			strategy = new GeneralAccessStrategy();
			ServiceContext.put(strategy.getClass().getName(), strategy);
		}
		return strategy;
	}
}
