package com.example.productservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.dto.SearchIndexDTO;
import com.example.productservice.entity.Product;
import com.example.productservice.enums.*;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.messaging.SearchProducer;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final Faker faker;
    private final MongoTemplate mongoTemplate;
    private final SearchProducer searchProducer;

    // Used for testing resiliency
    // private static final int FAULT_PERCENT = 50;

    @Autowired
    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository, MongoTemplate mongoTemplate, SearchProducer searchProducer) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.searchProducer = searchProducer;
        this.faker = new Faker();
    }

    // Get the order for a given status string
    public static Integer getOrder(String status) {
        if (status == null) {
            return Integer.MAX_VALUE; // Default for unknown or null statuses
        }
        return switch (status.toUpperCase()) {
            case "START BIDDING" -> 1;
            case "COMING SOON" -> 2;
            case "SOLD OUT" -> 3;
            case "PRODUCT UNAVAILABLE" -> 4;
            default -> Integer.MAX_VALUE; // Default for unrecognized statuses
        };
    }

    @Override
    public ResponseDTO<ConfigureProductDTO> getProductBySkuCode(String skuCode) {
        log.info("Fetching product details for: {}",skuCode.toUpperCase());
        // For testing resiliency
//        int randomThreshold = RandomGenerator.getDefault().nextInt(1, 100);
//        if (FAULT_PERCENT < randomThreshold) {
//            log.info("Bad luck, an error occurred, {} >= {}", FAULT_PERCENT, randomThreshold);
//            throw new UnsupportedOperationException("Something went wrong...[RESILIENCY TESTING]");
//        }

        ResponseDTO<ConfigureProductDTO> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Request processed successfully.", null);
        productRepository.findBySkuCode(skuCode)
                .ifPresentOrElse(product -> {
                    log.debug("Fetching product({}).", skuCode.toUpperCase());
                    ConfigureProductDTO productDTO = productMapper.toDto(product);
                    responseDTO.setData(productDTO);
                }, () -> {
                    log.debug("Associated product({}) not found.", skuCode.toUpperCase());
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("No product with the associated productId present.");
                });
        return responseDTO;
    }

    @Override
    @Transactional
    public ResponseDTO<String> createProduct(ConfigureProductDTO productDTO) {
        try {
            log.info("Received createProduct request");
            ResponseDTO<String> responseDTO =
                    new ResponseDTO<>(Boolean.TRUE, "Product successfully created.", null);
            Optional.ofNullable(productDTO)
                    .filter(product ->
                            !productRepository.existsByModelNumber(product.modelNumber()))
                    .map(productMapper::toEntity)
                    .ifPresentOrElse(entity -> {
                        entity.setSkuCode(UUID.randomUUID().toString().substring(0, 6));
                        entity.setStatus(Status.UNPUBLISHED);
                        log.info("Creating product ({},{}).", entity.getSkuCode(), entity.getStatus());
                        productRepository.save(entity);
                        SearchIndexDTO searchIndexDTO = new SearchIndexDTO(
                                entity.getProductName(),
                                entity.getBrandName(),
                                entity.getProductDescription(),
                                entity.getSkuCode());
                        searchProducer.createSearchEntry(searchIndexDTO);
                    }, () -> {
                        log.info("Unable to create product ({}).", productDTO.modelNumber());
                        responseDTO.setStatus(Boolean.FALSE);
                        responseDTO.setMessage("Invalid Request or Duplicate Product");
                    });
            log.info("finished processing createProduct request");
            return responseDTO;
        }catch (Exception e){
            log.error("Error creating product : {}", productDTO.skuCode());
            return null;
        }
    }

    /*
    Filters data by dynamically generating query based on the received input
    */
    @Override
    public Page<ConfigureProductDTO> filterData(FilterProductDTO filterProductDTO){
        Integer offset = Optional.ofNullable(filterProductDTO.offset()).orElse(0);
        Integer limit = Optional.ofNullable(filterProductDTO.limit()).orElse(10);
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Query dynamicQuery = new Query().with(pageable);

        // call function to add sub-queries
        Optional.of(filterProductDTO)
                .ifPresent(request ->
                        addCriteriaToQuery(request,dynamicQuery)
                );
        List<Product> queryResult = mongoTemplate.find(dynamicQuery, Product.class);
        List<ConfigureProductDTO> productList = productMapper.toDtoList(queryResult).stream()
                .sorted(Comparator.comparingInt(p->getOrder(p.status())))
                .toList();
        return PageableExecutionUtils.getPage(productList, pageable,
                ()-> mongoTemplate.count(dynamicQuery, Product.class));
    }
    /*
    creates criteria based on input fields and their values, and adds them to the provided query (givenQuery)
    */
    private void addCriteriaToQuery(FilterProductDTO filterProductDTO, Query givenQuery){
        log.info("constructing queries for filtering.");
        List<Criteria> andCriteriaList = new ArrayList<>();

        // Create BETWEEN query for price
        if(Objects.nonNull(filterProductDTO.minPrice()) && Objects.nonNull(filterProductDTO.maxPrice())){
            Criteria priceCriteria =
                    Criteria
                            .where("price")
                            .gt(filterProductDTO.minPrice())
                            .lt(filterProductDTO.maxPrice());
            andCriteriaList.add(priceCriteria);
        }
        // Create IN query for switches, keyCaps, brand, category, connectivity, layout
        Field[] fields = filterProductDTO.getClass().getDeclaredFields();
        for(Field currentField: fields){
            try {
                currentField.setAccessible(true);
                if(Collection.class.isAssignableFrom(currentField.getType()) && !CollectionUtils.isEmpty((Collection<?>) currentField.get(filterProductDTO))){
                    Criteria valuesInCriteria =
                            Criteria
                                    .where(currentField.getName())
                                    .in(((Collection<?>) currentField.get(filterProductDTO)).toArray());
                    andCriteriaList.add(valuesInCriteria);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        // Create SORT Query
        Sort sort;
        if(Objects.nonNull(filterProductDTO.sortType())){
            if (Objects.requireNonNull(filterProductDTO.sortType()) == SortType.PRICE_DESC) {
                sort = Sort.by(Sort.Direction.DESC, "price");
            } else {
                sort = Sort.by(Sort.Direction.ASC, "price");
            }
            givenQuery.with(sort);
        }else{
            givenQuery.with(Sort.by(Sort.Direction.DESC, "lastModifiedDate"));
        }
        if(!CollectionUtils.isEmpty(andCriteriaList)){
            givenQuery.addCriteria(new Criteria().andOperator(andCriteriaList));
        }
    }

    @Override
    public ResponseDTO<String> deleteProduct(String productId){
        ResponseDTO<String> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Request processed successfully.", null);
        productRepository.findById(productId).ifPresentOrElse(
                product -> {
                    log.debug("Deleting product (Id: {}).", productId);
                    productRepository.deleteById(productId);
                    responseDTO.setData("Deleted product ("+productId+").");
                },
                ()->{
                    log.debug("Product not found (Id: {}).", productId);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Request Failed");
                    responseDTO.setData("Associated product couldn't be found.");
                }
        );
        return responseDTO;
    }

    @Override
    public ResponseDTO<String> updateProductStatus(String skuCode, Status status) {
        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.FALSE,"Request failed.",null);
        Optional<Product> product = productRepository.findBySkuCode(skuCode);
        product.ifPresentOrElse(productData->{
            productData.setStatus(status);
            productRepository.save(productData);
            if(productData.getStatus().equals(Status.UNPUBLISHED)){
                searchProducer.deleteSearchEntry(productData.getSkuCode());
            }
            responseDTO.setStatus(Boolean.TRUE);
            responseDTO.setMessage("Request processed.");
            responseDTO.setData("Status updated to " + status);
            log.info("Updated status of product: {}, to {}", skuCode.toUpperCase(), status);
        },()-> responseDTO.setData("Product not found."));
        return responseDTO;
    }

    @Override
    public ResponseDTO<String> syncSearch() {
        List<Product> productCatalogue = productRepository.findByStatusNotIn(List.of(Status.UNPUBLISHED));
        if(!productCatalogue.isEmpty()){
            productCatalogue.stream()
                    .map(product -> {
                        return new SearchIndexDTO(
                                product.getProductName(),
                                product.getBrandName(),
                                product.getProductDescription(),
                                product.getSkuCode());
                    })
                    .forEach(searchProducer::createSearchEntry);
        }
        return ResponseDTO.<String>builder()
                .status(Boolean.TRUE)
                .message("Request processed successfully")
                .data(null)
                .build();
    }

}
