package accountingApp.tgbot;

import accountingApp.entity.Peppers;
import accountingApp.entity.Seed;
import accountingApp.entity.Tomatoes;
import accountingApp.repository.PeppersRepository;
import accountingApp.repository.TomatoesRepository;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;


@Controller
public class TGBotController {

    private final TGBotService tgBotService;

    private final TomatoesRepository tomatoesRepository;
    private final PeppersRepository peppersRepository;


    @Value("${telegram.chatId}")
    private String chatId;

    @Autowired
    public TGBotController(TGBotService tgBotService,
                           TomatoesRepository tomatoesRepository,
                           PeppersRepository peppersRepository) {
        this.tgBotService = tgBotService;
        this.tomatoesRepository = tomatoesRepository;
        this.peppersRepository = peppersRepository;
    }

    @SneakyThrows
    @PostMapping("/sendCommonMessage")
    public String sendTGCommonMessage(Model model) {

        tgBotService.sendAllSeedsTextAndPhotosMessage(chatId);

        List<Seed> seedList = new ArrayList<>();

        List<Tomatoes> tomatoesList = tomatoesRepository.findAll();
        List<Peppers> peppersList = peppersRepository.findAll();

        seedList.addAll(tomatoesList);
        seedList.addAll(peppersList);

        model.addAttribute("seedList", seedList);
        model.addAttribute("tomatoesList", tomatoesList);
        model.addAttribute("peppersList", peppersList);

        return "main";
    }


}

