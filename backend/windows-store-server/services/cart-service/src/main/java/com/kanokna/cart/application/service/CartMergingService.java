package com.kanokna.cart.application.service;

import com.kanokna.cart.application.service.dto.MergeResult;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * MODULE_CONTRACT id="MC-cart-merging"
 * LAYER="application.service"
 * INTENT="Handle cart merge operations when user logs in with anonymous cart"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;Technology.xml#DEC-CART-MERGE"
 *
 * Application service for cart merging operations.
 * Wraps domain CartMergeService with logging and application-level concerns.
 *
 * MERGE RULES (MERGE-001 to MERGE-006):
 * - MERGE-001: Items with same configuration_hash have quantities summed
 * - MERGE-002: Items with different configurations are added as new
 * - MERGE-003: Authenticated cart promo code takes precedence
 * - MERGE-004: If no auth promo, anonymous promo is adopted
 * - MERGE-005: All prices are refreshed after merge
 * - MERGE-006: Anonymous cart is marked as MERGED after operation
 */
@Service
public class CartMergingService {

    private static final Logger log = LoggerFactory.getLogger(CartMergingService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE = "UC-CART-MANAGE";

    private final CartMergeService domainMergeService;
    private final CartTotalsCalculator totalsCalculator;

    public CartMergingService(CartMergeService domainMergeService, CartTotalsCalculator totalsCalculator) {
        this.domainMergeService = domainMergeService;
        this.totalsCalculator = totalsCalculator;
    }

    /**
     * FUNCTION_CONTRACT id="FC-cart-merging-mergeCarts"
     * Merges source cart (anonymous) into target cart (authenticated).
     *
     * @param source the source cart (usually anonymous/session cart)
     * @param target the target cart (usually authenticated cart)
     * @return merge result with statistics
     */
    public MergeResult mergeCarts(Cart source, Cart target) {
        // BA-CART-MERGE-01: Load both carts
        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-MERGE-01")
            .addKeyValue("state", "START")
            .addKeyValue("sourceCartId", source.cartId().toString())
            .addKeyValue("targetCartId", target.cartId().toString())
            .addKeyValue("sourceItemCount", source.items().size())
            .addKeyValue("targetItemCount", target.items().size())
            .log("Starting cart merge");

        // BA-CART-MERGE-02: Match items by configuration_hash
        // BA-CART-MERGE-03: Merge items (sum quantities for matches, add new)
        // BA-CART-MERGE-04: Resolve promo code precedence
        CartMergeService.MergeResult domainResult = domainMergeService.merge(source, target, totalsCalculator);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-MERGE-03")
            .addKeyValue("state", "MERGED")
            .addKeyValue("itemsFromSource", domainResult.itemsFromAnonymous())
            .addKeyValue("itemsMerged", domainResult.itemsMerged())
            .addKeyValue("itemsAdded", domainResult.itemsAdded())
            .addKeyValue("promoPreserved", domainResult.promoCodePreserved())
            .addKeyValue("promoSource", domainResult.promoCodeSource())
            .log("Cart merge complete");

        return new MergeResult(
            domainResult.itemsFromAnonymous(),
            domainResult.itemsMerged(),
            domainResult.itemsAdded(),
            domainResult.promoCodePreserved(),
            domainResult.promoCodeSource()
        );
    }

    /**
     * Finds matching item in target cart by configuration hash.
     *
     * @param target the target cart
     * @param configurationHash the hash to match
     * @return true if matching item exists
     */
    public boolean findMatchingItem(Cart target, String configurationHash) {
        return target.findItemByHash(configurationHash).isPresent();
    }

    /**
     * Checks if two carts can be merged.
     *
     * @param source the source cart
     * @param target the target cart
     * @return true if merge is possible
     */
    public boolean canMerge(Cart source, Cart target) {
        if (source == null || target == null) {
            return false;
        }
        if (source.cartId().equals(target.cartId())) {
            return false; // Cannot merge cart into itself
        }
        if (source.items().isEmpty()) {
            return false; // Nothing to merge
        }
        return true;
    }
}
