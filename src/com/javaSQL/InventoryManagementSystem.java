package com.javaSQL;

import java.sql.*;
import java.util.Scanner;

public class InventoryManagementSystem {
    private static final String URL = "jdbc:postgresql://localhost:5432/inventory_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    private static Connection connection = null;

    public static void main(String[] args) {
        try {
            // Establish database connection
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            createTables();
            
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\n=== Inventory Management System ===");
                System.out.println("1. Add Product");
                System.out.println("2. Update Stock");
                System.out.println("3. View Products");
                System.out.println("4. Create Order");
                System.out.println("5. View Orders");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        updateStock();
                        break;
                    case 3:
                        viewProducts();
                        break;
                    case 4:
                        createOrder();
                        break;
                    case 5:
                        viewOrders();
                        break;
                    case 6:
                        System.out.println("Thank you for using the system!");
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Create Products table
        String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "product_id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "price DECIMAL(10,2) NOT NULL," +
                "stock_quantity INT NOT NULL," +
                "category VARCHAR(50)" +
                ")";
        
        // Create Orders table
        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                "order_id INT AUTO_INCREMENT PRIMARY KEY," +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "total_amount DECIMAL(10,2) NOT NULL," +
                "status VARCHAR(20) DEFAULT 'PENDING'" +
                ")";
        
        // Create OrderItems table
        String createOrderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                "order_id INT," +
                "product_id INT," +
                "quantity INT NOT NULL," +
                "price_per_unit DECIMAL(10,2) NOT NULL," +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY (product_id) REFERENCES products(product_id)" +
                ")";
        
        stmt.execute(createProductsTable);
        stmt.execute(createOrdersTable);
        stmt.execute(createOrderItemsTable);
        stmt.close();
    }

    private static void addProduct() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter product name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter product description: ");
            String description = scanner.nextLine();
            
            System.out.print("Enter price: ");
            double price = scanner.nextDouble();
            
            System.out.print("Enter initial stock quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine();
            
            System.out.print("Enter category: ");
            String category = scanner.nextLine();
            
            String sql = "INSERT INTO products (name, description, price, stock_quantity, category) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, category);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product added successfully!");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateStock() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter product ID: ");
            int productId = scanner.nextInt();
            
            System.out.print("Enter new stock quantity: ");
            int newQuantity = scanner.nextInt();
            
            String sql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Stock updated successfully!");
            } else {
                System.out.println("Product not found!");
            }
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewProducts() {
        try {
            String sql = "SELECT * FROM products";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            System.out.println("\nProduct List:");
            System.out.println("ID | Name | Description | Price | Stock | Category");
            System.out.println("------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %.2f | %d | %s%n",
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity"),
                    rs.getString("category")
                );
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createOrder() {
        try {
            Scanner scanner = new Scanner(System.in);
            double totalAmount = 0;
            
            // First create the order
            String createOrderSql = "INSERT INTO orders (total_amount) VALUES (0)";
            PreparedStatement pstmt = connection.prepareStatement(createOrderSql, Statement.RETURN_GENERATED_KEYS);
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            
            while (true) {
                System.out.print("Enter product ID (0 to finish): ");
                int productId = scanner.nextInt();
                if (productId == 0) break;
                
                System.out.print("Enter quantity: ");
                int quantity = scanner.nextInt();
                
                // Get product price and check stock
                String checkProductSql = "SELECT price, stock_quantity FROM products WHERE product_id = ?";
                PreparedStatement checkStmt = connection.prepareStatement(checkProductSql);
                checkStmt.setInt(1, productId);
                ResultSet productRs = checkStmt.executeQuery();
                
                if (productRs.next()) {
                    double price = productRs.getDouble("price");
                    int stockQuantity = productRs.getInt("stock_quantity");
                    
                    if (stockQuantity >= quantity) {
                        // Add order item
                        String addItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price_per_unit) VALUES (?, ?, ?, ?)";
                        PreparedStatement itemStmt = connection.prepareStatement(addItemSql);
                        itemStmt.setInt(1, orderId);
                        itemStmt.setInt(2, productId);
                        itemStmt.setInt(3, quantity);
                        itemStmt.setDouble(4, price);
                        itemStmt.executeUpdate();
                        
                        // Update stock
                        String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
                        PreparedStatement updateStmt = connection.prepareStatement(updateStockSql);
                        updateStmt.setInt(1, quantity);
                        updateStmt.setInt(2, productId);
                        updateStmt.executeUpdate();
                        
                        totalAmount += price * quantity;
                    } else {
                        System.out.println("Insufficient stock!");
                    }
                } else {
                    System.out.println("Product not found!");
                }
            }
            
            // Update order total
            String updateOrderSql = "UPDATE orders SET total_amount = ? WHERE order_id = ?";
            PreparedStatement updateOrderStmt = connection.prepareStatement(updateOrderSql);
            updateOrderStmt.setDouble(1, totalAmount);
            updateOrderStmt.setInt(2, orderId);
            updateOrderStmt.executeUpdate();
            
            System.out.println("Order created successfully! Total amount: $" + totalAmount);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewOrders() {
        try {
            String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                        "p.name, oi.quantity, oi.price_per_unit " +
                        "FROM orders o " +
                        "JOIN order_items oi ON o.order_id = oi.order_id " +
                        "JOIN products p ON oi.product_id = p.product_id " +
                        "ORDER BY o.order_date DESC";
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            int currentOrderId = -1;
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                
                if (orderId != currentOrderId) {
                    if (currentOrderId != -1) {
                        System.out.println("----------------------------------------");
                    }
                    System.out.printf("\nOrder #%d%n", orderId);
                    System.out.printf("Date: %s%n", rs.getTimestamp("order_date"));
                    System.out.printf("Status: %s%n", rs.getString("status"));
                    System.out.printf("Total Amount: $%.2f%n", rs.getDouble("total_amount"));
                    System.out.println("\nItems:");
                    currentOrderId = orderId;
                }
                
                System.out.printf("- %s (Qty: %d, Price: $%.2f)%n",
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price_per_unit")
                );
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
