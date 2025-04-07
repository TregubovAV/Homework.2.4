package ru.netology.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

import java.time.Duration;

public class DashboardPage {

    private ElementsCollection cards = $$(".list__item");

    public int getCardBalance(String maskedCardNumber) {
        SelenideElement card = cards.findBy(text(maskedCardNumber));
        String text = card.text();
        return extractBalance(text);
    }

    private int extractBalance(String text) {
        String balanceSubstring = text.split("баланс: ")[1];
        balanceSubstring = balanceSubstring.split(" р")[0];
        return Integer.parseInt(balanceSubstring);
    }

    public TransferPage selectCardToTransfer(String maskedCardNumber) {
        SelenideElement card = cards.findBy(text(maskedCardNumber));
        card.$("button").click();
        return new TransferPage();
    }

    public void waitForCardsToLoad(String... maskedCardNumbers) {
        for (String maskedCardNumber : maskedCardNumbers) {
            cards.findBy(text(maskedCardNumber)).shouldBe(visible, Duration.ofSeconds(10));
        }
    }
}