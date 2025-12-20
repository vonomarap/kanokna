package com.kanokna.catalog_configuration_service.adapters.out.memory;

import com.kanokna.catalog_configuration_service.application.port.out.MediaMetadataPort;
import org.springframework.stereotype.Component;

@Component
public class AlwaysExistsMediaMetadataPort implements MediaMetadataPort {
    @Override
    public boolean exists(String mediaRef) {
        return true;
    }
}
