online-mart/
├── api-gateway/
│
├── product-service/
│   └── src/main/java/com/onlinemart/product/
│       ├── controller/
│       │   └── ProductController.java
│       │
│       ├── service/
│       │   ├── ProductService.java
│       │   └── ProductServiceImpl.java
│       │
│       ├── repository/
│       │   └── ProductRepository.java
│       │
│       ├── entity/
│       │   └── Product.java
│       │
│       ├── dto/
│       │   ├── request/
│       │   │   ├── CreateProductRequest.java
│       │   │   └── UpdateProductRequest.java
│       │   │
│       │   └── response/
│       │       ├── ProductResponse.java
│       │       └── ProductListResponse.java
│       │
│       └── mapper/
│           └── ProductMapper.java
│
├── order-service/
│   └── src/main/java/com/onlinemart/order/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── client/
│       │
│       ├── entity/
│       │   └── Order.java
│       │
│       ├── dto/
│       │   ├── request/
│       │   │   ├── CreateOrderRequest.java
│       │   │   └── UpdateOrderStatusRequest.java
│       │   │
│       │   └── response/
│       │       ├── OrderResponse.java
│       │       └── OrderSummaryResponse.java
│       │
│       └── mapper/
│           └── OrderMapper.java
│
├── cart-service/
│   └── src/main/java/com/onlinemart/cart/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── client/
│       │
│       ├── entity/
│       │   └── Cart.java
│       │
│       ├── dto/
│       │   ├── request/
│       │   │   ├── AddCartItemRequest.java
│       │   │   └── UpdateCartItemRequest.java
│       │   │
│       │   └── response/
│       │       ├── CartResponse.java
│       │       └── CartItemResponse.java
│       │
│       └── mapper/
│           └── CartMapper.java
│
└── common/ (optional later)
├── exception/
├── constants/
└── util/