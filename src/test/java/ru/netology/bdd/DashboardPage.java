package ru.netology.bdd;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$$;

public class DashboardPage {

    private ElementsCollection cards = $$(".list__item");

    public int getCardBalance(String cardNumberEnding) {
        SelenideElement card = cards.findBy(text(cardNumberEnding));
        String text = card.text();
        return extractBalance(text);
    }

    private int extractBalance(String text) {
        String balanceSubstring = text.split("баланс: ")[1];
        balanceSubstring = balanceSubstring.split(" р")[0];
        return Integer.parseInt(balanceSubstring);
    }

    public TransferPage selectCardToTransfer(String cardNumberEnding) {
        SelenideElement card = cards.findBy(text(cardNumberEnding));
        card.$("button").click();
        return new TransferPage();
    }
}