package com.polarbookshop.catalogservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

public record Book(
    @Id
    Long id,
    @NotBlank
    @Pattern(
        regexp = "^([0-9]{10}|[0-9]{13})$",
        message = "The ISBN format must be valid."
    )
    String isbn,
    @NotBlank(message = "The book title must be defined.")
    String title,
    @NotBlank(message = "The book author must be defined.")
    String author,
    // @NotBlank(message = "The book price must be defined.")
    @Positive(message = "The book price must be greater than zero.")
    Double price,
    @CreatedDate
    Instant createdDate,
    @LastModifiedDate
    Instant lastModifiedDate,
    @Version
    int version // 낙관적 락을 위해 사용되는 엔티니 버전 번호(optimistic lock)
) {

    public static Book of(String isbn, String title, String author, Double price) {
        // id 가 null 이고, version 이 0 이면 새로운 엔티티로 인식한다.
        return new Book(null, isbn, title, author, price, null, null, 0);
    }

}
