package com.xiaomi.hera.trace.etl.metadata;

import com.xiaomi.hera.trace.etl.api.AttributeService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import com.xiaomi.mone.app.api.model.HeraMetaDataModel;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class OzHeraMetaDataServiceImpl implements OzHeraMetaDataService{

    @Autowired
    private AttributeService attributeService;

    @Override
    public void syncHeraMetaData() {

    }

    @Override
    public HeraMetaDataModel getHeraMetaData(String peerIpPort) {
        return null;
    }

    @Override
    public HeraMetaDataModel getHeraMetaData(SpanHolder spanHolder) {
        return null;
    }

    @Override
    public String getMetricsMetaDataName(String peerIpPort) {
        HeraMetaDataModel heraMetaData = getHeraMetaData(peerIpPort);
        if(heraMetaData == null){
            return null;
        }
        String destApp;
        if(heraMetaData.getMetaId() == null){
            destApp = heraMetaData.getMetaName().replaceAll("-", "_");
        }else{
            destApp = heraMetaData.getMetaId() + "_" + heraMetaData.getMetaName().replaceAll("-", "_");
        }
        return destApp;
    }
}
