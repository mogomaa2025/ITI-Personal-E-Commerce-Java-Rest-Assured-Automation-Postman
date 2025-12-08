# ITI E-Commerce API Documentation

## Overview

This document provides comprehensive documentation for the ITI E-Commerce API, covering all endpoints, request/response formats, authentication requirements, and usage examples. The API provides full e-commerce functionality including user management, product catalog, cart operations, order processing, and administrative features.

- **Base URL**: `https://itigraduation.pythonanywhere.com/api` (or your configured `{{base_url}}`)
- **API Version**: v33
- **Content-Type**: `application/json`
- **Authentication**: JWT Bearer tokens

## Authentication

The API uses JWT (JSON Web Tokens) for authentication. There are two types of tokens:

- **User Token**: For regular user operations
- **Admin Token**: For administrative operations

### Authentication Endpoints

#### POST /login
**Description**: Authenticate user and receive access/refresh tokens

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "user_password"
}
```

**Success Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 52,
    "email": "user@example.com",
    "name": "John Doe",
    "is_admin": false
  }
}
```

**Headers**:
- Content-Type: `application/json`

#### POST /register
**Description**: Register a new user account

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "secure_password",
  "name": "New User"
}
```

#### POST /refresh
**Description**: Refresh access token using refresh token

**Request Body**:
```json
{
  "refresh_token": "current_refresh_token"
}
```

## API Endpoints

### Products

#### GET /products
**Description**: List all products with pagination support

**Query Parameters**:
- `page` (optional): Page number (default: 1)
- `per_page` (optional): Items per page (default: 10)
- `category` (optional): Filter by category
- `search` (optional): Search term
- `min_price` (optional): Minimum price filter
- `max_price` (optional): Maximum price filter

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Product Name",
      "description": "Product description",
      "price": 29.99,
      "stock": 100,
      "category": "Electronics",
      "image_url": "https://example.com/image.jpg",
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-01T00:00:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "pages": 5,
    "per_page": 10,
    "total": 50
  }
}
```

#### GET /products/{{product_id}}
**Description**: Get a specific product by ID

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Product Name",
    "description": "Product description",
    "price": 29.99,
    "stock": 100,
    "category": "Electronics",
    "image_url": "https://example.com/image.jpg",
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

#### POST /products
**Description**: Create a new product (Admin only)
**Authentication**: Requires admin token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{admin_token}}`

**Request Body**:
```json
{
  "name": "New Product",
  "description": "Product description",
  "price": 29.99,
  "category": "Electronics",
  "stock": 20,
  "image_url": "https://example.com/image.jpg"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 123,
    "name": "New Product",
    "description": "Product description",
    "price": 29.99,
    "category": "Electronics",
    "stock": 20,
    "image_url": "https://example.com/image.jpg"
  }
}
```

#### PUT /products/{{product_id}}
**Description**: Update an existing product (Admin only)
**Authentication**: Requires admin token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{admin_token}}`

#### DELETE /products/{{product_id}}
**Description**: Delete a product (Admin only)
**Authentication**: Requires admin token

**Headers**:
- Authorization: `Bearer {{admin_token}}`

### Categories

#### GET /categories
**Description**: List all categories

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and accessories"
    }
  ]
}
```

#### POST /categories
**Description**: Create a new category (Admin only)
**Authentication**: Requires admin token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{admin_token}}`

**Request Body**:
```json
{
  "name": "New Category",
  "description": "Category description"
}
```

#### DELETE /categories/{{category_id}}
**Description**: Delete a category (Admin only)
**Authentication**: Requires admin token

### Users

#### GET /users
**Description**: List all users (Admin only)
**Authentication**: Requires admin token

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe",
      "is_admin": false,
      "created_at": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### GET /users/{{user_id}}
**Description**: Get a specific user (Admin only)
**Authentication**: Requires admin token

#### PUT /users/{{user_id}}
**Description**: Update user information
**Authentication**: Requires user's own token or admin token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}` or `{{admin_token}}`

#### DELETE /users/{{user_id}}
**Description**: Delete a user (Admin only)
**Authentication**: Requires admin token

### Cart

#### GET /cart
**Description**: Get the current user's cart
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "product_id": 123,
        "product_name": "Product Name",
        "price": 29.99,
        "quantity": 2,
        "subtotal": 59.98
      }
    ],
    "total_items": 2,
    "total_amount": 59.98
  }
}
```

#### POST /cart/items
**Description**: Add an item to the cart
**Authentication**: Requires user token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123,
  "quantity": 1
}
```

#### PUT /cart/items/{{cart_item_id}}
**Description**: Update cart item quantity
**Authentication**: Requires user token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}`

**Request Body**:
```json
{
  "quantity": 3
}
```

#### DELETE /cart/items/{{cart_item_id}}
**Description**: Remove an item from the cart
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

#### DELETE /cart
**Description**: Clear the entire cart
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

### Orders

#### GET /orders
**Description**: List user's orders
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

#### GET /orders/{{order_id}}
**Description**: Get a specific order
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

#### POST /orders
**Description**: Create a new order
**Authentication**: Requires user token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}`

**Request Body**:
```json
{
  "shipping_address": "123 Test Street"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 123,
    "user_id": 456,
    "shipping_address": "123 Test Street",
    "status": "pending",
    "total_amount": 59.98,
    "items": [
      {
        "product_id": 123,
        "product_name": "Product Name",
        "price": 29.99,
        "quantity": 2,
        "subtotal": 59.98
      }
    ]
  }
}
```

#### PUT /orders/{{order_id}}
**Description**: Update an order (User)
**Authentication**: Requires user token

#### PUT /orders/{{order_id}}/status
**Description**: Update order status (Admin only)
**Authentication**: Requires admin token

### Reviews

#### POST /reviews
**Description**: Create a product review
**Authentication**: Requires user token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123,
  "rating": 5,
  "comment": "Great product!"
}
```

#### GET /products/{{product_id}}/reviews
**Description**: Get reviews for a specific product

### Wishlist

#### GET /wishlist
**Description**: Get user's wishlist
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

#### POST /wishlist
**Description**: Add an item to wishlist
**Authentication**: Requires user token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123
}
```

#### DELETE /wishlist/{{wishlist_item_id}}
**Description**: Remove an item from wishlist
**Authentication**: Requires user token

**Headers**:
- Authorization: `Bearer {{user_token}}`

### Coupons

#### GET /coupons
**Description**: List available coupons (Admin only)
**Authentication**: Requires admin token

#### POST /coupons
**Description**: Create a new coupon (Admin only)
**Authentication**: Requires admin token

**Headers**:
- Content-Type: `application/json`
- Authorization: `Bearer {{admin_token}}`

#### POST /coupons/validate
**Description**: Validate a coupon code

**Headers**:
- Content-Type: `application/json`

**Request Body**:
```json
{
  "code": "COUPON_CODE"
}
```

### System Endpoints

#### GET /health
**Description**: Health check endpoint

**Response (200 OK)**:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

#### GET /system/health
**Description**: Detailed system health information

#### GET /docs
**Description**: API documentation endpoint

## Authentication Requirements

- **Public Endpoints**: No authentication required
  - GET /products
  - GET /products/{{product_id}}
  - GET /categories
  - POST /login
  - POST /register
  - POST /coupons/validate

- **User Authentication Required**: User token required
  - GET /users/{{user_id}}
  - PUT /users/{{user_id}}
  - GET /cart
  - POST /cart/items
  - PUT /cart/items/{{cart_item_id}}
  - DELETE /cart/items/{{cart_item_id}}
  - DELETE /cart
  - GET /orders
  - GET /orders/{{order_id}}
  - POST /orders
  - PUT /orders/{{order_id}}
  - GET /wishlist
  - POST /wishlist
  - DELETE /wishlist/{{wishlist_item_id}}
  - POST /reviews
  - GET /products/{{product_id}}/reviews
  - GET /products/{{product_id}}/reviews/check

- **Admin Authentication Required**: Admin token required
  - GET /users
  - POST /products
  - PUT /products/{{product_id}}
  - DELETE /products/{{product_id}}
  - POST /categories
  - DELETE /categories/{{category_id}}
  - DELETE /users/{{user_id}}
  - GET /stats
  - GET /analytics/dashboard
  - GET /analytics/reports/sales
  - GET /export/products
  - GET /export/orders
  - PUT /orders/{{order_id}}/status
  - POST /coupons
  - GET /coupons
  - POST /help
  - PUT /help/{{help_id}}
  - POST /notifications/test-create
  - GET /contact/messages
  - POST /contact/messages/{{contact_message_id}}/respond

## Error Responses

The API returns consistent error responses in the following format:

**400 Bad Request**:
```json
{
  "success": false,
  "error": "Error message explaining the issue"
}
```

**401 Unauthorized**:
```json
{
  "success": false,
  "error": "Authentication required"
}
```

**403 Forbidden**:
```json
{
  "success": false,
  "error": "Access denied"
}
```

**404 Not Found**:
```json
{
  "success": false,
  "error": "Resource not found"
}
```

**500 Internal Server Error**:
```json
{
  "success": false,
  "error": "Internal server error"
}
```

## Collection Variables

The Postman collection uses the following variables:

- `{{base_url}}`: API base URL (e.g., `https://itigraduation.pythonanywhere.com/api`)
- `{{admin_token}}`: Admin user JWT token
- `{{user_token}}`: Regular user JWT token
- `{{refresh_token}}`: User refresh token
- `{{refresh_admin_token}}`: Admin refresh token
- `{{product_id}}`: Current product ID for testing
- `{{user_id}}`: Current user ID for testing
- `{{order_id}}`: Current order ID for testing
- `{{category_id}}`: Current category ID for testing
- `{{cart_item_id}}`: Current cart item ID for testing
- `{{wishlist_item_id}}`: Current wishlist item ID for testing
- `{{like_id}}`: Current like ID for testing

## Testing Considerations

1. **Setup**: Ensure proper tokens are set in collection variables before running requests
2. **Order**: Some requests depend on others (e.g., creating a product before updating it)
3. **Cleanup**: Consider cleanup requests to reset test data between test runs
4. **Authentication**: Different endpoints require different authentication levels
5. **Validation**: Each request includes comprehensive response validation in its test script

## Rate Limiting

The API may implement rate limiting. If you encounter 429 status codes, implement appropriate retry logic with exponential backoff.

## Best Practices

1. Always include proper error handling in your client applications
2. Use refresh tokens to maintain user sessions
3. Validate all inputs before sending requests to the API
4. Handle different response status codes appropriately
5. Use HTTPS in production environments