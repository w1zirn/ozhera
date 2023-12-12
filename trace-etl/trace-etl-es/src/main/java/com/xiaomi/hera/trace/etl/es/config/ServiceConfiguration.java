package com.xiaomi.hera.trace.etl.es.config;

import com.xiaomi.hera.trace.etl.domain.ConfigGetType;
import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.service.DubboManagerService;
import com.xiaomi.hera.trace.etl.service.HttpManagerService;
import com.xiaomi.hera.trace.etl.service.WriteEsService;
import com.xiaomi.hera.trace.etl.service.api.ManagerService;
import com.xiaomi.hera.trace.etl.util.es.EsTraceUtil;
import com.xiaomi.mone.es.EsProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/28 10:32 am
 */
@Configuration
public class ServiceConfiguration {

    @Value("${trace.config.get.type}")
    private String configGetType;

    @Value("${trace.config.get.http.domain}")
    private String configGetHttpDomain;

    @Autowired
    private HeraTraceEtlConfigMapper heraTraceEtlConfigMapper;

    @Autowired
    private EsProcessor esProcessor;

    @Resource(name = "errorEsProcessor")
    private EsProcessor errorEsProcessor;

    @Bean
    public ManagerService managerService() {
        if (ConfigGetType.HTTP.equals(configGetType)) {
            return new HttpManagerService(configGetHttpDomain);
        } else {
            return new DubboManagerService(heraTraceEtlConfigMapper);
        }
    }

    @Bean
    public WriteEsService writeEsService() {
        return new WriteEsService(new EsTraceUtil(esProcessor, errorEsProcessor));
    }
}
