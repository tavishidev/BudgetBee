/*This screen will show:
- Total spending for the current month.
- Spending grouped by category.
- Monthly spending history for the last six months.
- A Refresh button to reload the reports. */



package ui;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class ReportsFrame extends JFrame {

    private final int userId ;
    

    private final JLabel monthlyTotal = new JLabel("₹ 0.00");

    private final DefaultTableModel categoryModel =
            new DefaultTableModel(
                    new String[]{"Category", "Total Spent"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final DefaultTableModel monthlyModel =
            new DefaultTableModel(
                    new String[]{"Month", "Total Spent"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public ReportsFrame(int userId) {
        this.userId=userId;
        setTitle("BudgetBee | Reports");
        setSize(850, 600);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(new Color(245, 247, 250));

        add(createHeader(), BorderLayout.NORTH);
        add(createReportTables(), BorderLayout.CENTER);

        refreshReports();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(Color.WHITE);

        JLabel title = new JLabel("Financial Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel subtitle = new JLabel("Understand where your money goes");
        subtitle.setForeground(new Color(110, 119, 138));

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBackground(Color.WHITE);

        JLabel totalLabel = new JLabel("THIS MONTH");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        totalLabel.setForeground(new Color(110, 119, 138));

        monthlyTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
        monthlyTotal.setForeground(new Color(76, 110, 245));

        totalPanel.add(totalLabel);
        totalPanel.add(Box.createVerticalStrut(5));
        totalPanel.add(monthlyTotal);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshReports());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(Color.WHITE);
        right.add(totalPanel);
        right.add(refreshButton);

        header.add(titles, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createReportTables() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JTable categoryTable = new JTable(categoryModel);
        categoryTable.setRowHeight(27);

        JTable monthlyTable = new JTable(monthlyModel);
        monthlyTable.setRowHeight(27);

        JScrollPane categoryScroll = new JScrollPane(categoryTable);
        categoryScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Spending by Category — This Month"));

        JScrollPane monthlyScroll = new JScrollPane(monthlyTable);
        monthlyScroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Monthly Spending — Last 6 Months"));

        panel.add(categoryScroll);
        panel.add(monthlyScroll);

        return panel;
    }

    private void refreshReports() {
        loadMonthlyTotal();
        loadCategoryReport();
        loadMonthlyHistory();
    }

    private void loadMonthlyTotal() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        String sql = """
                SELECT COALESCE(SUM(amount), 0) AS total
                FROM expenses
                WHERE user_id = ?
                  AND expense_date >= ?
                  AND expense_date < ?
                """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    monthlyTotal.setText(
                            "₹ " + rs.getBigDecimal("total"));
                }
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loadCategoryReport() {
        categoryModel.setRowCount(0);

        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        String sql = """
                SELECT c.category_name,
                       SUM(e.amount) AS total
                FROM expenses e
                JOIN categories c
                    ON e.category_id = c.category_id
                WHERE e.user_id = ?
                  AND e.expense_date >= ?
                  AND e.expense_date < ?
                GROUP BY c.category_id, c.category_name
                ORDER BY total DESC
                """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categoryModel.addRow(new Object[]{
                            rs.getString("category_name"),
                            "₹ " + rs.getBigDecimal("total")
                    });
                }
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loadMonthlyHistory() {
        monthlyModel.setRowCount(0);

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.minusMonths(5)
                .atDay(1);
        LocalDate end = currentMonth.plusMonths(1)
                .atDay(1);

        String sql = """
                SELECT
                    YEAR(expense_date) AS expense_year,
                    MONTH(expense_date) AS expense_month,
                    SUM(amount) AS total
                FROM expenses
                WHERE user_id = ?
                  AND expense_date >= ?
                  AND expense_date < ?
                GROUP BY YEAR(expense_date), MONTH(expense_date)
                ORDER BY expense_year DESC, expense_month DESC
                """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    YearMonth month = YearMonth.of(
                            rs.getInt("expense_year"),
                            rs.getInt("expense_month"));

                    BigDecimal total = rs.getBigDecimal("total");

                    monthlyModel.addRow(new Object[]{
                            month.toString(),
                            "₹ " + total
                    });
                }
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                "Could not load report: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

