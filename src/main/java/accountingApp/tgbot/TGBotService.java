package accountingApp.tgbot;

import accountingApp.controller.TomatoesController;
import accountingApp.entity.Tomatoes;
import accountingApp.repository.TomatoesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TGBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    final Logger logger = LoggerFactory.getLogger(TomatoesController.class);
    @Value("${telegram.bot.token}")
    private String botToken;

    private final TomatoesRepository tomatoesRepository;

    private TelegramClient telegramClient;

    @Autowired
    public TGBotService(TomatoesRepository tomatoesRepository) {
        this.tomatoesRepository = tomatoesRepository;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

//    // Инициализация TelegramClient
    @PostConstruct
    public void init() {
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    // Метод для отправки текстового сообщения
    public void sendTextMessage(String chatId, String text) throws TelegramApiException {
        if (telegramClient == null) {
            throw new IllegalStateException("TelegramClient is not initialized!");
        }

        try {
            SendMessage message = new SendMessage(chatId, text);
            telegramClient.execute(message);
        } catch (Exception e) {
            logger.error("TGBotService.sendTextMessage: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void consume(List<Update> updates) {
        // Обработка входящих обновлений (если нужно)
        updates.forEach(this::consume);
    }


    @Override
    public void consume(Update update) {

    }

    private String getSingleMessageTomatoesContent(){

        List<Tomatoes> tomatoList = tomatoesRepository.findAll();
        Tomatoes tomato = tomatoList.get(0);

        StringBuffer text = new StringBuffer();

        text.append(tomato.getName())
                .append( "\n")
                .append(tomato.getCategory())
                .append( "\n")
                .append(tomato.getTomatoesHeight())
                .append( "\n");

        return text.toString();
    }


}
