package com.kanokna.pricing_service.application.service;

import com.kanokna.pricing_service.application.dto.CampaignCommand;
import com.kanokna.pricing_service.application.dto.PriceBookCommand;
import com.kanokna.pricing_service.application.dto.TaxRuleCommand;
import com.kanokna.pricing_service.application.port.in.PriceAdminPort;
import com.kanokna.pricing_service.adapters.out.persistence.PriceBookJpaAdapter;
import com.kanokna.pricing_service.adapters.out.persistence.CampaignJpaAdapter;
import com.kanokna.pricing_service.adapters.out.persistence.TaxRuleJpaAdapter;
import com.kanokna.pricing_service.domain.model.Campaign;
import com.kanokna.pricing_service.domain.model.OptionPremiumKey;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.TaxRule;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PriceAdminService implements PriceAdminPort {

    private final PriceBookJpaAdapter priceBookAdapter;
    private final CampaignJpaAdapter campaignAdapter;
    private final TaxRuleJpaAdapter taxRuleAdapter;

    public PriceAdminService(PriceBookJpaAdapter priceBookAdapter, CampaignJpaAdapter campaignAdapter, TaxRuleJpaAdapter taxRuleAdapter) {
        this.priceBookAdapter = priceBookAdapter;
        this.campaignAdapter = campaignAdapter;
        this.taxRuleAdapter = taxRuleAdapter;
    }

    @Override
    @Transactional
    public void createOrUpdatePriceBook(PriceBookCommand command) {
        PriceBook priceBook = new PriceBook(
            command.id() == null ? Id.random() : command.id(),
            command.region(),
            command.currency(),
            command.status(),
            0,
            command.basePrices(),
            command.optionPremiums().entrySet().stream()
                .collect(Collectors.toMap(e -> {
                    String[] parts = e.getKey().split(":");
                    return new OptionPremiumKey(parts[0], parts[1]);
                }, Map.Entry::getValue))
        );
        priceBookAdapter.save(priceBook);
    }

    @Override
    @Transactional
    public void publishPriceBook(String priceBookId) {
        priceBookAdapter.findActiveById(Id.of(priceBookId))
            .map(PriceBook::publish)
            .ifPresent(priceBookAdapter::save);
    }

    @Override
    @Transactional
    public void defineCampaign(CampaignCommand command) {
        Campaign campaign = new Campaign(
            command.id() == null ? Id.random() : command.id(),
            command.name(),
            command.status(),
            command.percentOff(),
            command.startsAt(),
            command.endsAt()
        );
        campaignAdapter.save(campaign);
    }

    @Override
    @Transactional
    public void updateTaxRules(TaxRuleCommand command) {
        TaxRule rule = new TaxRule(
            command.region(),
            command.productType(),
            command.rate(),
            command.roundingPolicyCode()
        );
        taxRuleAdapter.save(rule);
    }
}
