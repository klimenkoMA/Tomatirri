package accountingApp.controller;

import accountingApp.entity.*;
import accountingApp.service.*;
import accountingApp.usefulmethods.Checker;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Класс для переадресации введенных пользователем данных в БД, получение ответа
 * из БД, передача ответа для отображения на веб-странице
 */
@RequestMapping
@Controller
public class TomatoesController {

    final Logger logger = LoggerFactory.getLogger(TomatoesController.class);
    private static final TomatoesCategory[] TOMATOES_CATEGORIES = TomatoesCategory.values();
    private static final IsPresent[] IS_PRESENTS = IsPresent.values();
    private final TomatoesService tomatoesService;
    private final AdminTomatoesService adminTomatoesService;
    private final Checker checker;

    @Autowired
    public TomatoesController(TomatoesService tomatoesService
            , AdminTomatoesService adminTomatoesService
            , Checker checker) {
        this.tomatoesService = tomatoesService;
        this.adminTomatoesService = adminTomatoesService;
        this.checker = checker;
    }

    @GetMapping("/tomatoescatalogwithpages/?{pageNumber}")
    public String getPeppersWithPage(@PathVariable("pageNumber") int pageNumber
            , @RequestParam(required = false) Integer limit
            , Model model) {
        model = adminTomatoesService.prepareTomatoesModelWithPages(pageNumber, limit, model);
        return "tomatoescatalogwithpages";
    }

    @GetMapping("/tomatoescatalog")
    public String getTomatoes(Model model) {
        List<Tomatoes> tomatoesList = tomatoesService.findAllTomatoes()
                .stream()
                .sorted(Comparator.comparingLong(Tomatoes::getIdCount).reversed())
                .collect(Collectors.toList());

        List<String> categoryList = Arrays.stream(TOMATOES_CATEGORIES)
                .map(TomatoesCategory::getCategory)
                .collect(Collectors.toList());

        List<String> isPresentList = Arrays.stream(IS_PRESENTS)
                .map(IsPresent::getPresent)
                .collect(Collectors.toList());

        model.addAttribute("tomatoesList", tomatoesList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("isPresentList", isPresentList);

        return "tomatoescatalog";
    }

    @GetMapping("/tomatoes")
    @Secured("ROLE_ADMIN")
    public String getTomatoesForCRUD(Model model) {
        List<Tomatoes> tomatoesList = tomatoesService.findAllTomatoes()
                .stream()
                .sorted(Comparator.comparingLong(Tomatoes::getIdCount).reversed())
                .collect(Collectors.toList());

        List<String> categoryList = Arrays.stream(TOMATOES_CATEGORIES)
                .map(TomatoesCategory::getCategory)
                .collect(Collectors.toList());

        List<String> isPresentList = Arrays.stream(IS_PRESENTS)
                .map(IsPresent::getPresent)
                .collect(Collectors.toList());

        model.addAttribute("tomatoesList", tomatoesList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("isPresentList", isPresentList);

        return "tomatoes";
    }

    @PostMapping("/addtomato")
    @Secured("ROLE_ADMIN")
    public String addTomato(@RequestParam(required = false) String category
            , @RequestParam String tomatoesName
            , @RequestParam String tomatoesHeight
            , @RequestParam String tomatoesDiameter
            , @RequestParam String tomatoesFruit
            , @RequestParam String tomatoesFlowerpot
            , @RequestParam String tomatoesAgroTech
            , @RequestParam String tomatoesDescription
            , @RequestParam String tomatoesTaste
            , @RequestParam(required = false) String tomatoesSpecificity
            , @RequestParam(required = false) String isPresent
            , @RequestParam(value = "content", required = false) MultipartFile[] content
            , Model model
    ) {
        if (category == null
                || checker.checkAttribute(tomatoesName)
                || checker.checkAttribute(tomatoesHeight)
                || checker.checkAttribute(tomatoesDiameter)
                || checker.checkAttribute(tomatoesFruit)
                || checker.checkAttribute(tomatoesFlowerpot)
                || checker.checkAttribute(tomatoesAgroTech)
                || checker.checkAttribute(tomatoesDescription)
                || checker.checkAttribute(tomatoesTaste)
        ) {
            logger.warn("*** TomatoesController.addTomato():" +
                    "  Attribute has a null value! ***" +
                    "\ncategory: " + category +
                    "\ntomatoesName: " + tomatoesName +
                    "\ntomatoesHeight: " + tomatoesHeight +
                    "\ntomatoesDiameter: " + tomatoesDiameter +
                    "\ntomatoesFruit: " + tomatoesFruit +
                    "\ntomatoesFlowerpot: " + tomatoesFlowerpot +
                    "\ntomatoesAgroTech: " + tomatoesAgroTech +
                    "\ntomatoesDescription: " + tomatoesDescription +
                    "\ntomatoesTaste: " + tomatoesTaste
            );
            return getTomatoesForCRUD(model);
        }

        String tomatoesNameTrim = tomatoesName.trim();
        String tomatoesHeightTrim = tomatoesHeight.trim();
        String tomatoesDiameterTrim = tomatoesDiameter.trim();
        String tomatoesFruitTrim = tomatoesFruit.trim();
        String tomatoesFlowerpotTrim = tomatoesFlowerpot.trim();
        String tomatoesAgroTechTrim = tomatoesAgroTech.trim();
        String tomatoesDescriptionTrim = tomatoesDescription.trim();
        String tomatoesTasteTrim = tomatoesTaste.trim();
        String tomatoesSpecificityTrim = "\uD83C\uDF45 \uD83C\uDF45 \uD83C\uDF45";

        if (!checker.checkAttribute(tomatoesSpecificity)) {
            tomatoesSpecificityTrim = tomatoesSpecificity.trim();
        }

        try {
            TomatoesCategory tomatoesCategory = Arrays.stream(TOMATOES_CATEGORIES)
                    .filter(cat -> cat.getCategory().equals(category))
                    .findFirst()
                    .orElse(TomatoesCategory.Штамбовый);

            IsPresent tomatoIsPresent = Arrays.stream(IS_PRESENTS)
                    .filter(isp -> isp.getPresent().equals(isPresent))
                    .findFirst()
                    .orElse(IsPresent.ДА);

            Tomatoes tomato = new Tomatoes(tomatoesCategory
                    , tomatoesNameTrim
                    , tomatoesHeightTrim
                    , tomatoesDiameterTrim
                    , tomatoesFruitTrim
                    , tomatoesFlowerpotTrim
                    , tomatoesAgroTechTrim
                    , tomatoesDescriptionTrim
                    , tomatoesTasteTrim
                    , tomatoesSpecificityTrim
                    , tomatoIsPresent
            );

            tomatoesService.addNewTomato(tomato);

            ObjectId objectId = tomato.getId();
            List<Photo> photos = new ArrayList<>();
            long count = 0;

            createPhotoList(content, photos, count);

            tomato.setPhotos(photos);

            long idCount;
            List<Tomatoes> tomatoesList = tomatoesService.findAllTomatoes();

            Set<Long> idSet = tomatoesList.stream()
                    .map(Tomatoes::getIdCount)
                    .collect(Collectors.toSet());

            idCount = LongStream.rangeClosed(1, idSet.size() + +1_000_000_000_000_000_000L)
                    .filter(idc -> !idSet.contains(idc))
                    .findFirst()
                    .orElse(0L);

            Map<ObjectId, Long> idLongMap = new HashMap<>();
            idLongMap.put(objectId, idCount);
            tomato.setIdMap(idLongMap);
            tomato.setIdCount(idCount);
            tomatoesService.addNewTomato(tomato);

            return getTomatoesForCRUD(model);
        } catch (Exception e) {
            logger.error("*** TomatoesController.addTomato():" +
                    "  wrong DB's values! ***" + e.getMessage());
            return getTomatoesForCRUD(model);
        }
    }

    @PostMapping("/updatetomato")
    @Secured("ROLE_ADMIN")
    public String updateTomato(@RequestParam String id
            , @RequestParam(required = false) String category
            , @RequestParam(required = false) String tomatoesName
            , @RequestParam(required = false) String tomatoesHeight
            , @RequestParam(required = false) String tomatoesDiameter
            , @RequestParam(required = false) String tomatoesFruit
            , @RequestParam(required = false) String tomatoesFlowerpot
            , @RequestParam(required = false) String tomatoesAgroTech
            , @RequestParam(required = false) String tomatoesDescription
            , @RequestParam(required = false) String tomatoesTaste
            , @RequestParam(required = false) String tomatoesSpecificity
            , @RequestParam(required = false) String isPresent
            , @RequestParam(value = "content", required = false) MultipartFile[] content
            , Model model) {
        try {
            String tomatoesIdTrim = id.trim();
            String realId = getIdFromMap(Long.parseLong(tomatoesIdTrim));
            Tomatoes tomato = tomatoesService.getTomatoById(realId);
            List<Photo> currentPhotos = tomato.getPhotos();

            updateTomatoFields(tomato
                    , category
                    , tomatoesName
                    , tomatoesHeight
                    , tomatoesDiameter
                    , tomatoesFruit
                    , tomatoesFlowerpot
                    , tomatoesAgroTech
                    , tomatoesDescription
                    , tomatoesTaste
                    , tomatoesSpecificity
                    , isPresent);

            List<Photo> photos = new ArrayList<>();
            long count = 0;

            if (content.length > 0) {
                createPhotoList(content, photos, count);
            }

            if (photos.size() > 1) {
                tomato.setPhotos(photos);
            } else {
                tomato.setPhotos(currentPhotos);
            }

            tomatoesService.addNewTomato(tomato);

            return getTomatoesForCRUD(model);
        } catch (Exception e) {
            logger.error("*** TomatoesController.updateTomato():" +
                    "  wrong DB's values! ***" + e.getMessage() + " ||| " + e);
            return getTomatoesForCRUD(model);
        }
    }

    @PostMapping("/deletetomato")
    @Secured("ROLE_ADMIN")
    public String deleteTomato(@RequestParam String id
            , Model model) {

        if (checker.checkAttribute(id)
        ) {
            logger.warn("*** TomatoesController.deleteTomato():" +
                    "  Attribute has a null value! ***");
            return getTomatoesForCRUD(model);
        }

        try {
            long idCheck = Long.parseLong(id);
            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
                logger.warn("*** TomatoesController.deleteTomato(): id <<<< 0 ***");
                return getTomatoesForCRUD(model);
            }

            String realId = getIdFromMap(idCheck);
            Tomatoes tomato = tomatoesService.getTomatoById(realId);
            tomatoesService.deleteTomato(tomato);
            return getTomatoesForCRUD(model);

        } catch (Exception e) {
            logger.error("*** TomatoesController.deleteTomato(): wrong DB's values! *** "
                    + e.getMessage());
            return getTomatoesForCRUD(model);
        }
    }

    private void createPhotoList(MultipartFile[] content,
                                 List<Photo> photos,
                                 long count) throws IOException {
        if (content != null) {
            for (MultipartFile f : content
            ) {
                long id = count;
                byte[] photoContent = f.getBytes();
                String contentType = f.getContentType();
                Photo photo = new Photo(id,
                        photoContent,
                        contentType);
                photos.add(photo);
                count++;
            }
        }
    }

    private String getIdFromMap(long id) {

        List<Tomatoes> classList = tomatoesService.findAllTomatoes();
        if (classList.isEmpty()) {
            logger.error("*** TomatoesController.getIdFromMap():" +
                    "  WRONG DB VALUES*** ");
            return null;
        }
        String objectId = "";
        for (Tomatoes tom : classList
        ) {
            Map<ObjectId, Long> idLongMap = tom.getIdMap();

            for (Map.Entry<ObjectId, Long> entry : idLongMap.entrySet()
            ) {
                if (entry.getValue() == id) {
                    objectId = entry.getKey().toString();
                    break;
                }
            }
        }
        return objectId;
    }

    private void updateTomatoFields(Tomatoes tomato
            , String category
            , String tomatoesName
            , String tomatoesHeight
            , String tomatoesDiameter
            , String tomatoesFruit
            , String tomatoesFlowerpot
            , String tomatoesAgroTech
            , String tomatoesDescription
            , String tomatoesTaste
            , String tomatoesSpecificity
            , String isPresent
    ) {
        TomatoesCategory tomatoesCategory = Arrays.stream(TOMATOES_CATEGORIES)
                .filter(cat -> cat.getCategory().equals(category))
                .findFirst()
                .orElse(tomato.getCategory());
        tomato.setCategory(tomatoesCategory);

        IsPresent tomatoIsPresent = Arrays.stream(IS_PRESENTS)
                .filter(isp -> isp.getPresent().equals(isPresent))
                .findFirst()
                .orElse(tomato.getIsPresent());
        tomato.setIsPresent(tomatoIsPresent);

        updateFieldIfProvided(tomato::setTomatoesSpecificity, tomato::getTomatoesSpecificity, tomatoesSpecificity);
        updateFieldIfProvided(tomato::setTomatoesName, tomato::getTomatoesName, tomatoesName);
        updateFieldIfProvided(tomato::setTomatoesHeight, tomato::getTomatoesHeight, tomatoesHeight);
        updateFieldIfProvided(tomato::setTomatoesDiameter, tomato::getTomatoesDiameter, tomatoesDiameter);
        updateFieldIfProvided(tomato::setTomatoesFruit, tomato::getTomatoesFruit, tomatoesFruit);
        updateFieldIfProvided(tomato::setTomatoesFlowerpot, tomato::getTomatoesFlowerpot, tomatoesFlowerpot);
        updateFieldIfProvided(tomato::setTomatoesAgroTech, tomato::getTomatoesAgroTech, tomatoesAgroTech);
        updateFieldIfProvided(tomato::setTomatoesDescription, tomato::getTomatoesDescription, tomatoesDescription);
        updateFieldIfProvided(tomato::setTomatoesTaste, tomato::getTomatoesTaste, tomatoesTaste);
    }

    private void updateFieldIfProvided(Consumer<String> setter,
                                       Supplier<String> getter,
                                       String value) {
        if (!checker.checkAttribute(value)) {
            setter.accept(value.trim());
        } else {
            setter.accept(getter.get());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadTomatoesPhoto(@PathVariable String id) {
        try {
            Tomatoes tomato = tomatoesService.getTomatoById(id);
            if (tomato != null) {
                List<Photo> photos = tomato.getPhotos();

                for (Photo ph : photos
                ) {
                    HttpHeaders headers = new HttpHeaders();
                    String contentType = tomato.getContentType();
                    assert contentType != null;
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDisposition(ContentDisposition.attachment()
                            .filename(tomato.getName())
                            .build());
                    headers.setContentLength(ph.getContent().length);
                    return new ResponseEntity<>(ph.getContent()
                            , headers
                            , HttpStatus.OK);
                }
            }
            throw new Exception("tomato is NULL");
        } catch (Exception e) {
            logger.error("*** TomatoesControllerClass.downloadTomatoesPhoto():" +
                    "  WRONG DB VALUES*** " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/adminfindtomato")
    @Secured("ROLE_ADMIN")
    public String adminFindTomato(@RequestParam String attr
            , Model model) {

        List<Tomatoes> tomatoesList = adminTomatoesService.findTomatoesForAdmin(attr);
        model.addAttribute("tomatoesList", tomatoesList);

        return "tomatoes";
    }

    @PostMapping("/findtomato")
    public String findTomato(@RequestParam String attr
            , Model model) {

        if (checker.checkAttribute(attr)) {
            logger.error("*** TomatoesController.findTomato():" +
                    "  WRONG DB VALUES*** ");
            return getTomatoes(model);
        }

        try {
            List<Tomatoes> tomatoes = tomatoesService.findAllTomatoes();
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
                logger.debug("*** TomatoesController.findTomato():  DATA NOT FOUND IN DB***");
                return getTomatoes(model);
            }

            model.addAttribute("tomatoesList", tomatoesList);
            return "tomatoescatalog";

        } catch (Exception e) {
            logger.error("*** TomatoesController.findTomato(): wrong DB's values! *** "
                    + e.getMessage());
            return getTomatoes(model);
        }
    }

    @PostMapping("/filtertomatoesbycategory")
    @Secured("ROLE_ADMIN")
    public String findTomatoesListByCategory(@RequestParam String category
            , Model model) {

        if (checker.checkAttribute(category)) {
            logger.warn("*** TomatoesController.findTomatoesListByCategory():" +
                    "  Attribute has a null value! ***");
            return getTomatoesForCRUD(model);
        }

        try {
            List<Tomatoes> tomatoes = tomatoesService.findAllTomatoes();
            List<Tomatoes> tomatoesList = new ArrayList<>();
            for (Tomatoes t : tomatoes
            ) {
                if (t.getCategory().getCategory().equals(category)) {
                    tomatoesList.add(t);
                }
            }
            model.addAttribute("tomatoesList", tomatoesList);

            return "tomatoes";
        } catch (Exception e) {
            logger.error("*** TomatoesController.findTomatoesListByCategory(): wrong DB's values! *** "
                    + e.getMessage());
            return getTomatoesForCRUD(model);
        }
    }

    @GetMapping("/tomatoes/{idCount}")
    public String getFullCard(@PathVariable Long idCount
            , Model model) {

        try {
            String realId = getIdFromMap(idCount);
            Tomatoes tomato = tomatoesService.getTomatoById(realId);
            List<Tomatoes> cardList = new ArrayList<>();
            cardList.add(tomato);

            List<Tomatoes> tomatoesList = tomatoesService.findAllTomatoes();
            model.addAttribute("cardList", cardList);
            model.addAttribute("tomatoesList", tomatoesList);
            model.addAttribute("photos", tomato.getPhotos());

            return "fullcard";

        } catch (Exception e) {
            logger.error("*** TomatoesController.getFullCard(): wrong DB's values! *** "
                    + e.getMessage());
            return "/";
        }
    }
}
