package org.snaker.engine.scheduling;


import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.DBAccess;
import org.snaker.engine.SnakerException;
import org.snaker.engine.core.ServiceContext;
import org.snaker.engine.entity.CallbackLog;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.helper.StringHelper;

import java.util.Date;
import java.util.Map;

/**
 * 流程结束或者打回执行后的回调类
 * @author yuqs
 * @since 1.4
 */
public class WfCallBack {
    private static Logger log = LoggerFactory.getLogger(WfCallBack.class);
    /**
     * 回调函数
     */
    public void callback(Map<String, Object> params, String url) {
        String post = HttpUtil.post(url, params);
        DBAccess access = ServiceContext.find(DBAccess.class);
        CallbackLog callbackLog = new CallbackLog(StringHelper.getPrimaryKey(),params.get("orderId").toString(), JsonHelper.toJson(params),post, DateHelper.getTime());
        access.saveCallbackLog(callbackLog);
        log.info("{},回调结果：{}",params.get("orderId"),post);
        /* throw new SnakerException("回调函数调用失败");*/
    }
}
