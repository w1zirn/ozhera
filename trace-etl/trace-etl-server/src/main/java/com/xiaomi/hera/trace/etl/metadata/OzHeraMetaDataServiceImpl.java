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
    public HeraMetaDataModel getHeraMetaData(String serviceName) {
        return null;
    }

    @Override
    public HeraMetaDataModel getHeraMetaData(SpanHolder spanHolder) {
        HeraMetaDataModel model = new HeraMetaDataModel();
        switch (spanHolder.getSpanKind()){
            case Client:
                if(SpanType.http.equals(spanHolder.getSpanType())){
                    Pair<String, String> hostAndOperation = attributeService.getHttpClientDestHostAndOperation(spanHolder);
                }
        }
        return null;
    }
}
