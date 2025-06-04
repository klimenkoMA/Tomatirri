package accountingApp.service;

import accountingApp.entity.IsPresent;
import accountingApp.entity.Peppers;
import accountingApp.entity.PeppersCategory;
import accountingApp.repository.PeppersRepository;
import accountingApp.usefulmethods.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPeppersService {

    final Logger logger = LoggerFactory.getLogger(AdminPeppersService.class);
    private static final PeppersCategory[] PEPPERS_CATEGORIES = PeppersCategory.values();
    private static final IsPresent[] IS_PRESENTS = IsPresent.values();
    private final PeppersRepository peppersRepository;
    private final Checker checker;

    @Autowired
    public AdminPeppersService(PeppersRepository peppersRepository
            , Checker checker) {
        this.peppersRepository = peppersRepository;
        this.checker = checker;
    }


    public void preparePeppersModel(Model model) {
        List<Peppers> peppersList = peppersRepository.findAll()
                .stream()
                .sorted(Comparator.comparingLong(Peppers::getIdCount).reversed())
                .collect(Collectors.toList());

        List<String> categoryList = getCategoryList();
        List<String> isPresentList = getIsPresentList();

        model.addAttribute("peppersList", peppersList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("isPresentList", isPresentList);
    }

    private List<String> getCategoryList() {
        return Arrays.stream(PEPPERS_CATEGORIES)
                .map(PeppersCategory::getCategory)
                .collect(Collectors.toList());
    }

    private List<String> getIsPresentList() {
        return Arrays.stream(IS_PRESENTS)
                .map(IsPresent::getPresent)
                .collect(Collectors.toList());
    }

    public Model addNewPepper(String category
            , String peppersName
            , String peppersHeight
            , String peppersDiameter
            , String peppersFruit
            , String peppersFlowerpot
            , String peppersAgroTech
            , String peppersDescription
            , String peppersTaste
            , String peppersSpecificity
            , String isPresent
            , MultipartFile[] content
            , Model model){

        if (category == null
                || checker.checkAttribute(peppersName)
                || checker.checkAttribute(peppersHeight)
                || checker.checkAttribute(peppersDiameter)
                || checker.checkAttribute(peppersFruit)
                || checker.checkAttribute(peppersFlowerpot)
                || checker.checkAttribute(peppersAgroTech)
                || checker.checkAttribute(peppersDescription)
                || checker.checkAttribute(peppersTaste)
        ) {
            logger.warn("*** AdminPeppersService.addNewPepper():" +
                    "  Attribute has a null value! ***" +
                    "\ncategory: " + category +
                    "\ntomatoesName: " + peppersName +
                    "\ntomatoesHeight: " + peppersHeight +
                    "\ntomatoesDiameter: " + peppersDiameter +
                    "\ntomatoesFruit: " + peppersFruit +
                    "\ntomatoesFlowerpot: " + peppersFlowerpot +
                    "\ntomatoesAgroTech: " + peppersAgroTech +
                    "\ntomatoesDescription: " + peppersDescription +
                    "\ntomatoesTaste: " + peppersTaste
            );
            return model;
        }
        return model;
    }

    public List<Peppers> findPeppersForAdmin(String attr){

        if (checker.checkAttribute(attr)) {
            logger.error("*** AdminPeppersService.findPeppersForAdmin():" +
                    "  WRONG DB VALUES*** ");
            return new ArrayList<>();
        }

        try {
            List<Peppers> peppers = peppersRepository.findAll();
            List<Peppers> peppersList = new ArrayList<>();
            String attrTrim = attr.trim().toLowerCase(Locale.ROOT);

            for (Peppers p : peppers
            ) {
                if (p.getCategory().getCategory().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersName().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersHeight().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersDiameter().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersFruit().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersFlowerpot().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersAgroTech().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersDescription().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersTaste().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getPeppersSpecificity().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if (p.getIsPresent().getPresent().toLowerCase(Locale.ROOT)
                        .contains(attrTrim)) {
                    peppersList.add(p);
                } else if ((p.getIdCount() + "").toLowerCase(Locale.ROOT).
                        contains(attrTrim)) {
                    peppersList.add(p);
                }
            }

            if (peppersList.isEmpty()) {
                logger.debug("*** AdminPeppersService.findPeppersForAdmin():  DATA NOT FOUND IN DB***");
                return new ArrayList<>();
            }

            return peppersList;

        } catch (Exception e) {
            logger.error("*** AdminPeppersService.findPeppersForAdmin(): wrong DB's values! *** "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

}
