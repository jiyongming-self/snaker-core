package org.snaker.engine.core;


import org.snaker.engine.ICallBackLogService;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.CallbackLog;
import org.snaker.engine.helper.StringHelper;

import java.util.ArrayList;
import java.util.List;

public class CallBackLogService extends AccessService implements ICallBackLogService {

    @Override
    public List<CallbackLog> getCallBackLogList(Page<CallbackLog> page, QueryFilter filter) {
        StringBuffer sql = new StringBuffer("select * from wf_callback_log ");
        sql.append(" where 1=1 ");
        List<Object> paramList = new ArrayList();
        int i;

//
//        if (filter.getState() != null) {
//            sql.append(" and state = ? ");
//            paramList.add(filter.getState());
//        }

        if (StringHelper.isNotEmpty(filter.getDisplayName())) {
            sql.append(" and process_name like ? ");
            paramList.add("%" + filter.getDisplayName() + "%");
        }

        if (!filter.isOrderBySetted()) {
            filter.setOrder("asc");
            filter.setOrderBy("process_name");
        }
        return this.access().queryList(page, filter, CallbackLog.class, sql.toString(), paramList.toArray());

    }


}
