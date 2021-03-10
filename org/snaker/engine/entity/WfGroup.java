package org.snaker.engine.entity;


import java.io.Serializable;

/**
 * <p>
 * 审核组
 * </p>
 *
 * @author GMQ
 * @since 2021-02-01
 */
public class WfGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 审核组名
     */
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
        return "WfGroup{" +
            "id=" + id +
            ", name=" + name +
            ", state=" + state +
            ", createTime=" + createTime +
            ", creator=" + creator +
        "}";
    }
}
