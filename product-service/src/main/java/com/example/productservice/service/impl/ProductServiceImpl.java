package com.example.productservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
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
import java.util.stream.IntStream;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final Faker faker;
    private final MongoTemplate mongoTemplate;

    // Used for testing resiliency
    private static final int FAULT_PERCENT = 50;

    @Autowired
    public ProductServiceImpl(ProductMapper productMapper, ProductRepository productRepository, MongoTemplate mongoTemplate) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.faker = new Faker();
    }

    @Override
    public ResponseDTO<ConfigureProductDTO> getProductById(String productId) {
        LOG.info(">>> getProductById");
        // For testing resiliency
        int randomThreshold = RandomGenerator.getDefault().nextInt(1, 100);
        if (FAULT_PERCENT < randomThreshold) {
            LOG.info("Bad luck, an error occurred, {} >= {}", FAULT_PERCENT, randomThreshold);
            throw new UnsupportedOperationException("Something went wrong...[RESILIENCY TESTING]");
        }

        ResponseDTO<ConfigureProductDTO> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Request processed successfully.", null);
        productRepository.findById(productId)
                .ifPresentOrElse(product -> {
                    LOG.debug("Fetching product({}).", productId);
                    ConfigureProductDTO productDTO = productMapper.toDto(product);
                    responseDTO.setData(productDTO);
                }, () -> {
                    LOG.debug("Associated product({}) not found.", productId);
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("No product with the associated productId present.");
                });
        LOG.info("<<< getProductById");
        return responseDTO;
    }

    @Override
    public ResponseDTO<String> createProduct(ConfigureProductDTO productDTO) {
        LOG.info("received createProduct request");
        ResponseDTO<String> responseDTO =
                new ResponseDTO<>(Boolean.TRUE, "Product successfully created.", null);
        Optional.ofNullable(productDTO)
                .filter(product ->
                        !productRepository.existsByBrandNameAndModelNumberAndConnectivityAndSwitchesAndCategory(
                                product.brandName(),
                                product.modelNumber(),
                                product.connectivity(),
                                product.switches(),
                                product.category()
                        ))
                .map(productMapper::toEntity)
                .ifPresentOrElse(entity -> {
                    entity.setSkuCode(UUID.randomUUID().toString().substring(0,6));
                    LOG.debug("Creating product ({}).", entity.getSkuCode());
                    productRepository.save(entity);
                }, () -> {
                    LOG.debug("Unable to create product ({},{},{},{},{}).",
                            productDTO.brandName(),
                            productDTO.modelNumber(),
                            productDTO.connectivity(),
                            productDTO.switches(),
                            productDTO.category()
                    );
                    responseDTO.setStatus(Boolean.FALSE);
                    responseDTO.setMessage("Invalid Request or Duplicate Product");
                });
        LOG.info("finished processing createProduct request");
        return responseDTO;
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
        List<ConfigureProductDTO> productList = productMapper.toDtoList(queryResult);
        return PageableExecutionUtils.getPage(productList, pageable,
                ()-> mongoTemplate.count(dynamicQuery, Product.class));
    }

    /*
    creates criteria based on input fields and their values, and adds them to the provided query (givenQuery)
    */
    private void addCriteriaToQuery(FilterProductDTO filterProductDTO, Query givenQuery){
        LOG.info("constructing queries for filtering.");
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
        if(Objects.nonNull(filterProductDTO.sortType())){
            Sort sort;
            if (Objects.requireNonNull(filterProductDTO.sortType()) == SortType.PRICE_DESC) {
                sort = Sort.by(Sort.Direction.DESC, "price");
            } else {
                sort = Sort.by(Sort.Direction.ASC, "price");
            }
            givenQuery.with(sort);
        }

        if(!CollectionUtils.isEmpty(andCriteriaList)){
            givenQuery.addCriteria(new Criteria().andOperator(andCriteriaList));
        }
        LOG.info("finished constructing queries.");
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

//    @Override
//    @Transactional
//    public ResponseDTO<String> insertDummyData() {
//        ResponseDTO<String> responseDTO = new ResponseDTO<>(Boolean.TRUE,"Data insertion complete.",null);
//        generateDummyReviews();
//        return responseDTO;
//    }

//    private void generateDummyProducts(){
//        if(productRepository.findAll().isEmpty()){
//            List<Product> reviewList = IntStream.rangeClosed(1,100)
//                    .mapToObj(i->
//                            new Product(
//                                    String.valueOf(i),
//                                    UUID.randomUUID().toString(),
//                                    faker.commerce().brand(),
//                                    faker.bothify("???###"),
//                                    faker.options().option(Connectivity.class),
//                                    faker.options().option(Switches.class),
//                                    faker.options().option(KeyCaps.class),
//                                    faker.options().option(Layout.class),
//                                    faker.options().option(Category.class),
//                                    faker.lorem().sentence(),
//                                    faker.bothify("#.#x#.#x#.#"),
//                                    faker.random().nextDouble(300,500),
//                                    faker.random().nextDouble(4000,15000),
//                                    faker.internet().url()
//                            ))
//                    .toList();
//            productRepository.saveAll(reviewList);
//        }
//    }

}
