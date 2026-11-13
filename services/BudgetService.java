//This service will save monthly limits and calculate spending for each category directly from MySQL.

/*What this adds:
- Saving a limit for a category and the current month.
- Updating the existing limit instead of creating a duplicate.
- Calculating actual spending for that category this month.
- Showing the remaining amount.
- Marking budgets ON TRACK, NEAR LIMIT (80% or more used), or OVER BUDGET.  */
package services;

import database.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {

    public boolean saveBudget(
            int userId, int categoryId, BigDecimal limit) {

        String sql = """
            INSERT INTO budgets
                (user_id, category_id, monthly_limit, budget_month)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE monthly_limit = ?
            """;

        LocalDate monthStart =
                LocalDate.now().withDayOfMonth(1);

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setBigDecimal(3, limit);
            ps.setDate(4, Date.valueOf(monthStart));
            ps.setBigDecimal(5, limit);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error saving budget: "
                    + e.getMessage());
            return false;
        }
    }

    public List<Object[]> getMonthlyBudgets(int userId) {
        List<Object[]> rows = new ArrayList<>();

        LocalDate monthStart =
                LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonth = monthStart.plusMonths(1);

        String sql = """
            SELECT
                b.category_id,
                c.category_name,
                b.monthly_limit,
                COALESCE(SUM(e.amount), 0) AS spent
            FROM budgets b
            JOIN categories c
                ON b.category_id = c.category_id
            LEFT JOIN expenses e
                ON e.user_id = b.user_id
                AND e.category_id = b.category_id
                AND e.expense_date >= b.budget_month
                AND e.expense_date < ?
            WHERE b.user_id = ?
                AND b.budget_month = ?
            GROUP BY
                b.category_id, c.category_name,
                b.monthly_limit
            ORDER BY c.category_name
            """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setDate(1, Date.valueOf(nextMonth));
            ps.setInt(2, userId);
            ps.setDate(3, Date.valueOf(monthStart));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal limit =
                            rs.getBigDecimal("monthly_limit");
                    BigDecimal spent =
                            rs.getBigDecimal("spent");
                    BigDecimal remaining = limit.subtract(spent);

                    String status;
                    if (remaining.signum() < 0) {
                        status = "OVER BUDGET";
                    } else if (spent.compareTo(
                            limit.multiply(new BigDecimal("0.8")))
                            >= 0) {
                        status = "NEAR LIMIT";
                    } else {
                        status = "ON TRACK";
                    }

                    rows.add(new Object[]{
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            limit,
                            spent,
                            remaining,
                            status
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading budgets: "
                    + e.getMessage());
        }

        return rows;
    }
}
