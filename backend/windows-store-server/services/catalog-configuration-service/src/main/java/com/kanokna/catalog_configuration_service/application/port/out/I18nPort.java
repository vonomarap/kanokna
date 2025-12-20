package com.kanokna.catalog_configuration_service.application.port.out;

import com.kanokna.shared.i18n.LocalizedString;

public interface I18nPort {

    LocalizedString resolveLabel(LocalizedString localizedString, String locale);
}
