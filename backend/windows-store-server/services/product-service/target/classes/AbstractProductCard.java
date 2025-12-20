package com.kanokna.product_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.kanokna.product_service.domain.model.enums.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractProductCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "{productCard.title.required}")
    @Size(min = 4, max = 50, message = "{productCard.title.size}")
    @Column(name = "title", length = 50, nullable = false)
    @ToString.Include
    private String title;

    @Lob
    @NotBlank(message = "{productCard.description.required}")
    @Column(name = "description")
    private String description;

    @NotNull(message = "{productCard.price.required}")
    @Positive(message = "{productCard.price.positive}")
    @Digits(integer = 12, fraction = 2, message = "{productCard.price.format}")
    @Column(name = "actual_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal actualPrice;

    @PositiveOrZero(message = "{productCard.discount.positiveOrZero}")
    @Digits(integer = 12, fraction = 2, message = "{productCard.price.format}")
    @Column(name = "discount_price", precision = 12, scale = 2)
    private BigDecimal discountPrice;

    @NotBlank(message = "{productCard.currency.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "{productCard.image.url}")
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /*<FUNCTION_CONTRACT
        id="validateDiscountInvariant"
        module="mod.catalog.product-service"
        SPECIFICATION="RequirementsAnalysis.xml#PRICING.FORMULA"
        LINKS="MODULE_CONTRACT#mod.catalog.product-service,Technology.xml#MoneyTime">
      <ROLE_IN_MODULE>
        Ensures the discount price never exceeds the actual price before persistence or updates using Bean Validation.
      </ROLE_IN_MODULE>
      <SIGNATURE>
        <INPUT>
          - actualPrice:BigDecimal (state) > 0 and currency:Currency enum already set on the entity.
          - discountPrice:BigDecimal|null (state) >= 0 when present.
        </INPUT>
        <OUTPUT>
          - boolean true when the invariant holds; Bean Validation treats false as a constraint violation.
        </OUTPUT>
        <SIDE_EFFECTS>
          - None; read-only invariant check.
        </SIDE_EFFECTS>
      </SIGNATURE>
      <PRECONDITIONS>
        - actualPrice must be non-null and positive; discountPrice is null or positive if provided.
        - Currency field is non-null and consistent across price fields before the check.
      </PRECONDITIONS>
      <POSTCONDITIONS>
        - Returns true iff discountPrice is null or <= actualPrice.
        - False result triggers Bean Validation with message key productCard.discount.invalid.
      </POSTCONDITIONS>
      <INVARIANTS>
        - No rounding or mutation of monetary fields occurs during validation.
        - Price comparisons assume identical currency; currency normalization happens earlier in the flow.
      </INVARIANTS>
      <ERROR_HANDLING>
        - Bean Validation converts a false result into a constraint violation; this method does not throw exceptions.
      </ERROR_HANDLING>
      <LOGGING>
        - No logging to keep validation lightweight and free of side effects.
      </LOGGING>
      <TEST_CASES>
        <HAPPY_PATH>
          - actualPrice=100.00, discountPrice=null -> returns true.
          - actualPrice=100.00, discountPrice=80.00 -> returns true.
        </HAPPY_PATH>
        <EDGE_CASES>
          - actualPrice=100.00, discountPrice=120.00 -> returns false and triggers validation error.
          - actualPrice positive, discountPrice=0 -> returns true; negative discountPrice rejected earlier by Bean Validation.
        </EDGE_CASES>
        <SECURITY_CASES>
          - Not applicable; upstream authentication/authorization handles access control.
        </SECURITY_CASES>
      </TEST_CASES>
    </FUNCTION_CONTRACT>
    */
    @AssertTrue(message = "{productCard.discount.invalid}")
    public boolean isValidDiscount() {
        return discountPrice == null || discountPrice.compareTo(actualPrice) <= 0;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
