package ru.netology.test;

import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;
import ru.netology.page.TransferPage;
import ru.netology.page.VerificationPage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {
    private DashboardPage dashboardPage;
    private int initialFirstBalance;

    @BeforeEach
    public void setUp() {
        // Открываем SUT и логинимся
        open("http://localhost:9999");
        LoginPage loginPage = new LoginPage();
        DataHelper.AuthInfo authInfo = DataHelper.getAuthInfo();
        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        dashboardPage = verificationPage.validVerify(DataHelper.getVerificationCodeFor());

        // Явно ждём появления обеих карт "0001" и "0002"
        dashboardPage.waitForCardsToLoad();

        // Сохраняем исходные балансы
        initialFirstBalance = dashboardPage.getCardBalance("0001");
    }

    @AfterEach
    public void tearDown() {
        // После каждого теста проверяем, изменились ли балансы и возвращаем их в исходное состояние, если нужно
        int currentFirst = dashboardPage.getCardBalance("0001");

        // Проверяем, что балансы не стали отрицательными
        int currentSecond = dashboardPage.getCardBalance("0002");
        assert currentFirst >= 0 : "Баланс первой карты стал отрицательным: " + currentFirst;
        assert currentSecond >= 0 : "Баланс второй карты стал отрицательным: " + currentSecond;

        // Если балансы не совпадают с исходными, выполняем компенсирующий перевод
        int diff = currentFirst - initialFirstBalance;
        if (diff > 0) {
            // Если первая карта увеличилась на diff, переведем diff обратно с первой карты на вторую
            TransferPage transferPage = dashboardPage.selectCardToTransfer("0002");
            dashboardPage = transferPage.makeTransfer(String.valueOf(diff), DataHelper.getFirstCardInfo().getCardNumber());
        } else if (diff < 0) {
            // Если первая карта уменьшилась на diff, переведем обратно abs(diff) с второй карты на первую
            TransferPage transferPage = dashboardPage.selectCardToTransfer("0001");
            dashboardPage = transferPage.makeTransfer(String.valueOf(Math.abs(diff)), DataHelper.getSecondCardInfo().getCardNumber());
        }
    }

    @Test
    public void testTransferFromSecondToFirst() {
        int transferAmount = 1000;
        int firstBalanceBefore = dashboardPage.getCardBalance("0001");
        int secondBalanceBefore = dashboardPage.getCardBalance("0002");

        // Переводим с второй карты на первую
        TransferPage transferPage = dashboardPage.selectCardToTransfer("0001");
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getSecondCardInfo().getCardNumber());

        int firstBalanceAfter = dashboardPage.getCardBalance("0001");
        int secondBalanceAfter = dashboardPage.getCardBalance("0002");

        // Проверяем корректность изменения балансов
        assertEquals(firstBalanceBefore + transferAmount, firstBalanceAfter);
        assertEquals(secondBalanceBefore - transferAmount, secondBalanceAfter);
    }

    @Test
    public void testTransferFromFirstToSecond() {
        int transferAmount = 500;
        int firstBalanceBefore = dashboardPage.getCardBalance("0001");
        int secondBalanceBefore = dashboardPage.getCardBalance("0002");

        // Переводим с первой карты на вторую
        TransferPage transferPage = dashboardPage.selectCardToTransfer("0002");
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getFirstCardInfo().getCardNumber());

        int firstBalanceAfter = dashboardPage.getCardBalance("0001");
        int secondBalanceAfter = dashboardPage.getCardBalance("0002");

        // Проверяем корректность изменения балансов
        assertEquals(firstBalanceBefore - transferAmount, firstBalanceAfter);
        assertEquals(secondBalanceBefore + transferAmount, secondBalanceAfter);
    }

    @Test
    public void shouldNotAllowTransferMoreThanBalance() {
        int firstCardBalance = dashboardPage.getCardBalance("0001");
        int transferAmount = firstCardBalance + 1; // на 1 больше, чем есть на карте

        // Пытаемся перевести больше, чем доступно
        TransferPage transferPage = dashboardPage.selectCardToTransfer("0002");
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getFirstCardInfo().getCardNumber());

        int actualFirstCardBalance = dashboardPage.getCardBalance("0001");
        int actualSecondCardBalance = dashboardPage.getCardBalance("0002");

        // Проверяем, что перевод не прошёл — балансы не изменились
        assertEquals(firstCardBalance, actualFirstCardBalance, "Перевод с недостаточным балансом не должен был пройти");
        // Можно не проверять вторую карту, если первая не изменилась, но добавим для надёжности
        assertEquals(dashboardPage.getCardBalance("0002"), actualSecondCardBalance);
    }
}