package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$;

public class TransferPage {
    private SelenideElement amountField = $("[data-test-id=amount] input");
    private SelenideElement fromField = $("[data-test-id=from] input");
    private SelenideElement transferButton = $("[data-test-id=action-transfer]");

    public DashboardPage makeTransfer(String amount, String fromCardNumber) {
        amountField.setValue(amount);
        fromField.setValue(fromCardNumber);
        transferButton.click();
        return new DashboardPage();
    }
}