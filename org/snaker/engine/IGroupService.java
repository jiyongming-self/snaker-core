package org.snaker.engine;

import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WfGroup;
import org.snaker.engine.entity.WfGroupPerson;

import java.util.List;

public interface IGroupService {
    /**
     * 审核组列表
     * @param page
     * @return
     */
    List<WfGroup> getGroupList(Page<WfGroup> page, QueryFilter filter);

    /**
     * 保存
     * @param group
     * @return
     */
    String insert(WfGroup group);

    /**
     * 修改
     * @param group
     */
    void update(WfGroup group);

    /**
     * 获取审核组中的审核人列表
     * @param page
     * @param filter
     */
    List<WfGroupPerson> getPersonListByGroupId(Page<WfGroupPerson> page, QueryFilter filter);

    /**
     * 添加审核人
     * @param person
     * @return
     */
    String insertPerson(WfGroupPerson person);

    /**
     * 修改审核人
     * @param person
     */
    void updatePerson(WfGroupPerson person);

    /**
     * 修改审核组状态
     * @param group
     */
    void updateState(WfGroup group);

    void updatePersonState(WfGroupPerson person);

    /**
     * 通过id获取审核组
     * @param groupId
     * @return
     */
    WfGroup getGroupById(String groupId);

    /**
     * 通过id获取审核人
     * @param personId
     * @return
     */
    WfGroupPerson getPersonById(String personId);

    /**
     * 取出某状态的审核组信息
     * @param state
     * @return
     */
    List<WfGroup> getGroupList(Integer state);
}
