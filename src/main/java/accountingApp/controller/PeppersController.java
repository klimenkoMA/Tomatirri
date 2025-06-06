package accountingApp.controller;

import accountingApp.service.AdminPeppersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Класс для переадресации введенных пользователем данных в БД, получение ответа
 * из БД, передача ответа для отображения на веб-странице
 */
@RequestMapping
@Controller
public class PeppersController {

    private final AdminPeppersService adminPeppersService;

    @Autowired
    public PeppersController(AdminPeppersService adminPeppersService) {
        this.adminPeppersService = adminPeppersService;
    }

    @GetMapping("/pepperscatalog")
    public String getPeppers(Model model) {
        model = adminPeppersService.preparePeppersModel(model);
        return "pepperscatalog";
    }

    @GetMapping("/peppers")
    @Secured("ROLE_ADMIN")
    public String getPeppersForCrud(Model model) {
        model = adminPeppersService.preparePeppersModel(model);
        return "peppers";
    }

    @PostMapping("/addpepper")
    @Secured("ROLE_ADMIN")
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

    @PostMapping("/updatepepper")
    @Secured("ROLE_ADMIN")
    public String updatePepper(@RequestParam String id
            , @RequestParam(required = false) String category
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
        model = adminPeppersService.updateNewPepper(id
                , category
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

    @PostMapping("/deletepepper")
    @Secured("ROLE_ADMIN")
    public String deleteOnePepper(@RequestParam String id
            , Model model) {

        model = adminPeppersService.deletePepper(id, model);
        return "peppers";
    }

    @GetMapping("/peppers/download/{id}")
    public ResponseEntity<byte[]> downloadAllPeppersPhoto(@PathVariable String id) {

        return adminPeppersService.downloadPeppersPhoto(id);

    }

    @PostMapping("/adminfindpepper")
    @Secured("ROLE_ADMIN")
    public String adminFindSomePeppers(@RequestParam String attr
            , Model model) {

        model = adminPeppersService.findPeppersForAdmin(attr, model);
        return "peppers";
    }

    @PostMapping("/findpepper")
    public String findSomePeppers(@RequestParam String attr
            , Model model) {

        model = adminPeppersService.findPeppersForAdmin(attr, model);
        return "peppers";
    }

    @PostMapping("/findpepperbycategory")
    @Secured("ROLE_ADMIN")
    public String findSomePeppersByCategory(@RequestParam String category
            , Model model) {

        model = adminPeppersService.findPeppersListByCategory(category, model);
        return "peppers";
    }

    @PostMapping("/peppers/{idCount}")
    public String getFullCard(@RequestParam Long idCount
            , Model model) {

        model = adminPeppersService.getFullCard(idCount, model);
        return "peppers";
    }
}
