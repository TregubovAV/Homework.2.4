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
        initialFirstBalance = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));

        
    }

    @AfterEach
    public void tearDown() {
        // После каждого теста проверяем, изменились ли балансы и возвращаем их в исходное состояние, если нужно
        int currentFirst = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));

        // Проверяем, что балансы не стали отрицательными
        int currentSecond = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        assert currentFirst >= 0 : "Баланс первой карты стал отрицательным: " + currentFirst;
        assert currentSecond >= 0 : "Баланс второй карты стал отрицательным: " + currentSecond;

        // Если балансы не совпадают с исходными, выполняем компенсирующий перевод
        int diff = currentFirst - initialFirstBalance;
        if (diff > 0) {
            // Если первая карта увеличилась на diff, переведем diff обратно с первой карты на вторую
            TransferPage transferPage = dashboardPage.selectCardToTransfer(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
            dashboardPage = transferPage.makeTransfer(String.valueOf(diff), DataHelper.getFirstCardInfo().getCardNumber());
        } else if (diff < 0) {
            // Если первая карта уменьшилась на diff, переведем обратно abs(diff) с второй карты на первую
            TransferPage transferPage = dashboardPage.selectCardToTransfer(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
            dashboardPage = transferPage.makeTransfer(String.valueOf(Math.abs(diff)), DataHelper.getSecondCardInfo().getCardNumber());
        }
    }

    @Test
    public void testTransferFromSecondToFirst() {
        int secondBalanceBefore = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        int firstBalanceBefore = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        int transferAmount = secondBalanceBefore - 1;
        TransferPage transferPage = dashboardPage.selectCardToTransfer(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getSecondCardInfo().getCardNumber());
        int firstBalanceAfter = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        int secondBalanceAfter = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        assertEquals(firstBalanceBefore + transferAmount, firstBalanceAfter);
        assertEquals(secondBalanceBefore - transferAmount, secondBalanceAfter);
    }

    @Test
    public void testTransferFromFirstToSecond() {
        int firstBalanceBefore = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        int secondBalanceBefore = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        int transferAmount = firstBalanceBefore - 1;
        TransferPage transferPage = dashboardPage.selectCardToTransfer(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getFirstCardInfo().getCardNumber());
        int firstBalanceAfter = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        int secondBalanceAfter = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        assertEquals(firstBalanceBefore - transferAmount, firstBalanceAfter);
        assertEquals(secondBalanceBefore + transferAmount, secondBalanceAfter);
    }

    @Test
    public void shouldNotAllowTransferMoreThanBalance() {
        int secondCardBalance = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        int transferAmount = secondCardBalance + 1;
        TransferPage transferPage = dashboardPage.selectCardToTransfer(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        dashboardPage = transferPage.makeTransfer(String.valueOf(transferAmount), DataHelper.getSecondCardInfo().getCardNumber());
        int actualSecondCardBalance = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getSecondCardInfo().getCardNumber()));
        int actualFirstCardBalance = dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber()));
        assertEquals(secondCardBalance, actualSecondCardBalance, "Перевод с недостаточным балансом не должен был пройти");
        assertEquals(dashboardPage.getCardBalance(DataHelper.getMaskedCardNumber(DataHelper.getFirstCardInfo().getCardNumber())), actualFirstCardBalance);
    }
}