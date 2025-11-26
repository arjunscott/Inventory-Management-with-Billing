Inventory Management with Billing (Console + Hibernate + MySQL)
===============================================================

Project Type:
-------------
- Core Java console application
- Inventory management + purchase billing
- Offers/discounts applied automatically on bill
- Data stored in MySQL using Hibernate ORM
- Maven project (import directly into Eclipse)

Features:
---------
1. Add products (code, name, price, quantity)
2. Update product stock quantity
3. View all products
4. Create purchase bill:
   - Add multiple items by product code and quantity
   - Validates stock
   - Calculates total, discount, final amount
   - Reduces stock from inventory
   - Saves bill and items to database
   - Prints formatted bill to console

Discount Rules (Offers):
------------------------
- If bill total >= 1000 and < 2000  → 5% discount
- If bill total >= 2000            → 10% discount
(You can change logic inside createBill() method.)

Prerequisites:
--------------
- JDK 8+ installed
- MySQL installed and running
- Maven (if building from command line)
- Eclipse IDE (Import as Existing Maven Project)

Database Setup:
---------------
1. Create database in MySQL:

   CREATE DATABASE inventorydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

2. Open: src/main/resources/hibernate.cfg.xml

   Update these properties according to your MySQL setup:

   - hibernate.connection.url
   - hibernate.connection.username
   - hibernate.connection.password

   Example:

   jdbc:mysql://localhost:3306/inventorydb?useSSL=false&serverTimezone=UTC
   username: root
   password: your_password

3. Hibernate property:

   <property name="hbm2ddl.auto">update</property>

   This will automatically create/update tables (products, sales, sale_items)
   when you run the application.

Running in Eclipse:
-------------------
1. File -> Import -> Maven -> Existing Maven Project
2. Select the 'inventory-billing-hibernate' folder
3. Finish and let Maven download dependencies

4. Run the app:
   - Right click: src/main/java/com/inventory/app/InventoryApp.java
   - Run As -> Java Application

Console Menu:
-------------
1. Add Product
2. Update Product Quantity
3. View All Products
4. Create Purchase Bill
5. Exit

Notes / Extensions:
-------------------
- You can add:
  * User login system
  * Product categories, GST, etc.
  * Report generation (daily sales, stock level)
  * GUI using Swing/JavaFX
