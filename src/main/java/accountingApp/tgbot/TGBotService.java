package accountingApp.tgbot;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class TGBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Override
    public String getBotToken() {
        return null;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return null;
    }

    @Override
    public void consume(List<Update> updates) {

    }

    @Override
    public void consume(Update update) {

    }


}
