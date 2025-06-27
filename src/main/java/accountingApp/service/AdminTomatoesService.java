package accountingApp.service;

import accountingApp.entity.IsPresent;
import accountingApp.entity.Peppers;
import accountingApp.entity.Tomatoes;
import accountingApp.entity.TomatoesCategory;
import accountingApp.repository.TomatoesRepository;
import accountingApp.usefulmethods.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AdminTomatoesService {

    final Logger logger = LoggerFactory.getLogger(AdminTomatoesService.class);
    private static final int DEFAULT_PAGE_LIMIT = 5;
    private static final TomatoesCategory[] TOMATOES_CATEGORIES = TomatoesCategory.values();
    private static final IsPresent[] IS_PRESENTS = IsPresent.values();
    private final TomatoesService tomatoesService;
    private final TomatoesRepository tomatoesRepository;
    private final Checker checker;

    @Autowired
    public AdminTomatoesService(TomatoesService tomatoesService
            , TomatoesRepository tomatoesRepository
            , Checker checker) {
        this.tomatoesService = tomatoesService;
        this.tomatoesRepository = tomatoesRepository;
        this.checker = checker;
    }

//    @GetMapping("/tutorials")
//    public String getAll(Model model, @RequestParam(required = false) String keyword,
//                         @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
//        try {
//            List<Tutorial> tutorials = new ArrayList<Tutorial>();
//            Pageable paging = PageRequest.of(page - 1, size);
//
//            Page<Tutorial> pageTuts;
//            if (keyword == null) {
//                pageTuts = tutorialRepository.findAll(paging);
//            } else {
//                pageTuts = tutorialRepository.findByTitleContainingIgnoreCase(keyword, paging);
//                model.addAttribute("keyword", keyword);
//            }
//
//            tutorials = pageTuts.getContent();
//
//            model.addAttribute("tutorials", tutorials);
//            model.addAttribute("currentPage", pageTuts.getNumber() + 1);
//            model.addAttribute("totalItems", pageTuts.getTotalElements());
//            model.addAttribute("totalPages", pageTuts.getTotalPages());
//            model.addAttribute("pageSize", size);
//        } catch (Exception e) {
//            model.addAttribute("message", e.getMessage());
//        }
//
//        return "tutorials";
//    }

    public Model prepareTomatoesModelWithPages(String pageNumber
            , Integer limit
            , Model model) {

        try{

            int convertedPageNumber = Integer.parseInt(pageNumber);

            int pageLimit = limit != null ? limit : DEFAULT_PAGE_LIMIT;

            List<Tomatoes> tomatoesList = new ArrayList<>();

            Pageable pageable = PageRequest.of(convertedPageNumber, pageLimit);

            Page<Tomatoes>  tomatoesPages = tomatoesRepository.findAll(pageable);

            List<String> categoryList = getCategoryList();
            List<String> isPresentList = getIsPresentList();

            model.addAttribute("tomatoesList", tomatoesList);
            model.addAttribute("tomatoesPages", tomatoesPages.getContent());
            model.addAttribute("pageNumber", convertedPageNumber);
            model.addAttribute("totalPages", tomatoesPages.getTotalPages());
            model.addAttribute("pageLimit", pageLimit);
            model.addAttribute("totalItems", tomatoesPages.getTotalElements());
            model.addAttribute("categoryList", categoryList);
            model.addAttribute("isPresentList", isPresentList);

            return model;

        }catch (Exception e){
            logger.warn(e.getMessage());
            return   model;
        }
    }

    private List<String> getIsPresentList() {
        return Arrays.stream(IS_PRESENTS)
                .map(IsPresent::getPresent)
                .collect(Collectors.toList());
    }

    private List<String> getCategoryList() {
        return Arrays.stream(TOMATOES_CATEGORIES)
                .map(TomatoesCategory::getCategory)
                .collect(Collectors.toList());
    }

    public List<Tomatoes> findTomatoesForAdmin(String attr) {

        if (checker.checkAttribute(attr)) {
            logger.error("*** AdminTomatoesService.findTomatoesForAdmin():" +
                    "  WRONG DB VALUES*** ");
            return new ArrayList<>();
        }

        try {
            List<Tomatoes> tomatoes = tomatoesRepository.findAll();
            List<Tomatoes> tomatoesList = new ArrayList<>();
            String attrTrim = attr.trim().toLowerCase(Locale.ROOT);

            for (Tomatoes t : tomatoes
            ) {
                if (t.getCategory().getCategory().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesName().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesHeight().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesDiameter().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesFruit().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesFlowerpot().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesAgroTech().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesDescription().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesTaste().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesSpecificity().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getIsPresent().getPresent().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if ((t.getIdCount() + "").toLowerCase(Locale.ROOT).
                        contains(attrTrim)) {
                    tomatoesList.add(t);
                }
            }

            if (tomatoesList.isEmpty()) {
                logger.debug("*** AdminTomatoesService.findTomatoesForAdmin():  DATA NOT FOUND IN DB***");
                return new ArrayList<>();
            }

            return tomatoesList;

        } catch (Exception e) {
            logger.error("*** AdminTomatoesService.findTomatoesForAdmin(): wrong DB's values! *** "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }
}
