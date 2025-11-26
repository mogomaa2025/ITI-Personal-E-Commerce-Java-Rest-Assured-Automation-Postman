# E-Commerce API Automation Project

This project provides a comprehensive framework for API automation testing of an E-Commerce backend, using REST Assured and Postman. It includes detailed documentation, automated tests, and reporting to ensure the quality and reliability of the API.

## Project Overview

This project is designed to serve as a reference for best practices in API automation. It includes a Postman collection for manual testing and exploration, as well as a complete REST Assured framework for automated testing. The project follows the Page Object Model (POM) design pattern to ensure maintainability and scalability.

## Key Features

- **API Health Checks:** Endpoints to monitor the availability and health of the API.
- **User Authentication:** Secure registration and login for both regular users and administrators.
- **Product Management:** Full CRUD (Create, Read, Update, Delete) functionality for products and categories.
- **Order Management:** A complete workflow for creating, updating, and tracking orders.
- **Allure Reporting:** Integrated Allure framework for detailed and visually appealing test reports.

## Technologies Used

- **Java:** The primary programming language for the automation framework.
- **REST Assured:** A Java library for testing RESTful APIs.
- **TestNG:** A testing framework for Java.
- **Allure:** A flexible, lightweight multi-language test report tool.
- **Postman:** A platform for building and using APIs, used here for manual testing and documentation.
- **Maven:** A build automation tool used for managing project dependencies and builds.

## Getting Started

To get started with this project, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/your-repository.git
   ```

2. **Navigate to the project directory:**
   ```bash
   cd your-repository
   ```

3. **Install the dependencies:**
   ```bash
   mvn install
   ```

4. **Run the tests:**
   ```bash
   mvn test
   ```

5. **Generate the Allure report:**
   ```bash
   mvn allure:serve
   ```

## Snapshots and Design Patterns

### Design Patterns

This project follows the **Page Object Model (POM)** design pattern, which is widely used in test automation. In the context of API testing, POM is adapted to create a clear and maintainable structure. Each API endpoint or a group of related endpoints is treated as a "page" or a service, with its own class responsible for handling requests and responses.

### Snapshots

Here are some snapshots of the Allure report, which provides a detailed overview of the test results:

**Allure Report Overview:**
![Allure Report](httpspre-commit-instructions://i.imgur.com/example-report.png)

**Test Execution Details:**
![Allure Test Details](https://i.imgur.com/example-details.png)
