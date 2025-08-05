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
//import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
                logger.error("Telegram API error: {}", e.response());
            } else {
                logger.error("Telegram update listener error", e);
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
            logger.error("Failed to send inline keyboard: {}", response.description());
        }
    }

    public void sendTextMessage(long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        SendResponse response = bot.execute(request);
        if (!response.isOk()) {
            logger.error("Failed to send text message: {}", response.description());
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
            logger.error("Error sending seeds info: {}", e.getMessage());
        }
    }

    public void sendTextWithPhotoMessage(String chatId, Seed seed) {
        try {
            long longChatId = Long.parseLong(chatId);
            if (seed instanceof Tomatoes) {
                Tomatoes tomato = (Tomatoes) seed;
                String text = getSingleMessageTomatoesContent(tomato);
                List<Photo> photos = tomato.getPhotos();

                getAllPhotosFromSeed(longChatId,
                        photos,
                        tomato,
                        Tomatoes::getTomatoesName,
                        text);

                if (!photos.isEmpty()) {
                    byte[] photo = photos.get(0).getContent();
                    SendPhoto request = new SendPhoto(chatId, photo)
                            .caption(text)
                            .parseMode(ParseMode.valueOf("HTML"));

                    bot.execute(request);
                }

                InlineKeyboardMarkup keyboard = createInlineKeyboard();
                sendInlineKeyboard(Long.parseLong(chatId), "Welcome!", keyboard);

            } else if (seed instanceof Peppers) {
                Peppers pepper = (Peppers) seed;
                String text = getSingleMessagePeppersContent(pepper);
                List<Photo> photos = pepper.getPhotos();


                getAllPhotosFromSeed(longChatId,
                                     photos,
                                     pepper,
                                     Peppers::getPeppersName,
                                     text);

//                if (!photos.isEmpty()) {
//                    byte[] photo = photos.get(0).getContent();
//                    SendPhoto request = new SendPhoto(chatId, photo)
//                            .caption(text)
//                            .parseMode(ParseMode.valueOf("HTML"));
//
//                    bot.execute(request);
//                }
            }
        } catch (Exception e) {
            logger.error("Error sending photo message: {}", e.getMessage());
        }
    }

    private <T> void getAllPhotosFromSeed(long chatId, List<Photo> photos, T seed,
                                          Function<T, String> nameExtractor, String caption) {
        if (photos == null || photos.isEmpty()) {
            return;
        }

        // Отправляем первую фотографию с подписью
        sendPhotoWithCaption(chatId, photos.get(0).getContent(), caption, nameExtractor.apply(seed) + "_1.jpg");

        // Отправляем остальные фотографии (без подписи)
        for (int i = 1; i < photos.size(); i++) {
            sendPhoto(chatId, photos.get(i).getContent(), nameExtractor.apply(seed) + "_" + (i + 1) + ".jpg");
            try {
                Thread.sleep(500); // Небольшая задержка между отправкой фото
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted while sending photos", e);
            }
        }
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

//        public void sendTextWithPhotoMessage(String chatId, Seed seed) {
////        if (telegramClient == null) {
////            throw new IllegalStateException("TelegramClient is not initialized!");
////        }
//
//        try {
//
//            if (seed instanceof Tomatoes) {
//                Tomatoes tomato = (Tomatoes) seed;
//                String text = getSingleMessageTomatoesContent(tomato);
//                List<Photo> photos = tomato.getPhotos();
//
//                List<InputMedia> mediaGroup = new ArrayList<>();
//
//                byte[] photo = getSingleSeedPhotos(photos.get(0));
//                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
//                        tomato.getTomatoesName() + ".jpg");
//                photoFile.caption(text);
//                photoFile.parseMode(ParseMode.valueOf("HTML"));
//                mediaGroup.add(photoFile);
//
//                getAllPhotosFromSeed(mediaGroup, photos, tomato, Tomatoes::getTomatoesName);
//
//
//                bot.execute(new SendMediaGroup(chatId, mediaGroup));
//
//                String mess = "Welcome!";
//                InlineKeyboardMarkup keyboard = createInlineKeyboard();
//                sendInlineKeyboard(Long.parseLong(chatId), mess, keyboard);
//
//            } else if (seed instanceof Peppers) {
//                Peppers pepper = (Peppers) seed;
//                String text = getSingleMessagePeppersContent(pepper);
//                List<Photo> photos = pepper.getPhotos();
//
//                List<InputMedia> mediaGroup = new ArrayList<>();
//
//                byte[] photo = getSingleSeedPhotos(photos.get(0));
//                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
//                        pepper.getPeppersName() + ".jpg");
//                photoFile.caption(text);
//                photoFile.parseMode(ParseMode.valueOf("HTML"));
//                mediaGroup.add(photoFile);
//
//                getAllPhotosFromSeed(mediaGroup, photos, pepper, Peppers::getPeppersName);
//
//                bot.execute(new SendMediaGroup(chatId, mediaGroup));
//            }
//
//
//        } catch (Exception e) {
//            logger.error("TGBotService.sendTextWithPhotoMessage: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//
//    private <T> void getAllPhotosFromSeed(List<InputMedia> mediaGroup,
//                                          List<Photo> photos,
//                                          T seed,
//                                          Function<T, String> nameExtractor) {
//        for (int i = 1; i < photos.size(); i++) {
//            InputMediaPhoto photoFile = new InputMediaPhoto(
//                    new ByteArrayInputStream(photos.get(i).getContent()),
//                    nameExtractor.apply(seed) + "_" + (i + 1) + ".jpg"
//            );
//            mediaGroup.add(photoFile);
//        }
//    }
//
//    private byte[] getSingleSeedPhotos(Photo photo) {
//
//        return photo.getContent();
//    }


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
































//package accountingApp.tgbot;
//
//import accountingApp.controller.TomatoesController;
//import accountingApp.entity.Peppers;
//import accountingApp.entity.Photo;
//import accountingApp.entity.Seed;
//import accountingApp.entity.Tomatoes;
//import accountingApp.repository.PeppersRepository;
//import accountingApp.repository.TomatoesRepository;
//import lombok.SneakyThrows;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
//import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
//import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
//import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
//import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
//import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
//import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
//import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.meta.generics.TelegramClient;
//
//import javax.annotation.PostConstruct;
//import java.io.ByteArrayInputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.function.Function;
//
//
//@Service
//public class TGBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
//
//    final Logger logger = LoggerFactory.getLogger(TomatoesController.class);
//    @Value("${telegram.bot.token}")
//    private String botToken;
//
//    private final TomatoesRepository tomatoesRepository;
//    private final PeppersRepository peppersRepository;
//
//    private TelegramClient telegramClient;
//
//    @Autowired
//    public TGBotService(TomatoesRepository tomatoesRepository, PeppersRepository peppersRepository) {
//        this.tomatoesRepository = tomatoesRepository;
//        this.peppersRepository = peppersRepository;
//    }
//
//    @Override
//    public String getBotToken() {
//        return botToken;
//    }
//
//    @Override
//    public LongPollingUpdateConsumer getUpdatesConsumer() {
//        return this;
//    }
//
////    @Override
////    public void consume(List<Update> updates) {
////        // Обработка входящих обновлений (если нужно)
////        updates.forEach(this::consume);
////    }
//
//    @SneakyThrows
//    @Override
//    public void consume(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            String responseText = "default";
//            long chatId = update.getMessage().getChatId();
//            InlineKeyboardMarkup keyboard = createInlineKeyboard();
//            sendInlineKeyboard(chatId, responseText, keyboard);
//        }else{
//            System.out.println("consume is out!");
//        }
//    }
//
//
////    @SneakyThrows
////    @Override
////    public void consume(Update update) {
////        String responseText = "default";
////        long chatId = update.getMessage().getChatId();
////        SendMessage message = SendMessage // Create a message object
////                .builder()
////                .chatId(chatId)
////                .text(responseText)
////                .build();
////
////        telegramClient.execute(message);
////
////        try {
////            if (update.hasMessage() && update.getMessage().hasText()) {
////                String messageText = update.getMessage().getText();
////                chatId = update.getMessage().getChatId();
////                System.out.println(chatId);
////
////                if (messageText.equals("/hi")) {
////                    sendInlineKeyboard(chatId, "Выберите действие:", createInlineKeyboard());
////                }
////            } else if (update.hasCallbackQuery()) {
////                String callbackData = update.getCallbackQuery().getData();
////              chatId = update.getCallbackQuery().getMessage().getChatId();
////                String callbackQueryId = update.getCallbackQuery().getId();
////                responseText = "default";
////
////                // Обработка нажатий на кнопки
////                if (callbackData.equals("button1_pressed")) {
////
////                    sendTextMessage(chatId, "Вы нажали кнопку 1");
////                   responseText ="Вы нажали кнопку 1";
////
////                } else if (callbackData.equals("button2_pressed")) {
////                    sendTextMessage(chatId, "Вы нажали кнопку 2");
////                    responseText ="Вы нажали кнопку 2";
////                }
////
////                SendMessage message2 = SendMessage // Create a message object
////                        .builder()
////                        .chatId(chatId)
////                        .text(responseText)
////                        .build();
////
////                telegramClient.execute(message2);
////                // Ответ на callback (обязательно)
////                try {
////                    telegramClient.execute(new AnswerCallbackQuery(callbackQueryId));
////                } catch (TelegramApiException e) {
////                    logger.error("Error answering callback query: ", e);
////                }
////            }
////        } catch (Exception e) {
////            logger.error("TGBotService.consume error: ", e);
////        }
////
////    }
//
//
//    public void sendInlineKeyboard(long chatId,
//                                   String text,
//                                   InlineKeyboardMarkup keyboard) {
//        SendMessage message = new SendMessage(String.valueOf(chatId), text);
//
//        message.setReplyMarkup(keyboard);
//
//        try {
//            telegramClient.execute(message);
//        } catch (TelegramApiException e) {
//            logger.error("TGBotService.sendInlineKeyboard: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public void sendTextMessage(long chatId, String text) {
//        SendMessage message = new SendMessage(String.valueOf(chatId), text);
//
//
//        try {
//            telegramClient.execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public InlineKeyboardMarkup createInlineKeyboard() {
//        // Создаем первую кнопку
//        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
//                .text("Томаты")
//                .callbackData("button1_pressed")
//                .build();
//
//        // Создаем вторую кнопку
//        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
//                .text("Перцы")
//                .callbackData("button2_pressed")
//                .build();
//
//        // Создаем ряд кнопок
//        InlineKeyboardRow row = new InlineKeyboardRow(button1, button2);
//
//        // Создаем клавиатуру с этим рядом
//        return InlineKeyboardMarkup.builder()
//                .keyboard(Collections.singletonList(row))
//                .build();
//    }
//
//    @PostConstruct
//    public void init() {
//        this.telegramClient = new OkHttpTelegramClient(getBotToken());
//    }
//
//    public void sendAllSeedsTextAndPhotosMessage(String chatId) {
//        if (telegramClient == null) {
//            throw new IllegalStateException("TelegramClient is not initialized!");
//        }
//
//        try {
//            List<Seed> seedList = new ArrayList<>();
//
//            List<Tomatoes> tomatoesList = tomatoesRepository.findAll();
//            List<Peppers> peppersList = peppersRepository.findAll();
//
//            seedList.addAll(tomatoesList);
//            seedList.addAll(peppersList);
//
//            for (int i = 0; i < 3; i++) {
//                Seed s = seedList.get(i);
//                sendTextWithPhotoMessage(chatId, s);
//                Thread.sleep(1000);
//            }
//
//        } catch (Exception e) {
//            logger.error("TGBotService.sendAllSeedsTextAndPhotosMessage: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//
//
//    public void sendTextWithPhotoMessage(String chatId, Seed seed) {
//        if (telegramClient == null) {
//            throw new IllegalStateException("TelegramClient is not initialized!");
//        }
//
//        try {
//
//            if (seed instanceof Tomatoes) {
//                Tomatoes tomato = (Tomatoes) seed;
//                String text = getSingleMessageTomatoesContent(tomato);
//                List<Photo> photos = tomato.getPhotos();
//
//                List<InputMedia> mediaGroup = new ArrayList<>();
//
//                byte[] photo = getSingleSeedPhotos(photos.get(0));
//                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
//                        tomato.getTomatoesName() + ".jpg");
//                photoFile.setCaption(text);
//                photoFile.setParseMode("HTML");
//                mediaGroup.add(photoFile);
//
//                getAllPhotosFromSeed(mediaGroup, photos, tomato, Tomatoes::getTomatoesName);
//
//
//                telegramClient.execute(new SendMediaGroup(chatId, mediaGroup));
//
//                String mess = "Welcome!";
//                InlineKeyboardMarkup keyboard = createInlineKeyboard();
//                sendInlineKeyboard(Long.parseLong(chatId), mess, keyboard);
//
//            } else if (seed instanceof Peppers) {
//                Peppers pepper = (Peppers) seed;
//                String text = getSingleMessagePeppersContent(pepper);
//                List<Photo> photos = pepper.getPhotos();
//
//                List<InputMedia> mediaGroup = new ArrayList<>();
//
//                byte[] photo = getSingleSeedPhotos(photos.get(0));
//                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
//                        pepper.getPeppersName() + ".jpg");
//                photoFile.setCaption(text);
//                photoFile.setParseMode("HTML");
//                mediaGroup.add(photoFile);
//
//                getAllPhotosFromSeed(mediaGroup, photos, pepper, Peppers::getPeppersName);
//
//                telegramClient.execute(new SendMediaGroup(chatId, mediaGroup));
//            }
//
//
//        } catch (Exception e) {
//            logger.error("TGBotService.sendTextWithPhotoMessage: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//
//    private <T> void getAllPhotosFromSeed(List<InputMedia> mediaGroup,
//                                          List<Photo> photos,
//                                          T seed,
//                                          Function<T, String> nameExtractor) {
//        for (int i = 1; i < photos.size(); i++) {
//            InputMediaPhoto photoFile = new InputMediaPhoto(
//                    new ByteArrayInputStream(photos.get(i).getContent()),
//                    nameExtractor.apply(seed) + "_" + (i + 1) + ".jpg"
//            );
//            mediaGroup.add(photoFile);
//        }
//    }
//
//    private String getSingleMessageTomatoesContent(Tomatoes tomato) {
//
//        StringBuffer text = new StringBuffer();
//
//        text.append("<b>").append(tomato.getTomatoesName()).append("</b>")
//                .append("\n")
//                .append("<i>").append(tomato.getCategory()).append("</i>")
//                .append("\n")
//                .append("- Есть в наличии: ")
//                .append("<b>")
//                .append(tomato.getIsPresent().getPresent())
//                .append("</b>")
//                .append("\n")
//                .append("- Высота: ")
//                .append("<i>").append(tomato.getTomatoesHeight()).append("</i>")
//                .append("\n")
//                .append("- Диаметр: ")
//                .append("<i>").append(tomato.getTomatoesDiameter()).append("</i>")
//                .append("\n")
//                .append("- Вес: ")
//                .append("<i>").append(tomato.getTomatoesFruit()).append("</i>")
//                .append("\n")
//                .append("- Кашпо: ")
//                .append("<i>").append(tomato.getTomatoesFlowerpot()).append("</i>")
//                .append("\n")
//                .append("- Созревание: ")
//                .append("<i>").append(tomato.getTomatoesAgroTech()).append("</i>")
//                .append("\n")
//                .append("- Описание: ")
//                .append("<i>").append(tomato.getTomatoesDescription()).append("</i>")
//                .append("\n")
//                .append("- Вкус: ")
//                .append("<i>").append(tomato.getTomatoesTaste()).append("</i>")
//                .append("\n")
//                .append("- Особенность: ")
//                .append("<i>").append(tomato.getTomatoesSpecificity()).append("</i>");
//
//        return text.toString();
//    }
//
//    private String getSingleMessagePeppersContent(Peppers pepper) {
//
//        StringBuffer text = new StringBuffer();
//
//        text.append("<b>").append(pepper.getPeppersName()).append("</b>")
//                .append("\n")
//                .append("<i>").append(pepper.getCategory()).append("</i>")
//                .append("\n")
//                .append("- Есть в наличии: ")
//                .append("<b>")
//                .append(pepper.getIsPresent().getPresent())
//                .append("</b>")
//                .append("\n")
//                .append("- Высота: ")
//                .append("<i>").append(pepper.getPeppersHeight()).append("</i>")
//                .append("\n")
//                .append("- Диаметр: ")
//                .append("<i>").append(pepper.getPeppersDiameter()).append("</i>")
//                .append("\n")
//                .append("- Вес: ")
//                .append("<i>").append(pepper.getPeppersFruit()).append("</i>")
//                .append("\n")
//                .append("- Кашпо: ")
//                .append("<i>").append(pepper.getPeppersFlowerpot()).append("</i>")
//                .append("\n")
//                .append("- Созревание: ")
//                .append("<i>").append(pepper.getPeppersAgroTech()).append("</i>")
//                .append("\n")
//                .append("- Описание: ")
//                .append("<i>").append(pepper.getPeppersDescription()).append("</i>")
//                .append("\n")
//                .append("- Вкус: ")
//                .append("<i>").append(pepper.getPeppersTaste()).append("</i>")
//                .append("\n")
//                .append("- Особенность: ")
//                .append("<i>").append(pepper.getPeppersSpecificity()).append("</i>");
//
//        return text.toString();
//    }
//
//    private byte[] getSingleSeedPhotos(Photo photo) {
//
//        return photo.getContent();
//    }
//
//
//}
