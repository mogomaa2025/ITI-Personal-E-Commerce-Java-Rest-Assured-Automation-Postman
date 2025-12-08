# ITI E-Commerce API Quick Reference

## API Overview
- **Base URL**: `https://itigraduation.pythonanywhere.com/api`
- **Version**: v33
- **Authentication**: JWT Bearer tokens
- **Content Type**: `application/json`

## Authentication Endpoints

### POST /login
Login and get tokens
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

### POST /register
Register new user
```json
{
  "email": "newuser@example.com",
  "password": "password",
  "name": "New User"
}
```

### POST /refresh
Refresh access token
```json
{
  "refresh_token": "current_refresh_token"
}
```

## Main Endpoints

### Products
- `GET /products` - List products
- `GET /products/{{product_id}}` - Get specific product
- `POST /products` - Create product (Admin)
- `PUT /products/{{product_id}}` - Update product (Admin)
- `DELETE /products/{{product_id}}` - Delete product (Admin)

### Cart
- `GET /cart` - Get user's cart
- `POST /cart/items` - Add item to cart
- `PUT /cart/items/{{cart_item_id}}` - Update item quantity
- `DELETE /cart/items/{{cart_item_id}}` - Remove item
- `DELETE /cart` - Clear cart

### Orders
- `GET /orders` - List user's orders
- `GET /orders/{{order_id}}` - Get specific order
- `POST /orders` - Create order
- `PUT /orders/{{order_id}}` - Update order
- `PUT /orders/{{order_id}}/status` - Update status (Admin)

### Users
- `GET /users` - List all users (Admin)
- `GET /users/{{user_id}}` - Get specific user
- `PUT /users/{{user_id}}` - Update user
- `DELETE /users/{{user_id}}` - Delete user (Admin)

### Categories
- `GET /categories` - List categories
- `POST /categories` - Create category (Admin)
- `DELETE /categories/{{category_id}}` - Delete category (Admin)

### Reviews
- `POST /reviews` - Create review
- `GET /products/{{product_id}}/reviews` - Get product reviews

### Wishlist
- `GET /wishlist` - Get user's wishlist
- `POST /wishlist` - Add to wishlist
- `DELETE /wishlist/{{wishlist_item_id}}` - Remove from wishlist

## Authentication Requirements

| Access Level | Endpoints |
|--------------|-----------|
| Public | GET /products, GET /products/{{product_id}}, GET /categories, POST /login, POST /register, POST /coupons/validate |
| User Auth | GET /users/{{user_id}}, PUT /users/{{user_id}}, Cart ops, Order ops, Wishlist ops, Reviews |
| Admin Auth | All user endpoints, Product CRUD, Category CRUD, Order status updates, Analytics, Export ops |

## Common Headers
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

## Common Response Format
```json
{
  "success": true,
  "data": { ... } // or "message": "..." for actions
}
```

## Error Response Format
```json
{
  "success": false,
  "error": "Error message"
}
```

## Common HTTP Status Codes
- `200` - Success (GET, PUT)
- `201` - Created (POST)
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `422` - Validation Error
- `500` - Server Error

## Collection Variables
- `{{base_url}}` - API base URL
- `{{admin_token}}` - Admin JWT token
- `{{user_token}}` - User JWT token
- `{{refresh_token}}` - User refresh token
- `{{product_id}}` - Current product ID
- `{{user_id}}` - Current user ID
- `{{order_id}}` - Current order ID
- `{{category_id}}` - Current category ID

## Testing Workflow Example
1. Login to get tokens
2. List products to get a product ID
3. Add product to cart
4. Create an order
5. Verify order was created

## Query Parameters
- `page` - Page number for pagination
- `per_page` - Items per page (max 100)
- `category` - Filter by category
- `search` - Search term
- `min_price`/`max_price` - Price range
- `sort_by` - Sort field (name, price, created_at)
- `sort_order` - Sort order (asc, desc)

## Important Notes
- Admin endpoints require `{{admin_token}}`
- User endpoints require `{{user_token}}`
- Tokens expire and need refreshing
- Always validate responses in your application
- Handle errors appropriately
- Use HTTPS in production