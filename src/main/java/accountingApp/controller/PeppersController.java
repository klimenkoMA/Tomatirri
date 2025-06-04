package accountingApp.controller;

import accountingApp.service.AdminPeppersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Класс для переадресации введенных пользователем данных в БД, получение ответа
 * из БД, передача ответа для отображения на веб-странице
 */
@RequestMapping
@Controller
public class PeppersController {

    final Logger logger = LoggerFactory.getLogger(PeppersController.class);

    private final AdminPeppersService adminPeppersService;

    @Autowired
    public PeppersController(AdminPeppersService adminPeppersService) {
        this.adminPeppersService = adminPeppersService;
    }

    @GetMapping("/pepperscatalog")
    public String getPeppers(Model model) {
        adminPeppersService.preparePeppersModel(model);
        return "pepperscatalog";
    }

    @GetMapping("/peppers")
    public String getPeppersForCrud(Model model) {
        adminPeppersService.preparePeppersModel(model);
        return "peppers";
    }

    @PostMapping("/addpepper")
    public String addPepper(@RequestParam(required = false) String category
            , @RequestParam String peppersName
            , @RequestParam String peppersHeight
            , @RequestParam String peppersDiameter
            , @RequestParam String peppersFruit
            , @RequestParam String peppersFlowerpot
            , @RequestParam String peppersAgroTech
            , @RequestParam String peppersDescription
            , @RequestParam String peppersTaste
            , @RequestParam(required = false) String peppersSpecificity
            , @RequestParam(required = false) String isPresent
            , @RequestParam(value = "content", required = false) MultipartFile[] content
            , Model model
    ) {
        model = adminPeppersService.addNewPepper(category
                , peppersName
                , peppersHeight
                , peppersDiameter
                , peppersFruit
                , peppersFlowerpot
                , peppersAgroTech
                , peppersDescription
                , peppersTaste
                , peppersSpecificity
                , isPresent
                , content
                , model);

        return getPeppersForCrud(model);
    }


}
