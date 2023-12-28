package com.example.productservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.enums.*;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final Faker faker;

    @Autowired
    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
        this.faker = new Faker();
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

    @Override
    public ResponseDTO<String> deleteProduct(String productId){
        ResponseDTO<String> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Request processed successfully.", null);
        productRepository.findById(productId).ifPresentOrElse(
                product -> {
                    LOG.debug("Deleting product (Id: {}).", productId);
                    productRepository.deleteById(productId);
                    responseDTO.setData("Deleted product ("+productId+").");
                },
                ()->{
                    LOG.debug("Product not found (Id: {}).", productId);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Request Failed");
                    responseDTO.setData("Associated product couldn't be found.");
                }
        );
        return responseDTO;
    }

    @Override
    @Transactional
    public ResponseDTO<String> insertDummyData() {
        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.TRUE,"Data insertion complete.",null);
        generateDummyReviews();
        return responseDTO;
    }

    private void generateDummyReviews(){
        if(productRepository.findAll().isEmpty()){
            List<Product> reviewList = IntStream.rangeClosed(1,100)
                    .mapToObj(i->
                            new Product(
                                    String.valueOf(i),
                                    faker.commerce().promotionCode(),
                                    faker.commerce().brand(),
                                    faker.bothify("???###"),
                                    faker.options().option(Connectivity.class),
                                    faker.options().option(Switches.class),
                                    faker.options().option(KeyCaps.class),
                                    faker.options().option(Layout.class),
                                    faker.options().option(Category.class),
                                    faker.lorem().sentence(),
                                    faker.bothify("#.#x#.#x#.#"),
                                    faker.random().nextDouble(300,500),
                                    faker.random().nextDouble(4000,15000),
                                    faker.internet().url()
                            ))
                    .toList();
            productRepository.saveAll(reviewList);
        }
    }

    /* TODO: 1. Create APIs for deletion, updation and insert dummy data. */
    /* TODO: 2. Add Documentation */

}
