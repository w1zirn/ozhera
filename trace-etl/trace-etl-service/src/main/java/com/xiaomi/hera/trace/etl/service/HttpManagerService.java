package com.xiaomi.hera.trace.etl.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.data.push.client.HttpClientV6;
import com.xiaomi.hera.trace.etl.api.service.TraceEtlService;
import com.xiaomi.hera.trace.etl.domain.HeraTraceConfigVo;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.PageData;
import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.service.api.ManagerService;
import com.xiaomi.hera.trace.etl.util.pool.AsyncNotify;
import com.xiaomi.youpin.infra.rpc.Result;
import com.xiaomi.youpin.infra.rpc.errors.GeneralCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Description get trace config from http
 * @Author dingtao
 * @Date 2022/4/18 3:31 下午
 */
@Slf4j
public class HttpManagerService implements ManagerService {

    private String traceConfigApiDomain;

    private final Retryer<List<HeraTraceEtlConfig>> retryer;

    private Gson gson = new Gson();

    private static final Type TYPE = new TypeToken<List<HeraTraceEtlConfig>>() { }.getType();

    public HttpManagerService(String traceConfigApiDomain){
        this.traceConfigApiDomain = traceConfigApiDomain;
        retryer = RetryerBuilder.<List<HeraTraceEtlConfig>>newBuilder()
                .retryIfException()
                .retryIfResult(Objects::isNull)
                .withWaitStrategy(WaitStrategies.fixedWait(3, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
    }

    @Override
    public List<HeraTraceEtlConfig> getAll(HeraTraceConfigVo vo) {
        try {
            return retryer.call(this::getAll);
        }catch (Throwable t){
            log.error("retry get trace config error , ", t);
        }
        return null;
    }

    private List<HeraTraceEtlConfig> getAll(){
        String url = new StringBuilder(traceConfigApiDomain).append("/manager/getAllList").toString();
        try {
            String result = HttpClientV6.get(url, null);
            if(StringUtils.isEmpty(result)){
                List<HeraTraceEtlConfig> list = gson.fromJson(result, TYPE);
                return list;
            }else{
                log.error("get trace config result is null!");
            }
        }catch (Throwable t){
            log.error("get trace config error , ", t);
        }

        return null;
    }

    @Override
    public PageData<List<HeraTraceEtlConfig>> getAllPage(HeraTraceConfigVo vo) {
        return null;
    }

    @Override
    public HeraTraceEtlConfig getByBaseInfoId(Integer baseInfoId) {
        return null;
    }

    @Override
    public HeraTraceEtlConfig getById(Integer id) {
        return null;
    }

    @Override
    public Result insertOrUpdate(HeraTraceEtlConfig config, String user) {
        return null;
    }

    @Override
    public int delete(HeraTraceEtlConfig config) {
        return 0;
    }
}
