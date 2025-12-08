# ITI E-Commerce API Reference Guide

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Examples](#requestresponse-examples)
5. [Error Handling](#error-handling)
6. [Testing Guidelines](#testing-guidelines)

## Overview

The ITI E-Commerce API is a comprehensive RESTful API that provides full e-commerce functionality including user management, product catalog, shopping cart, order processing, and administrative features.

**Base URL**: `https://itigraduation.pythonanywhere.com/api`
**API Version**: v33
**Content Type**: `application/json`

### Key Features
- Full CRUD operations for products, categories, and users
- Shopping cart and wishlist functionality
- Order management system
- Product reviews and ratings
- Coupon management
- Administrative dashboard and analytics
- JWT-based authentication and authorization

## Authentication

The API uses JWT (JSON Web Tokens) for authentication and authorization. There are two user types:
- Regular users (customers)
- Admin users (with extended permissions)

### Token Management
- Access tokens expire after a set period (typically 15 minutes)
- Refresh tokens are long-lived (typically 30 days)
- Use the `/refresh` endpoint to get a new access token

### Authorization Headers
All authenticated requests must include the Authorization header:
```
Authorization: Bearer <token>
```

## API Endpoints

### Products

#### GET /products
- **Description**: Retrieve a paginated list of products
- **Authentication**: Optional (public endpoint)
- **Query Parameters**:
  - `page` (integer): Page number (default: 1)
  - `per_page` (integer): Items per page (default: 10, max: 100)
  - `category` (string): Filter by category
  - `search` (string): Search in product names/descriptions
  - `min_price` (number): Minimum price filter
  - `max_price` (number): Maximum price filter
  - `min_rating` (number): Minimum rating filter (1-5)
  - `sort_by` (string): Sort by field (name, price, created_at, rating)
  - `sort_order` (string): Sort order (asc, desc)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Wireless Headphones",
      "description": "High-quality wireless headphones",
      "price": 99.99,
      "stock": 50,
      "category": "Electronics",
      "image_url": "https://example.com/image.jpg",
      "rating": 4.5,
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-02T00:00:00Z"
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
- **Description**: Retrieve a specific product by ID
- **Authentication**: Optional (public endpoint)
- **Path Parameters**: `product_id` (integer)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Wireless Headphones",
    "description": "High-quality wireless headphones",
    "price": 99.99,
    "stock": 50,
    "category": "Electronics",
    "image_url": "https://example.com/image.jpg",
    "rating": 4.5,
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-02T00:00:00Z"
  }
}
```

#### POST /products
- **Description**: Create a new product
- **Authentication**: Admin only
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{admin_token}}`

**Request Body**:
```json
{
  "name": "New Product",
  "description": "Product description",
  "price": 29.99,
  "category": "Electronics",
  "stock": 100,
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
    "stock": 100,
    "image_url": "https://example.com/image.jpg",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

#### PUT /products/{{product_id}}
- **Description**: Update an existing product
- **Authentication**: Admin only
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{admin_token}}`

**Request Body** (all fields optional):
```json
{
  "name": "Updated Product Name",
  "price": 39.99,
  "stock": 150
}
```

#### DELETE /products/{{product_id}}
- **Description**: Delete a product
- **Authentication**: Admin only
- **Headers**: `Authorization: Bearer {{admin_token}}`

### Categories

#### GET /categories
- **Description**: Retrieve all product categories
- **Authentication**: Optional (public endpoint)

**Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and accessories"
    },
    {
      "id": 2,
      "name": "Clothing",
      "description": "Apparel and fashion items"
    }
  ]
}
```

#### POST /categories
- **Description**: Create a new category
- **Authentication**: Admin only
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{admin_token}}`

**Request Body**:
```json
{
  "name": "Books",
  "description": "Books and educational materials"
}
```

### Users

#### GET /users
- **Description**: Retrieve all users (admin only)
- **Authentication**: Admin only
- **Headers**: `Authorization: Bearer {{admin_token}}`

#### GET /users/{{user_id}}
- **Description**: Retrieve a specific user
- **Authentication**: Admin only or self-access
- **Headers**: `Authorization: Bearer {{admin_token}}` or `{{user_token}}`

#### PUT /users/{{user_id}}
- **Description**: Update user information
- **Authentication**: User or admin
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}` or `{{admin_token}}`

**Request Body** (all fields optional):
```json
{
  "name": "Updated Name",
  "email": "newemail@example.com"
}
```

#### DELETE /users/{{user_id}}
- **Description**: Delete a user account
- **Authentication**: Admin only
- **Headers**: `Authorization: Bearer {{admin_token}}`

### Cart

#### GET /cart
- **Description**: Retrieve the current user's cart
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

**Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "product_id": 123,
        "product_name": "Wireless Headphones",
        "price": 99.99,
        "quantity": 2,
        "subtotal": 199.98
      }
    ],
    "total_items": 2,
    "total_amount": 199.98
  }
}
```

#### POST /cart/items
- **Description**: Add an item to the cart
- **Authentication**: User required
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123,
  "quantity": 1
}
```

#### PUT /cart/items/{{cart_item_id}}
- **Description**: Update cart item quantity
- **Authentication**: User required
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}`

**Request Body**:
```json
{
  "quantity": 3
}
```

#### DELETE /cart/items/{{cart_item_id}}
- **Description**: Remove an item from the cart
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

#### DELETE /cart
- **Description**: Clear the entire cart
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

### Orders

#### GET /orders
- **Description**: Retrieve user's orders
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

#### GET /orders/{{order_id}}
- **Description**: Retrieve a specific order
- **Authentication**: User required (own orders) or Admin
- **Headers**: `Authorization: Bearer {{user_token}}` or `{{admin_token}}`

#### POST /orders
- **Description**: Create a new order
- **Authentication**: User required
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}`

**Request Body**:
```json
{
  "shipping_address": "123 Main St, City, Country"
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
    "shipping_address": "123 Main St, City, Country",
    "status": "pending",
    "total_amount": 199.98,
    "items": [
      {
        "product_id": 123,
        "product_name": "Wireless Headphones",
        "price": 99.99,
        "quantity": 2,
        "subtotal": 199.98
      }
    ],
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

#### PUT /orders/{{order_id}}
- **Description**: Update an order (e.g., cancel order)
- **Authentication**: User (own orders) or Admin
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}` or `{{admin_token}}`

#### PUT /orders/{{order_id}}/status
- **Description**: Update order status (admin only)
- **Authentication**: Admin only
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{admin_token}}`

**Request Body**:
```json
{
  "status": "shipped"
}
```

### Reviews

#### POST /reviews
- **Description**: Create a product review
- **Authentication**: User required
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123,
  "rating": 5,
  "comment": "Excellent product, highly recommend!"
}
```

#### GET /products/{{product_id}}/reviews
- **Description**: Get reviews for a specific product
- **Authentication**: Optional (public endpoint)

### Wishlist

#### GET /wishlist
- **Description**: Retrieve user's wishlist
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

#### POST /wishlist
- **Description**: Add an item to wishlist
- **Authentication**: User required
- **Headers**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {{user_token}}`

**Request Body**:
```json
{
  "product_id": 123
}
```

#### DELETE /wishlist/{{wishlist_item_id}}
- **Description**: Remove an item from wishlist
- **Authentication**: User required
- **Headers**: `Authorization: Bearer {{user_token}}`

### Authentication

#### POST /login
- **Description**: Authenticate user
- **Authentication**: None required
- **Headers**: `Content-Type: application/json`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 123,
    "email": "user@example.com",
    "name": "John Doe",
    "is_admin": false
  }
}
```

#### POST /register
- **Description**: Register a new user
- **Authentication**: None required
- **Headers**: `Content-Type: application/json`

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "password",
  "name": "New User"
}
```

#### POST /refresh
- **Description**: Refresh access token
- **Authentication**: None required (uses refresh token)
- **Headers**: `Content-Type: application/json`

**Request Body**:
```json
{
  "refresh_token": "current_refresh_token"
}
```

## Request/Response Examples

### Example 1: Creating a Product (Admin)
**Request**:
```
POST /products
Authorization: Bearer {{admin_token}}
Content-Type: application/json

{
  "name": "Smartphone X",
  "description": "Latest smartphone with advanced features",
  "price": 699.99,
  "category": "Electronics",
  "stock": 50,
  "image_url": "https://example.com/smartphone.jpg"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 123,
    "name": "Smartphone X",
    "description": "Latest smartphone with advanced features",
    "price": 699.99,
    "category": "Electronics",
    "stock": 50,
    "image_url": "https://example.com/smartphone.jpg",
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

### Example 2: Adding Item to Cart (User)
**Request**:
```
POST /cart/items
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "product_id": 123,
  "quantity": 2
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "id": 456,
    "product_id": 123,
    "product_name": "Smartphone X",
    "price": 699.99,
    "quantity": 2,
    "subtotal": 1399.98
  }
}
```

### Example 3: Creating an Order (User)
**Request**:
```
POST /orders
Authorization: Bearer {{user_token}}
Content-Type: application/json

{
  "shipping_address": "123 Main Street, Anytown, USA"
}
```

**Response (201 Created)**:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": 789,
    "user_id": 101,
    "shipping_address": "123 Main Street, Anytown, USA",
    "status": "pending",
    "total_amount": 1399.98,
    "items": [
      {
        "product_id": 123,
        "product_name": "Smartphone X",
        "price": 699.99,
        "quantity": 2,
        "subtotal": 1399.98
      }
    ],
    "created_at": "2024-01-01T00:00:00Z"
  }
}
```

## Error Handling

The API returns consistent error responses in the following format:

### Common Error Responses

**400 Bad Request** - Invalid request data:
```json
{
  "success": false,
  "error": "Invalid input data"
}
```

**401 Unauthorized** - Missing or invalid authentication:
```json
{
  "success": false,
  "error": "Authentication required"
}
```

**403 Forbidden** - Insufficient permissions:
```json
{
  "success": false,
  "error": "Access denied"
}
```

**404 Not Found** - Resource doesn't exist:
```json
{
  "success": false,
  "error": "Resource not found"
}
```

**409 Conflict** - Resource conflict (e.g., duplicate email):
```json
{
  "success": false,
  "error": "Resource already exists"
}
```

**422 Unprocessable Entity** - Validation error:
```json
{
  "success": false,
  "error": "Validation failed",
  "details": {
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  }
}
```

**500 Internal Server Error** - Server error:
```json
{
  "success": false,
  "error": "Internal server error"
}
```

## Testing Guidelines

### Pre-test Setup
1. Ensure the base URL is properly configured in Postman environment
2. Obtain valid user and admin tokens before running authenticated tests
3. Set up required collection variables (product_id, user_id, etc.)

### Test Categories

#### Valid Test Cases
- **Authentication**: Login, register, token refresh
- **Product Operations**: List, get, create, update, delete products
- **Cart Operations**: Add, update, remove items, clear cart
- **Order Operations**: Create, list, update orders
- **User Operations**: Get, update user information
- **Review Operations**: Create, list product reviews

#### Invalid Test Cases
- **Authentication**: Invalid credentials, expired tokens, missing tokens
- **Authorization**: Admin endpoints with user tokens, user endpoints with wrong user ID
- **Validation**: Invalid input formats, out-of-range values, required field omissions
- **Business Logic**: Negative stock, duplicate resources, invalid states

### Postman Collection Variables
The collection uses the following variables for test data:
- `{{base_url}}` - API base URL
- `{{admin_token}}` - Admin JWT token
- `{{user_token}}` - User JWT token
- `{{refresh_token}}` - User refresh token
- `{{refresh_admin_token}}` - Admin refresh token
- `{{product_id}}` - Current product ID for testing
- `{{user_id}}` - Current user ID for testing
- `{{order_id}}` - Current order ID for testing
- `{{category_id}}` - Current category ID for testing
- `{{cart_item_id}}` - Current cart item ID for testing
- `{{wishlist_item_id}}` - Current wishlist item ID for testing

### Test Script Examples

#### Response Validation Script
```javascript
// Parse response JSON safely
let responseData = {};
let jsonValid = true;
try {
    responseData = pm.response.json();
} catch (e) {
    jsonValid = false;
}

pm.test("Response is valid JSON", function () {
    pm.expect(jsonValid).to.be.true;
});

// Validate success response
if (pm.response.code === 200) {
    pm.test("200 OK: Request successful", function () {
        if (!jsonValid) pm.expect.fail("Expected JSON body for 200 response, but parsing failed");

        pm.expect(responseData).to.have.property("success", true);
        pm.expect(responseData).to.have.property("data").that.is.an("object");
        
        // Validate specific data properties
        let data = responseData.data;
        pm.expect(data).to.have.property("id").that.is.a("number");
        pm.expect(data).to.have.property("name").that.is.a("string").and.not.empty;
    });
}
```

#### Authentication Validation Script
```javascript
// Check if user is authenticated
pm.test("User is authenticated", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
    
    let responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property("success", true);
    pm.expect(responseJson).to.have.property("user").that.is.an("object");
    
    let user = responseJson.user;
    pm.expect(user).to.have.property("id").that.is.a("number");
    pm.expect(user).to.have.property("email").that.is.a("string").and.not.empty;
    pm.expect(user).to.have.property("is_admin").that.is.a("boolean");
});
```

## Best Practices

### For API Consumers
1. Always implement proper error handling for all API calls
2. Use refresh tokens to maintain user sessions without prompting for login
3. Validate input data before sending requests to prevent unnecessary API calls
4. Implement retry logic for failed requests with exponential backoff
5. Cache responses where appropriate to reduce API load
6. Use HTTPS in production environments

### For Testing
1. Test both positive and negative scenarios
2. Verify all required fields are properly validated
3. Test authentication and authorization boundaries
4. Validate response schemas match the documentation
5. Test rate limiting behavior if implemented
6. Test with various data types and edge cases

### For Development
1. Follow RESTful API design principles
2. Use consistent naming conventions for endpoints and fields
3. Implement proper HTTP status codes
4. Provide meaningful error messages
5. Include comprehensive API documentation
6. Implement proper logging and monitoring