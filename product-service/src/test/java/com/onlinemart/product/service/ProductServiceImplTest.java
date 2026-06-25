// package com.onlinemart.product.service;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)          // Mockito only, no Spring
// class ProductServiceImplTest {

//     @Mock
//     ProductRepository productRepository;     // Fake repo — no real DB

//     @InjectMocks
//     ProductServiceImpl productService;       // Real class being tested

//     @Test
//     void getProductById_ShouldReturnProduct_WhenExists() {
//         // ARRANGE — set up fake data
//         Product fakeProduct = new Product(1L, "Laptop", 999.99, 10);
//         when(productRepository.findById(1L))
//                 .thenReturn(Optional.of(fakeProduct));

//         // ACT — call the real method
//         Product result = productService.getProductById(1L);

//         // ASSERT — verify output
//         assertNotNull(result);
//         assertEquals("Laptop", result.getName());
//         assertEquals(999.99, result.getPrice());

//         // VERIFY — repo was actually called once
//         verify(productRepository, times(1)).findById(1L);
//     }

//     @Test
//     void getProductById_ShouldThrowException_WhenNotFound() {
//         // ARRANGE
//         when(productRepository.findById(99L))
//                 .thenReturn(Optional.empty());

//         // ACT + ASSERT — expect exception
//         assertThrows(ProductNotFoundException.class, () -> {
//             productService.getProductById(99L);
//         });
//     }
// }