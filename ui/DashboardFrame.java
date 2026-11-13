
package ui;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final Color backgroundColor = new Color(245, 247, 250);
    private final Color sidebarColor = new Color(28, 35, 51);
    private final Color accentColor = new Color(76, 110, 245);
    private final Color textColor = new Color(40, 48, 65);
    
    private final int userId ; 

    private final services.ExpenseService expenseService =
        new services.ExpenseService();

    private final JLabel totalSpendingValue =
        new JLabel("Loading...");

    private final JLabel monthlySpendingValue =
        new JLabel("Loading...");

    private final JLabel transactionCountValue =
        new JLabel("Loading...");

    

    public DashboardFrame(int userId) {
        this.userId=userId;
        setTitle("BudgetBee | Smart Expense Manager");
        setSize(1100, 700);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(backgroundColor);

        add(createSidebar(), BorderLayout.WEST);
        add(createDashboard(), BorderLayout.CENTER);
        refreshDashboard();
        
    }
    

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(220, 700));
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 18, 20, 18));

        JLabel logo = new JLabel("BudgetBee");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Your money. Your plan.");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tagline.setForeground(new Color(190, 199, 216));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(45));

        
        addNavButton(sidebar, "Dashboard", true);
        addNavButton(sidebar, "Expenses", false);
        addNavButton(sidebar, "Budgets", false);
        addNavButton(sidebar, "Reports", false);

        sidebar.add(Box.createVerticalGlue());

        addNavButton(sidebar, "Logout", false);
        sidebar.add(Box.createVerticalStrut(10));


        JLabel footer = new JLabel("SMART FINANCE");
        footer.setForeground(new Color(160, 172, 195));
        footer.setFont(new Font("SansSerif", Font.BOLD, 10));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private void addNavButton(
            JPanel sidebar, String text, boolean selected) {

        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(selected ? accentColor : sidebarColor);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 44));
        button.setPreferredSize(new Dimension(184, 44));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


button.addActionListener(e -> {
    String buttonText = button.getText();

    if (buttonText.equals("Expenses")) {
        new ExpensesFrame(userId).setVisible(true);

    } else if (buttonText.equals("Budgets")) {
        new BudgetsFrame(userId).setVisible(true);

    } else if (buttonText.equals("Reports")) {
        new ReportsFrame(userId).setVisible(true);

    } else if (buttonText.equals("Logout")) {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
});


        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(10));
    }

    private JPanel createDashboard() {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setBackground(backgroundColor);
        content.setBorder(
                BorderFactory.createEmptyBorder(30, 32, 30, 32));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(backgroundColor);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(textColor);

        JLabel subtitle = new JLabel(
                "Here's an overview of your finances.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 119, 138));

        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshDashboard());

        heading.add(title);
        heading.add(Box.createVerticalStrut(7));
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(12));
        heading.add(refreshButton);

        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
        cards.setBackground(backgroundColor);
        
cards.add(createSummaryCard(
        "TOTAL SPENDING", totalSpendingValue,
        "Across all recorded expenses"));

cards.add(createSummaryCard(
        "THIS MONTH", monthlySpendingValue,
        "Spending this calendar month"));

cards.add(createSummaryCard(
        "TRANSACTIONS", transactionCountValue,
        "Expenses recorded"));


        JPanel welcome = new JPanel();
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.setBackground(Color.WHITE);
        welcome.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(228, 232, 240)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        String userName = new services.UserService()
        .getUserName(userId);

JLabel welcomeTitle = new JLabel(
        "Welcome back, " + userName + "!");
        welcomeTitle.setFont(new Font("SansSerif", Font.BOLD, 21));
        welcomeTitle.setForeground(textColor);

       
JLabel welcomeText = new JLabel(
        "<html>Track your spending, manage your budgets, "
        + "and make smarter financial decisions. "
        + "Your journey toward better money management "
        + "starts here.</html>");

        welcomeText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        welcomeText.setForeground(new Color(100, 110, 130));

        welcome.add(welcomeTitle);
        welcome.add(Box.createVerticalStrut(12));
        welcome.add(welcomeText);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setBackground(backgroundColor);
        center.add(cards, BorderLayout.NORTH);
        center.add(welcome, BorderLayout.CENTER);

        content.add(heading, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

        return content;
    }

    
private JPanel createSummaryCard(
        String heading, JLabel valueLabel, String description) {

    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    new Color(228, 232, 240)),
            BorderFactory.createEmptyBorder(20, 16, 20, 16)));

    JLabel headingLabel = new JLabel(heading);
    headingLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
    headingLabel.setForeground(new Color(110, 119, 138));

    valueLabel.setFont(new Font("SansSerif", Font.BOLD, 23));
    valueLabel.setForeground(textColor);

    JLabel descriptionLabel = new JLabel(description);
    descriptionLabel.setFont(
            new Font("SansSerif", Font.PLAIN, 11));
    descriptionLabel.setForeground(new Color(130, 139, 155));

    card.add(headingLabel);
    card.add(Box.createVerticalStrut(16));
    card.add(valueLabel);
    card.add(Box.createVerticalStrut(8));
    card.add(descriptionLabel);

    return card;
}
// Add the refresh method
private void refreshDashboard() {
    totalSpendingValue.setText(
            "₹ " + expenseService.getTotalSpending(userId));

    monthlySpendingValue.setText(
            "₹ " + expenseService.getCurrentMonthSpending(userId));

    transactionCountValue.setText(
            String.valueOf(expenseService.getExpenseCount(userId)));
}

}
