package com.xiaomi.hera.trace.etl.service.api;

import com.xiaomi.hera.trace.etl.domain.HeraTraceConfigVo;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.PageData;
import com.xiaomi.youpin.infra.rpc.Result;

import java.util.List;

public interface ManagerService {

    List<HeraTraceEtlConfig> getAll(HeraTraceConfigVo vo);

    PageData<List<HeraTraceEtlConfig>> getAllPage(HeraTraceConfigVo vo);

    HeraTraceEtlConfig getByBaseInfoId(Integer baseInfoId);

    HeraTraceEtlConfig getById(Integer id);

    Result insertOrUpdate(HeraTraceEtlConfig config, String user);

    int delete(HeraTraceEtlConfig config);
}
