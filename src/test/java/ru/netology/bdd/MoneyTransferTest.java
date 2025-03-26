package ru.netology.bdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {

    @BeforeEach
    void setup() {
        // Открываем SUT (System Under Test)
        // Адрес может отличаться, если вы локально поднимаете приложение.
        // Например, http://localhost:9999
        open("http://localhost:9999");
    }

    @Test
    void shouldTransferMoneyFromSecondToFirst() {
        var loginPage = new LoginPage();
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor();
        var dashboardPage = verificationPage.validVerify(verificationCode);

        // Получаем текущие балансы
        var firstCardBalanceBefore = dashboardPage.getCardBalance("0001");
        var secondCardBalanceBefore = dashboardPage.getCardBalance("0002");

        // Сумма перевода
        int amount = 200;

        // Переходим на страницу пополнения первой карты
        var transferPage = dashboardPage.selectCardToTransfer("0001");
        // Совершаем перевод со второй карты
        dashboardPage = transferPage.makeTransfer(String.valueOf(amount), DataHelper.getSecondCardInfo().getCardNumber());

        // Проверяем балансы после
        var firstCardBalanceAfter = dashboardPage.getCardBalance("0001");
        var secondCardBalanceAfter = dashboardPage.getCardBalance("0002");

        assertEquals(firstCardBalanceBefore + amount, firstCardBalanceAfter);
        assertEquals(secondCardBalanceBefore - amount, secondCardBalanceAfter);
    }
}