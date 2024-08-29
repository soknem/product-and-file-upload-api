package com.testing.product.mapper;

import com.testing.product.domain.Product;
import com.testing.product.feature.product.dto.ProductRequest;
import com.testing.product.feature.product.dto.ProductResponse;
import com.testing.product.feature.product.dto.ProductUpdateRequest;
import com.testing.product.utils.MediaUtil;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product fromRequest(ProductRequest videoRequest);

    @Mapping(source = "image",target = "image",qualifiedByName = "getImageUrl")
    ProductResponse toResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromRequest(@MappingTarget Product product, ProductUpdateRequest productUpdateRequest);

    @Named("getImageUrl")
    default String getLogoUrl(String image) {

        if (image != null && !image.trim().isEmpty()) {
            return MediaUtil.getUrl(image);
        } else {
            return null;
        }
    }
}
