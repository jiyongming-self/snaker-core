package org.snaker.engine.core;


import org.snaker.engine.IGroupService;
import org.snaker.engine.access.AbstractDBAccess;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WfGroup;
import org.snaker.engine.entity.WfGroupPerson;
import org.snaker.engine.helper.StringHelper;

import java.sql.Types;
import java.util.*;

public class GroupService extends AccessService implements IGroupService {

    @Override
    public List<WfGroup> getGroupList(Page<WfGroup> page, QueryFilter filter) {
        StringBuffer sql = new StringBuffer("select id,name,state,create_Time,creator from wf_group ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList();
        int i;


        if (filter.getState() != null) {
            sql.append(" and state = ? ");
            paramList.add(filter.getState());
        }

        if (StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and Name like ? ");
            paramList.add("%" + filter.getDisplayName() + "%");
        }

        if (!filter.isOrderBySetted()) {
            filter.setOrder("asc");
            filter.setOrderBy("name");
        }
        return this.access().queryList(page, filter, WfGroup.class, sql.toString(), paramList.toArray());

    }

    @Override
    public String insert(WfGroup group) {
        String sql = "insert into wf_group (id,name,creator,create_Time,state) values (?,?,?,?,?)";
        String id = StringHelper.getPrimaryKey();
        group.setId(id);
        Object[] args = new Object[]{group.getId(), group.getName(), group.getCreator(), group.getCreateTime(), group.getState()};
        int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};

        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
        return id;
    }

    @Override
    public void update(WfGroup group) {
        String sql = "update wf_group set name=? where id=? ";
        Object[] args = new Object[]{ group.getName(), group.getId()};
        int[] type = new int[]{Types.VARCHAR, Types.VARCHAR};
        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
    }

    @Override
    public List<WfGroupPerson> getPersonListByGroupId(Page<WfGroupPerson> page, QueryFilter filter) {
        StringBuffer sql = new StringBuffer("select id,group_id,type,name,state,create_Time,creator from wf_group_person ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList();
        int i;


        if (filter.getParentId() != null) {
            sql.append(" and group_id = ? ");
            paramList.add(filter.getParentId());
        }

        if (StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and Name like ? ");
            paramList.add("%" + filter.getDisplayName() + "%");
        }

        if (!filter.isOrderBySetted()) {
            filter.setOrder("asc");
            filter.setOrderBy("name");
        }
        return this.access().queryList(page, filter, WfGroupPerson.class, sql.toString(), paramList.toArray());

    }

    @Override
    public String insertPerson(WfGroupPerson person) {
        String sql = "insert into wf_group_person (id,group_id,type,name,creator,create_Time,state) values (?,?,?,?,?,?,?)";
        String id = StringHelper.getPrimaryKey();
        person.setId(id);
        Object[] args = new Object[]{person.getId(), person.getGroupId(), person.getType(),person.getName(), person.getCreator(), person.getCreateTime(), person.getState()};
        int[] type = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,Types.INTEGER, Types.VARCHAR,Types.VARCHAR,Types.VARCHAR, Types.INTEGER};

        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
        return id;
    }

    @Override
    public void updatePerson(WfGroupPerson person) {
        String sql = "update wf_group_person set type=?, name=? where id=? ";
        Object[] args = new Object[]{ person.getType(),person.getName(), person.getId()};
        int[] type = new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR};
        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
    }

    @Override
    public void updateState(WfGroup group) {
        String sql = "update wf_group set state=? where id=? ";
        Object[] args = new Object[]{ group.getState(), group.getId()};
        int[] type = new int[]{Types.INTEGER, Types.VARCHAR};
        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
    }

    @Override
    public void updatePersonState(WfGroupPerson person) {
        String sql = "update wf_group_person set state=? where id=? ";
        Object[] args = new Object[]{ person.getState(), person.getId()};
        int[] type = new int[]{Types.INTEGER, Types.VARCHAR};
        ((AbstractDBAccess)this.access()).saveOrUpdate(buildMap(sql,args,type));
    }

    @Override
    public WfGroup getGroupById(String groupId) {
        String sql ="select id,name,state,create_Time,creator from wf_group where id=?";
        List<Object> paramList = new ArrayList();
            paramList.add(groupId);
        return this.access().queryObject(WfGroup.class, sql, paramList.toArray());
    }

    @Override
    public WfGroupPerson getPersonById(String personId) {
        String sql ="select id,group_id,type,name,state,create_Time,creator from wf_group_person where id=?";
        List<Object> paramList = new ArrayList();
        paramList.add(personId);
        return this.access().queryObject(WfGroupPerson.class, sql, paramList.toArray());
    }

    @Override
    public List<WfGroup> getGroupList(Integer state) {
        StringBuffer sql = new StringBuffer("select id,name,state,create_Time,creator from wf_group ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList();

         sql.append(" and state = ? ");
         paramList.add(state);

        List<WfGroup> list = this.access().queryList(WfGroup.class, sql.toString(), paramList.toArray());
        return list;
    }

    /**
     * isORM为false，需要构造map传递给实现类
     * @param sql
     * @param args
     * @param type
     * @return
     */
    private Map<String, Object> buildMap(String sql, Object[] args, int[] type) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("SQL", sql);
        map.put("ARGS", args);
        map.put("TYPE", type);
        return map;
    }
}
