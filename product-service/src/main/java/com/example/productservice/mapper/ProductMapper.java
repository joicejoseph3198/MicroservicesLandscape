package com.example.productservice.mapper;

import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface ProductMapper extends BaseMapper<Product,ProductDTO> {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    Product toEntity(ProductDTO productDTO);
    ProductDTO toDto(Product product);
}
