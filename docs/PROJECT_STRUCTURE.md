online-mart/
├── pom.xml                          ← Parent POM
├── api-gateway/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/onlinemart/gateway/
│       │   └── ApiGatewayApplication.java
│       └── resources/
│           └── application.properties
├── product-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/onlinemart/product/
│       │   ├── ProductServiceApplication.java
│       │   ├── controller/
│       │   │   └── ProductController.java
│       │   ├── service/
│       │   │   ├── ProductService.java
│       │   │   └── ProductServiceImpl.java
│       │   ├── repository/
│       │   │   └── ProductRepository.java
│       │   └── model/
│       │       └── Product.java
│       └── resources/
│           └── application.properties
├── order-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/onlinemart/order/
│       │   ├── OrderServiceApplication.java
│       │   ├── controller/
│       │   │   └── OrderController.java
│       │   ├── service/
│       │   │   ├── OrderService.java
│       │   │   └── OrderServiceImpl.java
│       │   ├── repository/
│       │   │   └── OrderRepository.java
│       │   ├── client/
│       │   │   ├── ProductClient.java   ← Feign: calls Product Service
│       │   │   └── CartClient.java      ← Feign: calls Cart Service
│       │   └── model/
│       │       └── Order.java
│       └── resources/
│           └── application.properties
└── cart-service/
├── pom.xml
└── src/main/
├── java/com/onlinemart/cart/
│   ├── CartServiceApplication.java
│   ├── controller/
│   │   └── CartController.java
│   ├── service/
│   │   ├── CartService.java
│   │   └── CartServiceImpl.java
│   ├── repository/
│   │   └── CartRepository.java
│   ├── client/
│   │   └── ProductClient.java   ← Feign: validates product exists
│   └── model/
│       └── Cart.java
└── resources/
└── application.properties