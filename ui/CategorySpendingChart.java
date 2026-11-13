/*What this chart does
- Fetches spending by category from your MySQL database.
- Shows expenses for the current month only.
- Uses the logged-in userId, so it doesn't mix different users' expenses.
- Draws a bar for each category that has spending. */

package ui;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CategorySpendingChart extends JPanel {

    private final int userId;

    private static class CategoryData {
        String name;
        BigDecimal amount;

        CategoryData(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    private final List<CategoryData> data = new ArrayList<>();

    public CategorySpendingChart(int userId) {
        this.userId = userId;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(650, 320));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        loadData();
    }

    private void loadData() {
        data.clear();

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonth = monthStart.plusMonths(1);

        String sql = """
            SELECT c.category_name, COALESCE(SUM(e.amount), 0) AS total
            FROM categories c
            LEFT JOIN expenses e
                ON c.category_id = e.category_id
                AND e.user_id = ?
                AND e.expense_date >= ?
                AND e.expense_date < ?
            GROUP BY c.category_id, c.category_name
            HAVING total > 0
            ORDER BY total DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(monthStart));
            ps.setDate(3, Date.valueOf(nextMonth));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new CategoryData(
                        rs.getString("category_name"),
                        rs.getBigDecimal("total")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println("Chart loading error: " + e.getMessage());
        }
    }

    public void refreshChart() {
        loadData();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(new Color(40, 48, 65));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Spending by Category", 20, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(110, 119, 138));
        g2.drawString("Current month", 20, 48);

        if (data.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString(
                "No expenses recorded this month.",
                20, 85
            );
            g2.dispose();
            return;
        }

        int labelWidth = 105;
        int amountWidth = 90;
        int left = 20 + labelWidth;
        int right = getWidth() - amountWidth - 25;
        int availableWidth = Math.max(50, right - left);
        int rowHeight = 35;
        int startY = 75;

        BigDecimal max = data.stream()
            .map(item -> item.amount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ONE);

        for (int i = 0; i < data.size(); i++) {
            CategoryData item = data.get(i);
            int y = startY + i * rowHeight;

            if (y + rowHeight > getHeight()) {
                break;
            }

            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(new Color(65, 73, 90));
            g2.drawString(item.name, 20, y + 16);

            int barWidth = item.amount
                .multiply(BigDecimal.valueOf(availableWidth))
                .divide(max, 0, java.math.RoundingMode.HALF_UP)
                .intValue();

            barWidth = Math.max(3, barWidth);

            g2.setColor(new Color(232, 236, 245));
            g2.fillRoundRect(
                left, y, availableWidth, 18, 8, 8
            );

            g2.setColor(new Color(76, 110, 245));
            g2.fillRoundRect(
                left, y, Math.min(barWidth, availableWidth),
                18, 8, 8
            );

            g2.setColor(new Color(40, 48, 65));
            g2.drawString(
                "₹" + item.amount.toPlainString(),
                right + 8, y + 14
            );
        }

        g2.dispose();
    }
}
