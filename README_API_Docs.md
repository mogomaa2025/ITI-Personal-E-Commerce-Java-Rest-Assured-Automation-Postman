# ITI E-Commerce API Documentation

Welcome to the comprehensive documentation for the ITI E-Commerce API. This collection provides full e-commerce functionality including user management, product catalog, cart operations, order processing, and administrative features.

## Documentation Files

This documentation package includes three main files:

### 1. API_Documentation.md
A comprehensive guide covering all API endpoints, authentication requirements, request/response formats, and usage examples. This document provides detailed information about each endpoint and its functionality.

**Key Sections:**
- API Overview and Authentication
- Detailed endpoint documentation
- Request/Response examples
- Error handling
- Collection variables

### 2. API_Reference_Guide.md
An extensive reference guide with technical specifications, testing guidelines, and implementation best practices. This document is designed for developers who need detailed technical information.

**Key Sections:**
- Complete API reference
- Request/Response examples
- Error handling specifications
- Testing guidelines and scripts
- Implementation best practices

### 3. API_Quick_Reference.md
A concise quick reference guide for rapid lookup of common endpoints, headers, and parameters. This document is ideal for quick reference during development.

**Key Sections:**
- API overview
- Main endpoints
- Authentication requirements
- Common headers and response formats
- Collection variables
- Testing workflow

## Getting Started

### Prerequisites
- Postman installed
- Access to the ITI E-Commerce API
- Basic understanding of REST APIs and JWT authentication

### Setup
1. Import the Postman collection into your Postman workspace
2. Create an environment with the required variables:
   - `base_url`: The API base URL (e.g., `https://itigraduation.pythonanywhere.com/api`)
   - `admin_token`: Admin JWT token
   - `user_token`: User JWT token
   - `refresh_token`: User refresh token
3. Use the documentation to understand endpoint functionality

### Authentication
The API uses JWT tokens for authentication:
- **Public endpoints**: No authentication required
- **User endpoints**: Require `Authorization: Bearer {{user_token}}`
- **Admin endpoints**: Require `Authorization: Bearer {{admin_token}}`

## API Features

### Product Management
- Full CRUD operations for products
- Product search and filtering
- Category management
- Inventory tracking

### User Management
- User registration and login
- Profile management
- Admin user operations

### Shopping Experience
- Shopping cart functionality
- Wishlist management
- Product reviews and ratings

### Order Processing
- Order creation and management
- Order status tracking
- Shipping address management

### Administrative Features
- User management (admin)
- Product management (admin)
- Analytics and reporting
- Coupon management

## Testing Approach

The documentation covers both positive and negative test scenarios:

### Valid Test Cases
- Successful API operations
- Proper authentication
- Valid input data
- Expected responses

### Invalid Test Cases
- Missing authentication
- Invalid input data
- Insufficient permissions
- Non-existent resources

## Support

For questions about the API or documentation:
- Refer to the comprehensive documentation files
- Check the error handling section for troubleshooting
- Review the testing guidelines for implementation best practices

## Version Information

- **API Version**: v33
- **Documentation Updated**: December 2025
- **API Host**: `https://itigraduation.pythonanywhere.com/api`

## Additional Resources

- Postman collection: ITI E-Commerce API Online V33
- Test variables and environments are documented in the main documentation files
- Example requests and responses are provided for all major operations