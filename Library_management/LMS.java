package com.library;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class LMS {
    private static final String DB_URL = "jdbc:mysql://localhost/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "chiru";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("1. Add Book");
                System.out.println("2. Check Out Book");
                System.out.println("3. Return Book");
                System.out.println("4. List Books");
                System.out.println("5. Exit");
                System.out.print("Select an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        addBook(connection, scanner);
                        break;
                    case 2:
                        checkOutBook(connection, scanner);
                        break;
                    case 3:
                        returnBook(connection, scanner);
                        break;
                    case 4:
                        listBooks(connection);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addBook(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();

        String insertQuery = "INSERT INTO books (title, author, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setInt(3, quantity);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book added successfully.");
            } else {
                System.out.println("Failed to add the book.");
            }
        }
    }

    private static void checkOutBook(Connection connection, Scanner scanner) throws SQLException {
        listBooks(connection); // Display the available books

        System.out.print("Enter the ID of the book you want to check out: ");
        int bookId = scanner.nextInt();

        System.out.print("Enter the member's name: ");
        String memberName = scanner.nextLine(); // Consume newline
        memberName = scanner.nextLine(); // Read the member's name

        // Check if the book exists and is available
        String selectQuery = "SELECT * FROM books WHERE id = ? AND quantity > 0";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, bookId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Book exists and is available; proceed with check-out
                String updateQuery = "UPDATE books SET quantity = quantity - 1 WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, bookId);
                    int rowsUpdated = updateStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        System.out.println("Book checked out successfully.");
                        // You can add a record of the transaction here if needed
                    } else {
                        System.out.println("Failed to check out the book.");
                    }
                }
            } else {
                System.out.println("Book not found or not available for check-out.");
            }
        }
    }


    private static void returnBook(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter the ID of the book you want to return: ");
        int bookId = scanner.nextInt();

        // Check if the book exists and has been checked out
        String selectQuery = "SELECT * FROM books WHERE id = ? AND quantity < ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, bookId);
            preparedStatement.setInt(2, getMaxBookQuantity());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Book exists and has been checked out; proceed with return
                String updateQuery = "UPDATE books SET quantity = quantity + 1 WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, bookId);
                    int rowsUpdated = updateStatement.executeUpdate();

                    if (rowsUpdated > 0) {
                        System.out.println("Book returned successfully.");
                        // You can update the transaction record here if needed
                    } else {
                        System.out.println("Failed to return the book.");
                    }
                }
            } else {
                System.out.println("Book not found or already returned.");
            }
        }
    }

    private static int getMaxBookQuantity() {
        // Define the maximum quantity for a book in the library
        // Modify this value as per your requirements
        return 10; // For example, a maximum of 10 copies of each book
    }


    private static void listBooks(Connection connection) throws SQLException {
        String selectQuery = "SELECT * FROM books";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            System.out.println("Books in the library:");
            System.out.println("ID\tTitle\tAuthor\tQuantity");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                int quantity = resultSet.getInt("quantity");

                System.out.println(id + "\t" + title + "\t" + author + "\t" + quantity);
            }
        }
    }
}
