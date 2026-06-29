# E-Commerce Platform API Contract

## Version

| Version | Date       | Author | Remarks       |
| ------- | ---------- | ------ | ------------- |
| 1.0     | 2026-06-22 | Team   | Initial Draft |

---

# 1. Overview

This document defines the REST API contract for the E-Commerce Platform.

The platform follows a cloud-native microservices architecture and is composed of the following services:

| Service         | Responsibility                               |
| --------------- | -------------------------------------------- |
| Cart Service    | Manage shopping carts and cart items         |
| Order Service   | Manage customer orders and order lifecycle   |
| Product Service | Product catalog and inventory (Future Scope) |

---

# 2. Base URLs

## Development

```text
http://localhost:8081/api/v1/carts
http://localhost:8082/api/v1/orders
```

## Production

```text
placeholder if at all any in future
```

---

# 3. Common Request Headers

| Header           | Required | Description                       |
| ---------------- | -------- | --------------------------------- |
| Content-Type     | Yes      | application/json                  |
| Accept           | Yes      | application/json                  |

Example:

```http
Content-Type: application/json
Accept: application/json
```

---

# 4. Standard Response Format

## Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

## Error Response

```json
{
  "success": false,
  "message": "Cart not found",
  "errorCode": "CART_404"
}
```
---


# 5. Product Service

## Purpose

Responsible for product discovery, product details, category filtering, and inventory availability.

---

## POST /products

### Endpoint

```http
POST /products
```

### Request
```json
{
  "productName": "Dell Inspiron 15",
  "categoryId": "CAT001",
  "description": "15 inch laptop with Intel i7 processor",
  "price": 55000,
  "availableQuantity": 15,
  "thumbnailUrl": "/images/products/prod001.jpg"
}
```

### Response

```json
{
  "success": true,
  "message": "Products created successfully",
  "data": {
    "categoryId": "CAT001",
    "productId": "PROD001",
    "productName": "Dell Inspiron 15",
    "price": 55000,
    "availableQuantity": 15,
    "thumbnailUrl": "/images/products/prod001.jpg",
    "status": true,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
}
```

## PUT /products

### Endpoint

```http
POST /products/{productId}
```

### Request
```json
{
  "productName": "Dell Inspiron 15",
  "categoryId": "CAT001",
  "description": "15 inch laptop with Intel i7 processor",
  "price": 55000,
  "availableQuantity": 15,
  "thumbnailUrl": "/images/products/prod001.jpg"
}
```

### Response

```json
{
  "success": true,
  "message": "Products updated successfully",
  "data": {
    "categoryId": "CAT001",
    "productId": "PROD001",
    "productName": "Dell Inspiron 15",
    "price": 55000,
    "availableQuantity": 15,
    "thumbnailUrl": "/images/products/prod001.jpg",
    "status": true,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
}
```

## Search Products

### Endpoint

```http
POST /products/discovery
```

### Request

```json
{
  "page": 0,
  "size": 20,
  "limit": 20,
  "filters": [
    {
      "categoryId": "CAT001"
    },
    {
      "inStock": true
    }
  ],
  "sort": [
    {
      "price": "asc"
    }
  ]
}
```

### Response

```json
{
  "success": true,
  "message": "Products fetched successfully",
  "data": [
    {
      "categoryId": "CAT001",
      "productId": "PROD001",
      "productName": "Dell Inspiron 15",
      "price": 55000,
      "availableQuantity": 15,
      "thumbnailUrl": "/images/products/prod001.jpg",
      "status": true,
      "createdBy": "CUST001",
      "createdAt": "2026-06-22T10:00:00Z",
      "updatedBy": "CUST001",
      "updatedAt": "2026-06-22T10:00:00Z",
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "limit": 20,
    "totalElements": 1
  }
}
```

---

## Get Product Details

### Endpoint

```http
GET /products/{productId}
```

### Response

```json
{
  "success": true,
  "message": "Product fetched successfully",
  "data": {
    "categoryId": "CAT001",
    "productId": "PROD001",
    "productName": "Dell Inspiron 15",
    "price": 55000,
    "availableQuantity": 15,
    "thumbnailUrl": "/images/products/prod001.jpg",
    "status": true,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z"
  }
}
```

---

## Check Product Availability

### Endpoint

```http
GET /products/{productId}/availability
```

### Response

```json
{
  "success": true,
  "message": "Inventory fetched successfully",
  "data": {
    "productId": "PROD001",
    "isAvailable": true,
    "availableQuantity": 15
  }
}
```

---

## Get Categories

### Endpoint

```http
GET /products/category
```

### Response

```json
{
  "success": true,
  "message": "Categories fetched successfully",
  "data": [
    {
      "categoryId": "CAT001",
      "categoryName": "Laptop",
      "status": true,
      "createdBy": "SYSTEM",
      "createdAt": "2026-06-22T10:00:00Z",
      "updatedBy": "SYSTEM",
      "updatedAt": "2026-06-22T10:00:00Z"
    },
    {
      "categoryId": "CAT002",
      "categoryName": "Mobile",
      "status": true,
      "createdBy": "SYSTEM",
      "createdAt": "2026-06-22T10:00:00Z",
      "updatedBy": "SYSTEM",
      "updatedAt": "2026-06-22T10:00:00Z"
    }
  ]
}
```

---

## Get Category Details

### Endpoint

```http
GET /products/category/{categoryId}
```

### Response

```json
{
  "success": true,
  "message": "Category fetched successfully",
  "data": {
    "categoryId": "CAT001",
    "categoryName": "Laptop",
    "status": true,
    "createdBy": "SYSTEM",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "SYSTEM",
    "updatedAt": "2026-06-22T10:00:00Z"
  }
}
```

---

## Product DTO

```json
{
  "productId": "PROD001",
  "sku": "DELL-INS-15",
  "productName": "Dell Inspiron 15",
  "categoryId": "CAT001",
  "categoryName": "Laptop",
  "description": "15 inch laptop with Intel i7 processor",
  "price": 55000,
  "availableQuantity": 15,
  "thumbnailUrl": "/images/products/prod001.jpg",
  "status": true,
  "createdBy": "SYSTEM",
  "createdAt": "2026-06-22T10:00:00Z",
  "updatedBy": "SYSTEM",
  "updatedAt": "2026-06-22T10:00:00Z"
}
```

---

## Category DTO

```json
{
  "categoryId": "CAT001",
  "categoryName": "Laptop",
  "description": "Laptop and notebook computers",
  "status": true,
  "createdBy": "SYSTEM",
  "createdAt": "2026-06-22T10:00:00Z",
  "updatedBy": "SYSTEM",
  "updatedAt": "2026-06-22T10:00:00Z"
}
```

---


# 6. Cart Service

## Purpose

Manages customer shopping carts.

---

## Create Cart

### Endpoint

```http
POST /carts
```

### Request

```json
{
  "customerId": "CUST001",
  "platform": "MAIN"
}
```

### Response

```json
{ 
  "cartId": "CART001", 
  "customerId": "CUST001",
  "platform": "MAIN",
  "status": true,
  "items": [],
  "createdBy": "CUST001",
  "createdAt": "2026-06-22T10:00:00Z",
  "updatedBy": "CUST001",
  "updatedAt": "2026-06-22T10:00:00Z",
}
```

---

## Get Customer Cart

### Endpoint

```http
GET /carts/customer/{customerId}
```

### Response

```json
[
  {
    "cartId": "CART001",
    "customerId": "CUST001",
    "platform": "MAIN",
    "totalAmount": 0,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
]
```

---

## Add Item to Cart

### Endpoint

```http
POST /carts/{cartId}/items
```

### Request

#### Success Response (201 Created)

```json
{
  "productId": "PROD001",
  "quantity": 2
}
```

### Response

```json
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "cartId": "CART001",
    "customerId": "CUST001",
    "platform": "MAIN",
    "status": true,
    "items": [
      {
        "itemId": "ITEM001",
        "productId": "PROD001",
        "quantity": 2,
        "unitPrice": 1200
      }
    ],
    "cartTotal": 2400,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z"
  }
}
```

---

## Update Cart Item Quantity

### Endpoint

```http
PUT /carts/{cartId}/items/{itemId}
```

### Request

```json
{
  "quantity": 5
}
```

### Response

```json
{
  "success": true,
  "message": "Item updated successfully",
  "data": {
    "cartId": "CART001",
    "customerId": "CUST001",
    "platform": "MAIN",
    "status": true,
    "items": [
      {
        "itemId": "ITEM001",
        "productId": "PROD001",
        "quantity": 5,
        "unitPrice": 1200,
        "totalPrice": 2400
      }
    ],
    "cartTotal": 2400,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
}
```

---

## Remove Cart Item

### Endpoint

```http
DELETE /carts/{cartId}/items/{itemId}
```

### Response (204 No Content)

---

## Clear Cart

### Endpoint

```http
DELETE /carts/{cartId}/items
```

### Response (204 No Content)

---

## Get Cart Details

### Endpoint

```http
GET /carts/{cartId}
```

### Response

```json
{
  "cartId": "CART001",
  "customerId": "CUST001",
  "platform": "MAIN",
  "status": true,
  "items": [
    {
      "itemId": "ITEM001",
      "productId": "PROD001",
      "quantity": 2,
      "unitPrice": 1200,
      "totalPrice": 2400
    }
  ],
  "cartTotal": 2400,
  "createdBy": "CUST001",
  "createdAt": "2026-06-22T10:00:00Z",
  "updatedBy": "CUST001",
  "updatedAt": "2026-06-22T10:00:00Z",
}
```

---

# 7. Order Service

## Purpose

Responsible for order creation, status tracking, and order history.

---

## Place Order

### Endpoint

```http
POST /orders
```

### Request

```json
{
  "cartId": "CART001",
  "customerId": "CUST001",
  "items": [
    {
      "productId": "PROD001",
      "quantity": 2,
      "unitPrice": 1200
    }
  ]
}
```

### Response

```json
{
  "status": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "ORD001",
    "customerId": "CUST001",
    "cartId": "CART001",
    "status": "PLACED",
    "items": [
      {
        "itemId": "ITEM001",
        "productId": "PROD001",
        "quantity": 2,
        "unitPrice": 1200  
      }
    ],
    "totalPrice": 2400,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
}
```

---

## Get Order Details

### Endpoint

```http
GET /orders/{orderId}
```

### Response

```json
{
  "status": true,
  "message": "Order fetched successfully",
  "data": {
    "orderId": "ORD001",
    "customerId": "CUST001",
    "cartId": "CART001",
    "status": "PLACED",
    "items": [
      {
        "itemId": "ITEM001",
        "productId": "PROD001",
        "quantity": 2,
        "unitPrice": 1200  
      }
    ],
    "totalPrice": 2400,
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T10:00:00Z",
  }
}
```

---

## Get Customer Order

### Endpoint

```http
POST /orders/customer/{customerId}
```

### Request Parameters

```json
{
  "page": 0,
  "size": 20,
  "limit": 10,
  "filters": [
    { "status": "SHIPPED" }
  ],
  "sort": [
    { "createdAt": "desc" }
  ]
}
```

### Response

```json
{
  "success": true,
  "message": "Order feched successfully",
  "data": [
    {
      "orderId": "ORD001",
      "customerId": "CUST001",
      "cartId": "CART001",
      "status": "SHIPPED",
      "createdBy": "CUST001",
      "createdAt": "2026-06-22T10:00:00Z",
      "updatedBy": "CUST001",
      "updatedAt": "2026-06-22T10:00:00Z",
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "limit": 10,
    "totalElements": 1,
    "filters": [
      { "status": "SHIPPED" }
    ],
    "sort": [
      { "createdAt": "desc" }
    ]
  }
}
```

---

## Update Order Status

### Endpoint

```http
PATCH /orders/{orderId}/status
```

### Request

```json
{
  "status": "CANCELLED"
}
```

### Response

```json
{
  "status": true,
  "message": "Order cancelled successfully",
  "data": {
    "orderId": "ORD001",
    "customerId": "CUST001",
    "cartId": "CART001",
    "status": "CANCELLED",
    "createdBy": "CUST001",
    "createdAt": "2026-06-22T10:00:00Z",
    "updatedBy": "CUST001",
    "updatedAt": "2026-06-22T12:00:00Z",
  }
}
```

---

# 8. Order Status Lifecycle

```text
PENDING
  ↓
PLACED
  ↓
CONFIRMED
  ↓
PACKED
  ↓
SHIPPED
  ↓
DELIVERED
```

Alternative Flow

```text
PLACED
  ↓
CANCELLED
```

---

# 9. DTO Definitions

## Cart Item

```json
{
  "itemId": "ITEM001",
  "productId": "PROD001",
  "productName": "Laptop",
  "quantity": 2,
  "unitPrice": 1200,
  "totalPrice": 2400
}
```

---

## Address

```json
{
  "line1": "123 Main Street",
  "line2": "Apartment 10",
  "city": "Bangalore",
  "state": "Karnataka",
  "postalCode": "560001",
  "country": "India"
}
```

---

## Order Item

```json
{
  "productId": "PROD001",
  "productName": "Laptop",
  "quantity": 2,
  "unitPrice": 1200,
  "totalPrice": 2400
}
```

---

# 10. HTTP Status Codes

| Status | Description           |
| ------ | --------------------- |
| 200    | Success               |
| 201    | Created               |
| 204    | No Content            |
| 400    | Bad Request           |
| 401    | Unauthorized          |
| 403    | Forbidden             |
| 404    | Resource Not Found    |
| 409    | Conflict              |
| 422    | Validation Failure    |
| 500    | Internal Server Error |

---

# 11. Future Scope

The following capabilities will be added in future releases:

### Product Service

* Product Search
* Product Details
* Product Categories
* Inventory Availability
* Product Reviews
* Product Recommendations

### Additional Services

* Customer Service
* Payment Service
* Inventory Service
* Service Discovery
* Event-Driven Messaging

---

# 12. API Versioning Strategy

All APIs will follow URI-based versioning.

Example:

```http
/api/v1/carts
/api/v1/orders
```

Future versions:

```http
/api/v2/carts
/api/v2/orders
```
