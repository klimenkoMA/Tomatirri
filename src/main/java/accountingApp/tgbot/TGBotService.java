package accountingApp.tgbot;

import accountingApp.controller.TomatoesController;
import accountingApp.entity.Peppers;
import accountingApp.entity.Photo;
import accountingApp.entity.Seed;
import accountingApp.entity.Tomatoes;
import accountingApp.repository.PeppersRepository;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
public class TGBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    final Logger logger = LoggerFactory.getLogger(TomatoesController.class);
    @Value("${telegram.bot.token}")
    private String botToken;

    private final TomatoesRepository tomatoesRepository;
    private final PeppersRepository peppersRepository;

    private TelegramClient telegramClient;

    @Autowired
    public TGBotService(TomatoesRepository tomatoesRepository, PeppersRepository peppersRepository) {
        this.tomatoesRepository = tomatoesRepository;
        this.peppersRepository = peppersRepository;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {
        // Обработка входящих обновлений (если нужно)
        updates.forEach(this::consume);
    }

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendInlineKeyboard(chatId, "Выберите действие:", createInlineKeyboard());
            } else {
                // Обработка других команд и сообщений
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            // Обработка нажатий на кнопки
            if(callbackData.equals("button1")){
                sendMessage(chatId, "Вы нажали кнопку 1");
            } else if (callbackData.equals("button2")){
                sendMessage(chatId, "Вы нажали кнопку 2");
            }
        }

    }

    private void sendInlineKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Кнопка 1");
        button1.setCallbackData("button1");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Кнопка 2");
        button2.setCallbackData("button2");
        rowInline.add(button1);
        rowInline.add(button2);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    @PostConstruct
    public void init() {
        this.telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    public void sendAllSeedsTextAndPhotosMessage(String chatId){
        if (telegramClient == null) {
            throw new IllegalStateException("TelegramClient is not initialized!");
        }

        try{
            List<Seed> seedList = new ArrayList<>();

            List<Tomatoes> tomatoesList = tomatoesRepository.findAll();
            List<Peppers> peppersList = peppersRepository.findAll();

            seedList.addAll(tomatoesList);
            seedList.addAll(peppersList);

            for (int i = 0; i < 3; i++) {
                Seed s = seedList.get(i);
                sendTextWithPhotoMessage(chatId, s);
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            logger.error("TGBotService.sendAllSeedsTextAndPhotosMessage: " + e.getMessage());
            e.printStackTrace();
        }

    }


    public void sendTextWithPhotoMessage(String chatId, Seed seed) {
        if (telegramClient == null) {
            throw new IllegalStateException("TelegramClient is not initialized!");
        }

        try {

            if (seed instanceof Tomatoes){
                Tomatoes tomato = (Tomatoes) seed;
                String text = getSingleMessageTomatoesContent(tomato);
                List<Photo> photos = tomato.getPhotos();

                List<InputMedia> mediaGroup = new ArrayList<>();

                byte[] photo = getSingleSeedPhotos(photos.get(0));
                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
                        tomato.getTomatoesName() + ".jpg");
                photoFile.setCaption(text);
                photoFile.setParseMode("HTML");
                mediaGroup.add(photoFile);

                getAllPhotosFromSeed(mediaGroup, photos, tomato, Tomatoes::getTomatoesName);

                telegramClient.execute(new SendMediaGroup(chatId, mediaGroup));
            }else if(seed instanceof Peppers){
                Peppers pepper = (Peppers) seed;
                String text = getSingleMessagePeppersContent(pepper);
                List<Photo> photos = pepper.getPhotos();

                List<InputMedia> mediaGroup = new ArrayList<>();

                byte[] photo = getSingleSeedPhotos(photos.get(0));
                InputMediaPhoto photoFile = new InputMediaPhoto(new ByteArrayInputStream(photo),
                        pepper.getPeppersName() + ".jpg");
                photoFile.setCaption(text);
                photoFile.setParseMode("HTML");
                mediaGroup.add(photoFile);

                getAllPhotosFromSeed(mediaGroup, photos, pepper, Peppers::getPeppersName);

                telegramClient.execute(new SendMediaGroup(chatId, mediaGroup));
            }


        } catch (Exception e) {
            logger.error("TGBotService.sendTextWithPhotoMessage: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private <T> void getAllPhotosFromSeed(List<InputMedia> mediaGroup,
                                          List<Photo> photos,
                                          T seed,
                                          Function<T, String> nameExtractor) {
        for (int i = 1; i < photos.size(); i++) {
            InputMediaPhoto photoFile = new InputMediaPhoto(
                    new ByteArrayInputStream(photos.get(i).getContent()),
                    nameExtractor.apply(seed) + "_" + (i + 1) + ".jpg"
            );
            mediaGroup.add(photoFile);
        }
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

    private byte[] getSingleSeedPhotos(Photo photo) {

        return photo.getContent();
    }


}
