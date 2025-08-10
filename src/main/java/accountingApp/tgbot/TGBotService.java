package accountingApp.tgbot;

import accountingApp.controller.TomatoesController;
import accountingApp.entity.Peppers;
import accountingApp.entity.Photo;
import accountingApp.entity.Seed;
import accountingApp.entity.Tomatoes;
import accountingApp.repository.PeppersRepository;
import accountingApp.repository.TomatoesRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Service
public class TGBotService {

    private static final Logger logger = LoggerFactory.getLogger(TomatoesController.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    private final TomatoesRepository tomatoesRepository;
    private final PeppersRepository peppersRepository;
    private TelegramBot bot;

    public TGBotService(TomatoesRepository tomatoesRepository, PeppersRepository peppersRepository) {
        this.tomatoesRepository = tomatoesRepository;
        this.peppersRepository = peppersRepository;
    }

    @PostConstruct
    public void init() {
        this.bot = new TelegramBot(botToken);

        bot.setUpdatesListener(updates -> {
            updates.forEach(this::processUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                logger.error("TGBotService.init(): Telegram API error: {}", e.response());
            } else {
                logger.error("TGBotService.init(): Telegram update listener error", e);
            }
        });
    }

    private void processUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            handleMessage(update);
        } else if (update.callbackQuery() != null) {
            handleCallbackQuery(update);
        }
    }

    private void handleMessage(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        if ("/hi".equals(messageText)) {
            sendInlineKeyboard(chatId, "Выберите действие:", createInlineKeyboard());
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.callbackQuery().data();
        long chatId = update.callbackQuery().message().chat().id();
        String callbackQueryId = update.callbackQuery().id();
        String responseText = "default";

        if ("button1_pressed".equals(callbackData)) {
            responseText = "Вы нажали кнопку 1";
            sendTextMessage(chatId, responseText);
        } else if ("button2_pressed".equals(callbackData)) {
            responseText = "Вы нажали кнопку 2";
            sendTextMessage(chatId, responseText);
        }

        // Ответ на callback (обязательно)
        bot.execute(new AnswerCallbackQuery(callbackQueryId).text(responseText));
    }

    public void sendInlineKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage request = new SendMessage(chatId, text)
                .replyMarkup(keyboard);

        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            logger.error("TGBotService.sendInlineKeyboard(): Failed to send inline keyboard: {}",
                    response.description());
        }
    }

    public void sendTextMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            logger.error("TGBotService.sendTextMessage(): Failed to send text message: {}",
                    response.description());
        }
    }

    public InlineKeyboardMarkup createInlineKeyboard() {
        // Создаем кнопки
        InlineKeyboardButton button1 = new InlineKeyboardButton("Томаты")
                .callbackData("button1_pressed");

        InlineKeyboardButton button2 = new InlineKeyboardButton("Перцы")
                .callbackData("button2_pressed");

        // Создаем ряд кнопок
        InlineKeyboardButton[] row = {button1, button2};

        // Создаем клавиатуру
        return new InlineKeyboardMarkup(row);
    }

    public void sendAllSeedsTextAndPhotosMessage(String chatId) {
        try {
            List<Seed> seedList = new ArrayList<>();
            seedList.addAll(tomatoesRepository.findAll());
            seedList.addAll(peppersRepository.findAll());

            for (int i = 0; i < 3 && i < seedList.size(); i++) {
                Seed s = seedList.get(i);
                sendTextWithPhotoMessage(chatId, s);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("TGBotService.sendAllSeedsTextAndPhotosMessage(): Error sending seeds info: {}",
                    e.getMessage());
        }
    }

    public void sendTextWithPhotoMessage(String chatId, Seed seed) {
        try {

            List<Photo> photos = new ArrayList<>();
            String text = "";

            if (seed instanceof Tomatoes) {
                Tomatoes tomato = (Tomatoes) seed;
                photos = tomato.getPhotos();
                text = getSingleMessageTomatoesContent(tomato);
            }else if (seed instanceof Peppers){
                Peppers pepper = (Peppers) seed;
                photos = pepper.getPhotos();
                text = getSingleMessagePeppersContent(pepper);

            }

            InputMediaPhoto[] mediaGroup = getInputMediaPhotos(photos, text);

            getAllPhotosFromSeed(mediaGroup, photos);

            bot.execute(new SendMediaGroup(chatId, mediaGroup));

            InlineKeyboardMarkup keyboard = createInlineKeyboard();
            sendInlineKeyboard(Long.parseLong(chatId), "Welcome!", keyboard);

        } catch (Exception e) {
            logger.error("TGBotService.sendTextWithPhotoMessage(): Error sending photo message: {}",
                    e.getMessage());
        }
    }

    @NotNull
    private InputMediaPhoto[] getInputMediaPhotos(List<Photo> photos, String text) {
        InputMediaPhoto[] mediaGroup = new InputMediaPhoto[photos.size()];

        byte[] photo = getSingleSeedPhotos(photos.get(0));
        InputMediaPhoto photoFile = new InputMediaPhoto(photo);
        photoFile.caption(text);
        photoFile.parseMode(ParseMode.valueOf("HTML"));
        mediaGroup[0] = photoFile;
        return mediaGroup;
    }


    private <T> void getAllPhotosFromSeed(InputMediaPhoto[] mediaGroup,
                                          List<Photo> photos) {
        for (int i = 1; i < photos.size(); i++) {
            InputMediaPhoto photoFile = new InputMediaPhoto(
                    photos.get(i).getContent());
            mediaGroup[i] = photoFile;
        }
    }

    private byte[] getSingleSeedPhotos(Photo photo) {

        return photo.getContent();
    }

    private void sendPhotoWithCaption(long chatId, byte[] photoBytes, String caption, String fileName) {
        SendPhoto request = new SendPhoto(chatId, photoBytes)
                .caption(caption)
                .fileName(fileName)
                .parseMode(ParseMode.valueOf("HTML"));

        bot.execute(request);
    }

    private void sendPhoto(long chatId, byte[] photoBytes, String fileName) {
        SendPhoto request = new SendPhoto(chatId, photoBytes)
                .fileName(fileName);

        bot.execute(request);
    }

    private String getSingleMessageTomatoesContent(Tomatoes tomato) {

        StringBuffer text = new StringBuffer();

        text.append("<b>").append(tomato.getTomatoesName()).append("</b>")
                .append("\n")
                .append("<i>").append(tomato.getCategory()).append("</i>")
                .append("\n")
                .append("- Есть в наличии: ")
                .append("<b>")
                .append(tomato.getIsPresent().getPresent())
                .append("</b>")
                .append("\n")
                .append("- Высота: ")
                .append("<i>").append(tomato.getTomatoesHeight()).append("</i>")
                .append("\n")
                .append("- Диаметр: ")
                .append("<i>").append(tomato.getTomatoesDiameter()).append("</i>")
                .append("\n")
                .append("- Вес: ")
                .append("<i>").append(tomato.getTomatoesFruit()).append("</i>")
                .append("\n")
                .append("- Кашпо: ")
                .append("<i>").append(tomato.getTomatoesFlowerpot()).append("</i>")
                .append("\n")
                .append("- Созревание: ")
                .append("<i>").append(tomato.getTomatoesAgroTech()).append("</i>")
                .append("\n")
                .append("- Описание: ")
                .append("<i>").append(tomato.getTomatoesDescription()).append("</i>")
                .append("\n")
                .append("- Вкус: ")
                .append("<i>").append(tomato.getTomatoesTaste()).append("</i>")
                .append("\n")
                .append("- Особенность: ")
                .append("<i>").append(tomato.getTomatoesSpecificity()).append("</i>");

        return text.toString();
    }

    private String getSingleMessagePeppersContent(Peppers pepper) {
        StringBuffer text = new StringBuffer();

        text.append("<b>").append(pepper.getPeppersName()).append("</b>")
                .append("\n")
                .append("<i>").append(pepper.getCategory()).append("</i>")
                .append("\n")
                .append("- Есть в наличии: ")
                .append("<b>")
                .append(pepper.getIsPresent().getPresent())
                .append("</b>")
                .append("\n")
                .append("- Высота: ")
                .append("<i>").append(pepper.getPeppersHeight()).append("</i>")
                .append("\n")
                .append("- Диаметр: ")
                .append("<i>").append(pepper.getPeppersDiameter()).append("</i>")
                .append("\n")
                .append("- Вес: ")
                .append("<i>").append(pepper.getPeppersFruit()).append("</i>")
                .append("\n")
                .append("- Кашпо: ")
                .append("<i>").append(pepper.getPeppersFlowerpot()).append("</i>")
                .append("\n")
                .append("- Созревание: ")
                .append("<i>").append(pepper.getPeppersAgroTech()).append("</i>")
                .append("\n")
                .append("- Описание: ")
                .append("<i>").append(pepper.getPeppersDescription()).append("</i>")
                .append("\n")
                .append("- Вкус: ")
                .append("<i>").append(pepper.getPeppersTaste()).append("</i>")
                .append("\n")
                .append("- Особенность: ")
                .append("<i>").append(pepper.getPeppersSpecificity()).append("</i>");

        return text.toString();
    }
}