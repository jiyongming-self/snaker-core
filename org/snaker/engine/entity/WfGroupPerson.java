package org.snaker.engine.entity;

import java.io.Serializable;

/**
 * <p>
 * 审核组-人
 * </p>
 *
 * @author GMQ
 * @since 2021-02-01
 */
public class WfGroupPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 审核组ID
     */
    private String groupId;

    /**
     * 类型：1：岗位；2：用户id；3：工号；4：角色id
     */
    private Integer type;

    private String name;

    /**
     * 状态：1：有效；0：无效
     */
    private Integer state;

    private String createTime;

    /**
     * 创建人
     */
    private String creator;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


    @Override
    public String toString() {
        return "WfGroupPerson{" +
            "id=" + id +
            ", groupId=" + groupId +
            ", type=" + type +
            ", name=" + name +
            ", state=" + state +
            ", createTime=" + createTime +
            ", creator=" + creator +
        "}";
    }

    public WfGroupPerson(){

    }

    /**
     *
     * @param id
     * @param groupId
     * @param type
     * @param name
     * @param state
     */
    public WfGroupPerson(String id, String groupId, Integer type, String name, Integer state) {
        this.id = id;
        this.groupId = groupId;
        this.type = type;
        this.name = name;
        this.state = state;
    }
}
