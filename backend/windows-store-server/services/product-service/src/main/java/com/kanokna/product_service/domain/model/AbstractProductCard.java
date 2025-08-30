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
