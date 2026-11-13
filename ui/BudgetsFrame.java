
package ui;

import model.Category;
import services.CategoryService;
import services.BudgetService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class BudgetsFrame extends JFrame {

    private final int userId;

    private final JComboBox<Category> categoryBox =
            new JComboBox<>();
    private final JTextField limitField = new JTextField(12);

    private final BudgetService budgetService =
            new BudgetService();

    private final CategoryService categoryService =
            new CategoryService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{"Category", "Monthly Limit",
                            "Spent", "Remaining", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    public BudgetsFrame(int userId) {
        this.userId=userId;
        setTitle("BudgetBee | Monthly Budgets");
        setSize(900, 550);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        add(createForm(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);

        loadCategories();
        refreshBudgets();
    }

    private JPanel createForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 15));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder(
                "Set Monthly Category Budget"));

        form.add(new JLabel("Category:"));
        categoryBox.setPreferredSize(new Dimension(150, 30));
        form.add(categoryBox);

        form.add(new JLabel("Limit (₹):"));
        form.add(limitField);

        JButton saveButton = new JButton("Save Budget");
        saveButton.setBackground(new Color(76, 110, 245));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveBudget());
        form.add(saveButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshBudgets());
        form.add(refreshButton);

        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 0, 12),
                BorderFactory.createTitledBorder(
                        "Set Monthly Category Budget")));

        return form;
    }

    private JScrollPane createTable() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 12, 12, 12),
                BorderFactory.createTitledBorder(
                        "Current Month Budgets")));

        return scrollPane;
    }

    private void loadCategories() {
        categoryBox.removeAllItems();

        for (Category category : categoryService.getAllCategories()) {
            categoryBox.addItem(category);
        }
    }

    private void saveBudget() {
        Category category =
                (Category) categoryBox.getSelectedItem();

        if (category == null) {
            JOptionPane.showMessageDialog(
                    this, "No categories available.");
            return;
        }

        try {
            BigDecimal limit =
                    new BigDecimal(limitField.getText().trim());

            if (limit.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Budget must be greater than zero.");
                return;
            }

            boolean saved = budgetService.saveBudget(
                    userId, category.getCategoryId(), limit);

            if (saved) {
                JOptionPane.showMessageDialog(
                        this, "Monthly budget saved successfully!");
                limitField.setText("");
                refreshBudgets();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Could not save the budget.");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid amount, such as 2000.");
        }
    }

    private void refreshBudgets() {
        tableModel.setRowCount(0);

        for (Object[] row :
                budgetService.getMonthlyBudgets(userId)) {

            // Hide the internal category ID in the displayed table.
            tableModel.addRow(new Object[]{
                    row[1],
                    "₹ " + row[2],
                    "₹ " + row[3],
                    "₹ " + row[4],
                    row[5]
            });
        }
    }
}

