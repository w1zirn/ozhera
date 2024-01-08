package com.xiaomi.hera.trace.etl.metadata;

import com.xiaomi.mone.app.api.model.HeraMetaDataModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class OzHeraMetaDataServiceImpl implements OzHeraMetaDataService{

    @Override
    public void syncHeraMetaData() {

    }

    @Override
    public HeraMetaDataModel getHeraMetaData() {
        return null;
    }
}
