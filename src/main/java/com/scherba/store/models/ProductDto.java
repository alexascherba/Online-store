package com.scherba.store.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ProductDto {
    @NotEmpty(message = "The name is required")
    private String name;
    @NotEmpty(message = "The category is required")
    private String category;
    @Min(0)
    private double price;
    @NotEmpty(message = "The city is required")
    private String city;

    @Size(min = 10, message = "The description should be at least 10 characters")
    @Size(max = 2000,  message = "The description cannot exceed 2000 characters")
    private String description;

    private MultipartFile imageFileName;
}
