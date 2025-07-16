package accountingApp.tgbot;

import accountingApp.entity.Peppers;
import accountingApp.entity.Seed;
import accountingApp.entity.Tomatoes;
import accountingApp.repository.PeppersRepository;
import accountingApp.repository.TomatoesRepository;
import accountingApp.service.AdminPeppersService;
import accountingApp.service.TomatoesService;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


@Controller
public class TGBotController {

    private final TGBotService tgBotService;

    private final TomatoesRepository tomatoesRepository;
    private final PeppersRepository peppersRepository;
    private final TomatoesService tomatoesService;
    private final AdminPeppersService peppersService;

    @Value("${telegram.chatId}")
    private String chatId;

    @Autowired
    public TGBotController(TGBotService tgBotService,
                           TomatoesRepository tomatoesRepository,
                           PeppersRepository peppersRepository, TomatoesService tomatoesService, AdminPeppersService peppersService) {
        this.tgBotService = tgBotService;
        this.tomatoesRepository = tomatoesRepository;
        this.peppersRepository = peppersRepository;
        this.tomatoesService = tomatoesService;
        this.peppersService = peppersService;
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

    @SneakyThrows
    @PostMapping("/sendTomatoMessage")
    public String sendTGTomatoMessage(@RequestParam String id,
                                      Model model) {

        if (id != null) {
            Tomatoes tomato = tomatoesService.getTomatoById(id);
            if (tomato != null){
                tgBotService.sendTextWithPhotoMessage(chatId, tomato);
            }
        }
        List<Tomatoes> tomatoesList = tomatoesRepository.findAll();
        model.addAttribute("tomatoesList", tomatoesList);
        return "tomatoes";
    }

    @SneakyThrows
    @PostMapping("/sendPepperMessage")
    public String sendTGPepperMessage(@RequestParam String id,
                                      Model model) {

        if (id != null) {
            Peppers pepper = peppersService.getPepperById(id);
            if (pepper != null){
                tgBotService.sendTextWithPhotoMessage(chatId, pepper);
            }
        }
        List<Peppers> peppersList = peppersRepository.findAll();
        model.addAttribute("peppersList", peppersList);
        return "peppers";
    }


}

