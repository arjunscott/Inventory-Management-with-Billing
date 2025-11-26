package com.inventory.app;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.util.HibernateUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InventoryApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ProductDAO productDAO = new ProductDAO();
    private static final SaleDAO saleDAO = new SaleDAO();

    public static void main(String[] args) {
        // Initialize Hibernate
        HibernateUtil.getSessionFactory();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    addProduct();
                    break;
                case "2":
                    updateProductQuantity();
                    break;
                case "3":
                    listProducts();
                    break;
                case "4":
                    createBill();
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        HibernateUtil.shutdown();
        System.out.println("Application closed.");
    }

    private static void printMenu() {
        System.out.println("\n===== INVENTORY MANAGEMENT WITH BILLING =====");
        System.out.println("1. Add Product");
        System.out.println("2. Update Product Quantity");
        System.out.println("3. View All Products");
        System.out.println("4. Create Purchase Bill");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    private static void addProduct() {
        System.out.print("Enter product code: ");
        String code = scanner.nextLine().trim();

        Product existing = productDAO.findByCode(code);
        if (existing != null) {
            System.out.println("Product code already exists. Use update option instead.");
            return;
        }

        System.out.print("Enter product name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter price: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());

        Product p = new Product(code, name, price, qty);
        productDAO.save(p);
        System.out.println("Product added successfully.");
    }

    private static void updateProductQuantity() {
        System.out.print("Enter product code: ");
        String code = scanner.nextLine().trim();

        Product p = productDAO.findByCode(code);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("Current quantity: " + p.getQuantity());
        System.out.print("Enter new quantity: ");
        int qty = Integer.parseInt(scanner.nextLine());
        p.setQuantity(qty);
        productDAO.save(p);
        System.out.println("Quantity updated.");
    }

    private static void listProducts() {
        System.out.println("\n--- Product List ---");
        for (Product p : productDAO.findAll()) {
            System.out.printf("Code: %s | Name: %s | Price: %.2f | Qty: %d%n",
                    p.getCode(), p.getName(), p.getPrice(), p.getQuantity());
        }
    }

    private static void createBill() {
        Map<Product, Integer> cart = new LinkedHashMap<>();

        while (true) {
            System.out.print("Enter product code (or 'done' to finish): ");
            String code = scanner.nextLine().trim();
            if ("done".equalsIgnoreCase(code)) {
                break;
            }

            Product p = productDAO.findByCode(code);
            if (p == null) {
                System.out.println("Product not found.");
                continue;
            }

            System.out.print("Enter quantity: ");
            int qty = Integer.parseInt(scanner.nextLine());

            if (qty <= 0) {
                System.out.println("Quantity must be > 0.");
                continue;
            }

            if (qty > p.getQuantity()) {
                System.out.println("Insufficient stock. Available: " + p.getQuantity());
                continue;
            }

            cart.put(p, cart.getOrDefault(p, 0) + qty);
        }

        if (cart.isEmpty()) {
            System.out.println("No items added to bill.");
            return;
        }

        double total = 0.0;
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            total += p.getPrice() * qty;
        }

        // OFFER / DISCOUNT LOGIC
        double discount = 0.0;
        if (total >= 1000 && total < 2000) {
            discount = total * 0.05; // 5% off
        } else if (total >= 2000) {
            discount = total * 0.10; // 10% off
        }

        double finalAmount = total - discount;

        // Create Sale and SaleItems
        Sale sale = new Sale(LocalDateTime.now(), total, discount, finalAmount);
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            Product p = entry.getKey();
            int qty = entry.getValue();
            double lineTotal = p.getPrice() * qty;
            SaleItem item = new SaleItem(p, qty, p.getPrice(), lineTotal);
            sale.addItem(item);

            // reduce stock
            p.setQuantity(p.getQuantity() - qty);
            productDAO.save(p);
        }

        saleDAO.save(sale);

        printBill(sale);
    }

    private static void printBill(Sale sale) {
        System.out.println("\n================ INVOICE ================");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        System.out.println("Bill No : " + sale.getId());
        System.out.println("Date    : " + sale.getSaleTime().format(formatter));
        System.out.println("----------------------------------------");
        System.out.printf("%-10s %-15s %5s %10s%n", "Code", "Name", "Qty", "Amount");
        System.out.println("----------------------------------------");

        for (SaleItem item : sale.getItems()) {
            Product p = item.getProduct();
            System.out.printf("%-10s %-15s %5d %10.2f%n",
                    p.getCode(), p.getName(), item.getQuantity(), item.getLineTotal());
        }

        System.out.println("----------------------------------------");
        System.out.printf("TOTAL    : %.2f%n", sale.getTotalAmount());
        System.out.printf("DISCOUNT : %.2f%n", sale.getDiscountAmount());
        System.out.printf("PAYABLE  : %.2f%n", sale.getFinalAmount());
        System.out.println("========================================");
    }
}
