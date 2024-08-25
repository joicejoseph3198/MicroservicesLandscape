package com.example.productservice.mapper;

import com.example.UtilService.base.BaseMapper;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.enums.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper extends BaseMapper<Product,ConfigureProductDTO> {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    Product toEntity(ConfigureProductDTO productDTO);
    ConfigureProductDTO toDto(Product product);
}
