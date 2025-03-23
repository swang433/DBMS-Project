/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT\n");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out\n");
                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
    public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do 
      {
         System.out.print("Please make your choice: ");
         try 
         { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }
         catch (Exception e) 
         {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
    public static void CreateUser(PizzaStore esql) //new user registration
    {
      Scanner in = new Scanner(System.in);
      try {
         System.out.print("Enter user login: ");
         String login = in.nextLine();

         System.out.print("Enter user password: ");
         String password = in.nextLine();

         System.out.print("What type of user are you? (customer, manager, driver): ");

         String role = in.nextLine();
         if (!role.equals("customer") && !role.equals("manager") && !role.equals("driver")) 
         {
            System.out.println("Invalid role. Please try again.");
            return;
         }

         String favItems="";

         System.out.print("Enter user phone: ");
         String phone = in.nextLine();

         String query = String.format("INSERT INTO USERS (login, password, role, favoriteItems, phoneNum) VALUES ('%s','%s','%s','%s','%s')", login, password, role, favItems, phone);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
         System.out.println ("");
      }
      catch(Exception e)
      {
         System.err.println(e.getMessage());
      }
   }//end
  
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
    
   public static String LogIn(PizzaStore esql)
   {
      Scanner in = new Scanner(System.in);
      try {
         System.out.print("Enter your login: ");
         String login = in.nextLine();  // Reading user input
         System.out.print("Enter your password: ");
         String password = in.nextLine();  // Reading user input

         // Construct the query
         String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s';", login, password);

         // Execute the query
         int rows = esql.executeQuery(query);
         if (rows == 0) {
            System.out.println("Invalid login or password.");
            return null;
         }
         System.out.println("Login successful.\n");
         return login;
      } catch (Exception e) {
         System.err.println("Error logging in: " + e.getMessage());
      }
      //in.close(); 
      return null;
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql) 
   {
      Scanner in = new Scanner(System.in);
      
      try {
         System.out.print("Enter your login: ");
         String login = in.nextLine();  // Reading user input
         
         // Construct the query
         String query = String.format("SELECT * FROM Users WHERE login = '%s';", login);
         
         // Execute the query and display the results
         int rows = esql.executeQueryAndPrintResult(query);
         if (rows == 0) {
            System.out.println("No profile found for the given login.");
         }
      } catch (Exception e) {
         System.err.println("Error viewing profile: " + e.getMessage());
      }

      // in.close(); // No need to close explicitly as try-with-resources will handle it
   }
   
   public static void updateProfile(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
      
      try {
          System.out.print("Enter your login: ");
          String login = in.nextLine();  // Read user's login
          
          System.out.println("What would you like to update?");
          System.out.println("1. Favorite Items");
          System.out.println("2. Phone Number");
          System.out.println("3. Role");
          System.out.print("Enter your choice: ");
          String choice = in.nextLine();  // Read user's choice
  
          String query = "";
          if (choice.equals("1")) {
              System.out.print("Enter new favorite item: ");
              String favoriteItems = in.nextLine();
              query = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s';", favoriteItems, login);
          } else if (choice.equals("2")) {
              System.out.print("Enter new phone number: ");
              String phoneNum = in.nextLine();
              query = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s';", phoneNum, login);
          } else if (choice.equals("3")) {
              System.out.print("Enter new role (e.g., customer, manager, driver): ");
              String role = in.nextLine();

              query = String.format("UPDATE Users SET role = '%s' WHERE login = '%s';", role, login);
          } else {
              System.out.println("Invalid choice. Please try again.");
          }
  
          // Execute the update query
          esql.executeUpdate(query);
          System.out.println("Profile updated successfully.\n");
      } catch (Exception e) {
          System.err.println("Error updating profile: " + e.getMessage());
      }
  }
  

   public static void viewMenu(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
      
      try {
         System.out.print("Enter the store ID: ");
         int storeID = Integer.parseInt(in.nextLine());  // Reading user input
         
         // Construct the query
         String query = String.format("SELECT DISTINCT i.itemName, i.price, i.description " +
         "FROM Items i " +
         "JOIN Store s ON 1=1 " + // This join ensures all items are listed for any store
         "WHERE s.storeID = %d;", storeID);
         
         // Execute the query and display the results
         int rows = esql.executeQueryAndPrintResult(query);
         if (rows == 0) {
            System.out.println("No menu found for the given store ID.");
         }
      } catch (Exception e) {
         System.err.println("Error viewing menu: " + e.getMessage());
      }
   }

   public static void placeOrder(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
  
      try {
          // Get user login
          System.out.print("Enter your login: ");
          String login = in.nextLine();
  
          // Get store ID
          System.out.print("Enter the store ID: ");
          int storeID = Integer.parseInt(in.nextLine());
  
          // Get item name
          System.out.print("Enter the item you want to order: ");
          String itemName = in.nextLine();
  
          // Check item price
          String priceQuery = String.format("SELECT price FROM Items WHERE itemName = '%s'", itemName);
          double price = Double.parseDouble(esql.executeQueryAndReturnResult(priceQuery).get(0).get(0));
  
          // Generate unique orderID (assuming orderID is generated programmatically here)
          int orderID = (int)(Math.random() * 1000000); // Replace with proper orderID generation logic if necessary
  
          // Insert order into FoodOrder
          String insertOrderQuery = String.format(
              "INSERT INTO FoodOrder (orderID, login, storeID, totalPrice, orderTimestamp, orderStatus) " +
              "VALUES (%d, '%s', %d, %.2f, NOW(), 'Pending')",
              orderID, login, storeID, price
          );
          esql.executeUpdate(insertOrderQuery);
  
          // Insert item into ItemsInOrder
          String insertItemQuery = String.format(
              "INSERT INTO ItemsInOrder (orderID, itemName, quantity) " +
              "VALUES (%d, '%s', %d)",
              orderID, itemName, 1 // Assuming quantity is 1 for a single order
          );
          esql.executeUpdate(insertItemQuery);
  
          // Display order ID to user
          System.out.println("Order placed successfully! Your order ID is: " + orderID);
      } catch (Exception e) {
          System.err.println("Error: " + e.getMessage());
      }
  }
   
   public static void viewAllOrders(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
      
      try {
         System.out.print("Enter your login: ");
         String login = in.nextLine();  // Reading user input
         
         // Construct the query
         String query = String.format("SELECT * FROM FoodOrder WHERE login = '%s';", login);
         
         // Execute the query and display the results
         int rows = esql.executeQueryAndPrintResult(query);
         if (rows == 0) {
            System.out.println("No orders found for the given login.");
         }
      } catch (Exception e) {
         System.err.println("Error viewing orders: " + e.getMessage());
      }
   }
   
   public static void viewRecentOrders(PizzaStore esql) { //most 5 recent orders given a login
      Scanner in = new Scanner(System.in);
      
      try {
         System.out.print("Enter your login: ");
         String login = in.nextLine();  // Reading user input
         
         // Construct the query
         String query = String.format("SELECT * FROM FoodOrder WHERE login = '%s' ORDER BY orderTimestamp DESC LIMIT 5;", login);
         
         // Execute the query and display the results
         int rows = esql.executeQueryAndPrintResult(query);
         if (rows == 0) {
            System.out.println("No recent orders found for the given login.");
         }
      } catch (Exception e) {
         System.err.println("Error viewing recent orders: " + e.getMessage());
      }

   }
   
   public static void viewOrderInfo(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
      
      try {
         System.out.print("Enter the order ID: ");
         int orderID = Integer.parseInt(in.nextLine());  // Reading user input
         
         // Construct the query
         String query = String.format("SELECT * FROM FoodOrder WHERE orderID = %d;", orderID);
         
         // Execute the query and display the results
         int rows = esql.executeQueryAndPrintResult(query);
         if (rows == 0) {
            System.out.println("No order found for the given order ID.");
         }
      } catch (Exception e) {
         System.err.println("Error viewing order information: " + e.getMessage());
      }
   }
   
   public static void viewStores(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
    
    try {
        // Construct the query
        String query = "SELECT storeID, address, city, state, isOpen, reviewScore FROM store;";
        
        // Execute the query and display the results
        int rows = esql.executeQueryAndPrintResult(query);
        if (rows == 0) {
            System.out.println("No stores found.");
        }
    } catch (Exception e) {
        System.err.println("Error viewing stores: " + e.getMessage());
    }
   }
   
   public static void updateOrderStatus(PizzaStore esql) {
      Scanner in = new Scanner(System.in);
  
      try {
          System.out.print("Enter your login: ");
          String login = in.nextLine().trim();
  
          // Check if user is a manager
          String roleQuery = String.format("SELECT role FROM Users WHERE login = '%s';", login);
          List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
  
          if (roleResult.isEmpty()) {
              System.out.println("Login not found.");
              return;
          }
          String role = roleResult.get(0).get(0).trim();
          if (!role.equalsIgnoreCase("manager")) {
              System.out.println("Only managers can update order status.");
              return;
          }
  
          System.out.print("Enter the order ID: ");
          int orderID = Integer.parseInt(in.nextLine().trim());
  
          // Check if the order exists
          String countQuery = String.format("SELECT COUNT(*) FROM FoodOrder WHERE orderID = %d;", orderID);
          List<List<String>> countResult = esql.executeQueryAndReturnResult(countQuery);
  
          if (countResult.isEmpty() || Integer.parseInt(countResult.get(0).get(0)) == 0) {
              System.out.println("Order not found.");
              return;
          }
  
          // Construct the query
          String query = String.format("UPDATE FoodOrder SET orderStatus = 'Delivered' WHERE orderID = %d;", orderID);
  
          // Execute the query
          esql.executeUpdate(query);
          System.out.println("Order status updated successfully.\n");
      } catch (Exception e) {
          System.err.println("Error updating order status: " + e.getMessage());
      }
  }
  
  
  
  
   
    public static void updateMenu(PizzaStore esql) {
    Scanner in = new Scanner(System.in);
    System.out.println("login: ");
    String login = in.nextLine().trim();  // Read user's login and trim whitespace

      try {
        // Check if the logged-in user is a manager
         String roleQuery = String.format("SELECT role FROM Users WHERE login = '%s'", login);
         List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
         String temp = roleResult.get(0).get(0).trim();
         System.out.println(temp);


         if (!temp.equalsIgnoreCase("manager")) {
            System.out.println("You do not have permission to update menu.");
            return;
        }

        System.out.println("1. Update existing item");
        System.out.println("2. Add new item");
        System.out.print("Choose an option (1 or 2): ");
        int choice = Integer.parseInt(in.nextLine().trim());

        if (choice == 1) { // Update existing item
            System.out.print("Enter the item name to update: ");
            String itemName = in.nextLine().trim();
            
            // Check if item exists
            String checkQuery = String.format("SELECT * FROM Items WHERE itemName = '%s';", itemName);
            int count = esql.executeQuery(checkQuery);
            
            if (count == 0) {
                System.out.println("Item not found.");
                return;
            }
            
            System.out.print("Enter new ingredients (or press Enter to keep current): ");
            String ingredients = in.nextLine().trim();
            if (ingredients.isEmpty()) ingredients = null; // Keep existing if no new input

            System.out.print("Enter new type of item (or press Enter to keep current): ");
            String typeOfItem = in.nextLine().trim();
            if (typeOfItem.isEmpty()) typeOfItem = null; // Keep existing if no new input

            System.out.print("Enter new price (or press Enter to keep current): ");
            String priceStr = in.nextLine().trim();
            BigDecimal price = priceStr.isEmpty() ? null : new BigDecimal(priceStr);

            System.out.print("Enter new description (or press Enter to keep current): ");
            String description = in.nextLine().trim();
            if (description.isEmpty()) description = null; // Keep existing if no new input

            StringBuilder updateQuery = new StringBuilder("UPDATE Items SET ");
            List<String> updates = new ArrayList<>();

            if (ingredients != null) updates.add("ingredients = '" + ingredients + "'");
            if (typeOfItem != null) updates.add("typeOfItem = '" + typeOfItem + "'");
            if (price != null) updates.add("price = " + price);
            if (description != null) updates.add("description = '" + description + "'");

            if (!updates.isEmpty()) {
                updateQuery.append(String.join(", ", updates));
                updateQuery.append(String.format(" WHERE itemName = '%s';", itemName));
                esql.executeUpdate(updateQuery.toString());
                System.out.println("Item updated successfully.");
            } else {
                System.out.println("No updates were made.");
            }
            
        } else if (choice == 2) { // Add new item
            System.out.print("Enter new item name: ");
            String itemName = in.nextLine().trim();

            // Check if item already exists to avoid duplicates
            String checkQuery = String.format("SELECT * FROM Items WHERE itemName = '%s';", itemName);
            int count = esql.executeQuery(checkQuery);
            
            if (count > 0) {
                System.out.println("Item already exists.");
                return;
            }
            
            System.out.print("Enter ingredients: ");
            String ingredients = in.nextLine().trim();

            System.out.print("Enter type of item: ");
            String typeOfItem = in.nextLine().trim();

            System.out.print("Enter price: ");
            BigDecimal price = new BigDecimal(in.nextLine().trim());

            System.out.print("Enter description: ");
            String description = in.nextLine().trim();

            String insertQuery = String.format("INSERT INTO Items(itemName, ingredients, typeOfItem, price, description) VALUES ('%s', '%s', '%s', %s, '%s');", 
                                               itemName, ingredients, typeOfItem, price, description);
            
            esql.executeUpdate(insertQuery);
            System.out.println("New item added successfully.");
        } else {
            System.out.println("Invalid choice.");
        }
      } 
      catch (SQLException e) {
         System.err.println("Error updating menu: " + e.getMessage());
      }
   }
   
   public static void updateUser(PizzaStore esql) {
            
   Scanner in = new Scanner(System.in);
   System.out.println("Manager Login: ");
   String managerLogin = in.nextLine().trim();

    try {
        String roleQuery = String.format("SELECT role FROM Users WHERE login = '%s'", managerLogin);
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);
        String temp = roleResult.get(0).get(0).trim();

        if (!temp.equalsIgnoreCase("manager")) {
            System.out.println("You do not have permission");
            return;
        }

        System.out.println("Choose an operation:");
        System.out.println("1. Add a user");
        System.out.println("2. Delete a user");
        System.out.println("3. Update user details");
        System.out.print("Enter your choice: ");
        int choice = Integer.parseInt(in.nextLine().trim());

        switch (choice) {
            case 1: // Add a new user
                System.out.print("Enter new user login: ");
                String newLogin = in.nextLine().trim();
                System.out.print("Enter new user password: ");
                String newPassword = in.nextLine().trim();
                System.out.print("Enter new user role (customer/manager/driver): ");
                String newRole = in.nextLine().trim();
                System.out.print("Enter new user favorite items: ");
                String newFavItems = in.nextLine().trim();
                System.out.print("Enter new user phone number: ");
                String newPhone = in.nextLine().trim();

                // Check if the user already exists
                String checkUserQuery = String.format("SELECT * FROM Users WHERE login = '%s'", newLogin);
                if (esql.executeQuery(checkUserQuery) > 0) {
                    System.out.println("User already exists.");
                    return;
                }

                String insertUserQuery = String.format("INSERT INTO Users (login, password, role, favoriteItems, phoneNum) VALUES ('%s', '%s', '%s', '%s', '%s')", 
                                                       newLogin, newPassword, newRole, newFavItems, newPhone);
                esql.executeUpdate(insertUserQuery);
                System.out.println("New user added successfully.");
                break;

            case 2: // Delete a user
                System.out.print("Enter the login of the user to delete: ");
                String loginToDelete = in.nextLine().trim();
                String deleteUserQuery = String.format("DELETE FROM Users WHERE login = '%s'", loginToDelete);
                esql.executeUpdate(deleteUserQuery); // No need for return value here
                System.out.println("Attempt to delete user made.");
                break;

            case 3: // Update user details
                System.out.print("Enter the login of the user to update: ");
                String loginToUpdate = in.nextLine().trim();
                System.out.println("What would you like to update?");
                System.out.println("1. Favorite Items");
                System.out.println("2. Role");
                System.out.println("3. Phone Number");
                System.out.print("Enter your choice: ");
                int updateChoice = Integer.parseInt(in.nextLine().trim());

                String updateQuery = "";
                switch(updateChoice) {
                    case 1:
                        System.out.print("Enter new favorite items: ");
                        String newFavItemsUpdate = in.nextLine().trim();
                        updateQuery = String.format("UPDATE Users SET favoriteItems = '%s' WHERE login = '%s'", newFavItemsUpdate, loginToUpdate);
                        break;
                    case 2:
                        System.out.print("Enter new role: ");
                        String newRoleUpdate = in.nextLine().trim();
                        updateQuery = String.format("UPDATE Users SET role = '%s' WHERE login = '%s'", newRoleUpdate, loginToUpdate);
                        break;
                    case 3:
                        System.out.print("Enter new phone number: ");
                        String newPhoneUpdate = in.nextLine().trim();
                        updateQuery = String.format("UPDATE Users SET phoneNum = '%s' WHERE login = '%s'", newPhoneUpdate, loginToUpdate);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                        return;
                  }

                esql.executeUpdate(updateQuery); // No need for return value here
                System.out.println("Attempt to update user details made.");
                break;

            default:
                System.out.println("Invalid choice.");
         }
      } 
      catch (Exception e) {
         System.err.println("Error managing users: " + e.getMessage());
      }
   }


}//end PizzaStore

