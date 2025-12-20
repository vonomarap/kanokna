package com.kanokna.pricing_service.application.port.in;

import com.kanokna.pricing_service.application.dto.PriceBookCommand;
import com.kanokna.pricing_service.application.dto.CampaignCommand;
import com.kanokna.pricing_service.application.dto.TaxRuleCommand;

public interface PriceAdminPort {
    void createOrUpdatePriceBook(PriceBookCommand command);
    void publishPriceBook(String priceBookId);
    void defineCampaign(CampaignCommand command);
    void updateTaxRules(TaxRuleCommand command);
}
