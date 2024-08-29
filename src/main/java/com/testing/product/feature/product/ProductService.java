package com.testing.product.feature.product;

import com.testing.product.feature.product.dto.ProductRequest;
import com.testing.product.feature.product.dto.ProductResponse;
import com.testing.product.feature.product.dto.ProductUpdateRequest;
import org.springframework.data.domain.Page;

public interface ProductService {

    void createProduct(ProductRequest productRequest);

    Page<ProductResponse> getAllProducts(int pageNumber, int pageSize);

    ProductResponse getProductById(String id);

    ProductResponse updateProductById(String id, ProductUpdateRequest productUpdateRequest);

    void deleteProductById(String id);


}
