package com.testing.product.feature.product;

import com.testing.product.feature.product.dto.ProductRequest;
import com.testing.product.feature.product.dto.ProductResponse;
import com.testing.product.feature.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@Valid @RequestBody ProductRequest productRequest) {

        productService.createProduct(productRequest);
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {

        return productService.getAllProducts(pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable String id) {

        return productService.getProductById(id);
    }

    @PatchMapping("/{id}")
    public ProductResponse updateProductById(@PathVariable String id,
                                             @Valid @RequestBody ProductUpdateRequest productUpdateRequest) {

        return productService.updateProductById(id, productUpdateRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProductById(@PathVariable String id) {

        productService.deleteProductById(id);
    }

}
