/*This screen will include:
- A category dropdown populated from MySQL.
- Amount, description, and date fields.
- An Add Expense button.
- A transaction table that refreshes after saving.*/


package ui;

import model.Category;
import model.Expense;
import services.CategoryService;
import services.ExpenseService;
import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ExpensesFrame extends JFrame {

    private final int userId ;

    private final JComboBox<Category> categoryBox =
            new JComboBox<>();

    private final JTextField amountField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JTextField dateField =
            new JTextField(LocalDate.now().toString());

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"ID", "Category", "Amount",
                            "Description", "Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable expenseTable = new JTable(tableModel);

    private final CategoryService categoryService =
            new CategoryService();

    private final ExpenseService expenseService =
            new ExpenseService();

    public ExpensesFrame(int userId) {
        this.userId=userId;
        setTitle("BudgetBee | Expenses");
        setSize(950, 650);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout(15, 15));

        add(createForm(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadCategories();
        refreshExpenses();
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 0, 15),
                BorderFactory.createTitledBorder("Add New Expense")));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, "Category", categoryBox, 0, 0);
        addField(panel, gbc, "Amount (₹)", amountField, 1, 0);
        addField(panel, gbc, "Description", descriptionField, 0, 1);
        addField(panel, gbc, "Date (yyyy-mm-dd)", dateField, 1, 1);

        JButton addButton = new JButton("Add Expense");
        addButton.setBackground(new Color(76, 110, 245));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addExpense());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          String label, JComponent field,
                          int column, int row) {
        gbc.gridx = column * 2;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = column * 2 + 1;
        panel.add(field, gbc);
    }

    private JScrollPane createTable() {
        expenseTable.setRowHeight(28);
        expenseTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "Your Transactions"));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 15, 15),
                BorderFactory.createTitledBorder("Your Transactions")));

        return scrollPane;
    }

    private void loadCategories() {
        categoryBox.removeAllItems();

        for (Category category :
                categoryService.getAllCategories()) {
            categoryBox.addItem(category);
        }
    }

    private void addExpense() {
        try {
            Category selectedCategory =
                    (Category) categoryBox.getSelectedItem();

            if (selectedCategory == null) {
                JOptionPane.showMessageDialog(
                        this, "No categories available.");
                return;
            }

            BigDecimal amount =
                    new BigDecimal(amountField.getText().trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Amount must be greater than zero.");
                return;
            }

            LocalDate date =
                    LocalDate.parse(dateField.getText().trim());

            String description = descriptionField.getText().trim();

            Expense expense = new Expense(
                    userId,
                    selectedCategory.getCategoryId(),
                    amount,
                    description.isEmpty() ? null : description,
                    date
            );

            if (expenseService.addExpense(expense)) {
                JOptionPane.showMessageDialog(
                        this, "Expense added successfully!");

                amountField.setText("");
                descriptionField.setText("");
                dateField.setText(LocalDate.now().toString());

                refreshExpenses();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Could not save the expense.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid amount, such as 250.50.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter the date in yyyy-mm-dd format.");
        }
    }

    private void refreshExpenses() {
        tableModel.setRowCount(0);

        String sql = """
                SELECT e.expense_id, c.category_name, e.amount,
                       e.description, e.expense_date
                FROM expenses e
                JOIN categories c
                    ON e.category_id = c.category_id
                WHERE e.user_id = ?
                ORDER BY e.expense_date DESC, e.expense_id DESC
                """;

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("expense_id"),
                            rs.getString("category_name"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("expense_date")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load expenses: " + ex.getMessage());
        }
    }
}

