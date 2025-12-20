package com.kanokna.order_service.application.port.in;

import com.kanokna.order_service.application.dto.ScheduleInstallationCommand;
import com.kanokna.order_service.domain.model.Order;

public interface InstallationPort {
    Order scheduleInstallation(ScheduleInstallationCommand command);
}
