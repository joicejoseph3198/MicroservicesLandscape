package com.example.productservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }

    @Override
    public ResponseDTO<ProductDTO> getProductBySkuCode(String skuCode) {
        ResponseDTO<ProductDTO> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Request processed successfully.", null);
        productRepository.findBySkuCode(skuCode)
                .ifPresentOrElse(product -> {
                    LOG.debug("Fetching product({}).", skuCode);
                    ProductDTO productDTO = productMapper.toDto(product);
                    responseDTO.setData(productDTO);
                }, () -> {
                    LOG.debug("Associated product({}) not found.", skuCode);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("No product with the associated skuCode present.");
                });
        return responseDTO;

    }

    @Override
    public ResponseDTO<String> createProduct(ProductDTO productDTO) {
        ResponseDTO<String> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Product successfully created.", null);

        Optional.ofNullable(productDTO)
                .filter(product ->
                        !productRepository.existsByBrandAndModelAndConnectivityAndKeySwitchesAndCategory(
                                product.getBrand(),
                                product.getModel(),
                                product.getConnectivity(),
                                product.getKeySwitches(),
                                product.getCategory()
                        ))
                .map(productMapper::toEntity)
                .ifPresentOrElse(entity -> {
                    LOG.debug("Creating product ({}).", entity.getSkuCode());
                    productRepository.save(entity);
                }, () -> {
                    LOG.debug("Unable to create product ({},{},{},{},{}).",
                            productDTO.getBrand(),
                            productDTO.getModel(),
                            productDTO.getConnectivity(),
                            productDTO.getKeySwitches(),
                            productDTO.getCategory()
                    );
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Invalid Request or Duplicate Product");
                });

        return responseDTO;
    }

    /* TODO: 1. Create APIs for deletion, updation and insert dummy data. */
    /* TODO: 2. Add Documentation */

}
