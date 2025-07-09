package accountingApp.tgbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TGBotController {



    private final TGBotService tgBotService;

    @Value("${telegram.chatId}")
    private String chatId;

    @Autowired
    public TGBotController(TGBotService tgBotService) {
        this.tgBotService = tgBotService;
    }

    @PostMapping("/sendCommonMessage")
    public String sendTGCommonMessage(@RequestParam(required = false) String message) {

        message = message != null ? message : "Default message";

        try {
            tgBotService.sendTextMessage(chatId, message);
            return "redirect:/main";
        } catch (Exception e) {
            return "Failed to send message: " + e.getMessage();
        }
    }

}

