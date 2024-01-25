/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
