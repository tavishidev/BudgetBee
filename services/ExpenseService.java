
package services;

import database.DBConnection;
import model.Expense;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

////The ? symbols are placeholders. PreparedStatement safely supplies the values rather than building SQL by joining user input into the query.

public class ExpenseService {

    public boolean addExpense(Expense expense) {
        String sql = """
            INSERT INTO expenses
            (user_id, category_id, amount, description, expense_date)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, expense.getUserId());
            ps.setInt(2, expense.getCategoryId());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setString(4, expense.getDescription());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println("Error adding expense: " + e.getMessage());
            return false;
        }
    }
    
public void viewExpenses(int userId) {
    String sql = """
        SELECT e.expense_id, c.category_name, e.amount,
               e.description, e.expense_date
        FROM expenses e
        JOIN categories c ON e.category_id = c.category_id
        WHERE e.user_id = ?
        ORDER BY e.expense_date DESC, e.expense_id DESC
        """;

    try (
        Connection con = DBConnection.getConnection();
        java.sql.PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setInt(1, userId);

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            System.out.println("\n========== YOUR EXPENSES ==========");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println(
                    "ID: " + rs.getInt("expense_id")
                    + " | Category: " + rs.getString("category_name")
                    + " | Amount: Rs. " + rs.getBigDecimal("amount")
                    + " | Description: " + rs.getString("description")
                    + " | Date: " + rs.getDate("expense_date")
                );
            }

            if (!found) {
                System.out.println("No expenses found.");
            }
        }

    } catch (SQLException e) {
        System.out.println("Error viewing expenses: " + e.getMessage());
    }
}
  
//This method uses SQL's SUM() to add all expenses belonging to the selected user. COALESCE() returns zero if no expenses exist.
public void viewTotalSpending(int userId) {
    String sql =
        "SELECT COALESCE(SUM(amount), 0) AS total " +
        "FROM expenses WHERE user_id = ?";

    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setInt(1, userId);

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println(
                    "\nTotal Spending: Rs. " +
                    rs.getBigDecimal("total")
                );
            }
        }

    } catch (SQLException e) {
        System.out.println(
            "Error calculating spending: " + e.getMessage()
        );
    }
}

public BigDecimal getTotalSpending(int userId) {
    String sql =
        "SELECT COALESCE(SUM(amount), 0) AS total " +
        "FROM expenses WHERE user_id = ?";

    return getDecimalValue(sql, userId);
}

/*one method calculates all-time spending, another calculates spending for the current calendar month, and the third counts saved expenses. The month calculation includes the first day of this month and excludes the first day of the next month. */

public BigDecimal getCurrentMonthSpending(int userId) {
    String sql =
        "SELECT COALESCE(SUM(amount), 0) AS total " +
        "FROM expenses WHERE user_id = ? " +
        "AND expense_date >= ? AND expense_date < ?";

    LocalDate today = LocalDate.now();
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate nextMonth = monthStart.plusMonths(1);

    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setInt(1, userId);
        ps.setDate(2, java.sql.Date.valueOf(monthStart));
        ps.setDate(3, java.sql.Date.valueOf(nextMonth));

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error loading monthly spending: "
                + e.getMessage());
    }

    return BigDecimal.ZERO;
}

public int getExpenseCount(int userId) {
    String sql = "SELECT COUNT(*) FROM expenses WHERE user_id = ?";

    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setInt(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        System.out.println("Error counting expenses: " + e.getMessage());
    }

    return 0;
}

private BigDecimal getDecimalValue(String sql, int userId) {
    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setInt(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error loading total spending: "
                + e.getMessage());
    }

    return BigDecimal.ZERO;
}

}
