package com.example.productservice;

import com.example.productservice.entity.Product;
import com.example.productservice.enums.*;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.UUID;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase{

    @Autowired
    private ProductRepository productRepository;

    private static Product savedProduct;
    @BeforeEach
    public void setUpDb(){
        productRepository.deleteAll();
        Product product =
                Product
                .builder()
                        .id("TestId")
                        .brand("Royal Kludge")
                        .category(Category.FULLSIZE)
                        .connectivity(Connectivity.BLUETOOTH)
                        .skuCode(UUID.randomUUID().toString())
                        .keyCaps(KeyCaps.OEM)
                        .model("RK61")
                        .layout(Layout.QWERTY)
                        .keySwitches(Switches.GYELLOW)
                        .price(4500.00f)
                        .description("Test Product")
                        .dimension("1.5x1.5x2 inches")
                        .weight(358.6f)
                .build();

        savedProduct = productRepository.save(product);
        assertEquals(savedProduct,product);
    }

    @Test
    void create(){
        Product newProduct =
                Product
                        .builder()
                        .model("TST123")
                        .brand("Test Containers")
                        .keySwitches(Switches.GBLUE)
                        .description("TACTILE")
                        .build();
        productRepository.save(newProduct);

        Optional<Product> foundProduct = productRepository.findById(newProduct.getId());
        assertEquals(newProduct,foundProduct.get());
    }

    @Test
    void update(){
        savedProduct.setBrand("RK71");
        productRepository.save(savedProduct);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertEquals("RK71", foundProduct.get().getBrand());
    }

    @Test
    void delete() {
        productRepository.delete(savedProduct);
        assertFalse(productRepository.existsById(savedProduct.getId()));
    }

    @Test
    void getByProductId() {
        Optional<Product> product =
                productRepository.findById(savedProduct.getId());
        assertTrue(product.isPresent());
        assertEquals(savedProduct, product.get());
    }

}

