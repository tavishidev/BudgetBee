
package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private int userId;
    private int categoryId;
    private BigDecimal amount; //BigDecimal avoids the rounding surprises that can happen with double.
    private String description;
    private LocalDate expenseDate;

    public Expense(int userId, int categoryId, BigDecimal amount,
                   String description, LocalDate expenseDate) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
    }

    public int getUserId() {
        return userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }
}
