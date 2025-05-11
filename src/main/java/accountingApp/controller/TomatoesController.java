package accountingApp.controller;

import accountingApp.entity.*;
import accountingApp.service.*;
import accountingApp.usefulmethods.Checker;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
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
    private final Checker checker;

    @Autowired
    public TomatoesController(TomatoesService tomatoesService, Checker checker) {
        this.tomatoesService = tomatoesService;
        this.checker = checker;
    }

    @GetMapping("/tomatoes")
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

        return "tomatoes";
    }

    @PostMapping("/addtomato")
    public String addTomato(@RequestParam(required = false) String category
            , @RequestParam String tomatoesName
            , @RequestParam String tomatoesHeight
            , @RequestParam String tomatoesDiameter
            , @RequestParam String tomatoesFruit
            , @RequestParam String tomatoesFlowerpot
            , @RequestParam String tomatoesAgroTech
            , @RequestParam String tomatoesDescription
            , @RequestParam String tomatoesTaste
            , @RequestParam String tomatoesSpecificity
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
                    "  Attribute has a null value! ***");
            return getTomatoes(model);
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

            return getTomatoes(model);
        } catch (Exception e) {
            logger.error("*** TomatoesController.addTomato():" +
                    "  wrong DB's values! ***" + e.getMessage());
            return getTomatoes(model);
        }
    }

    @PostMapping("/updatetomato")
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

            createPhotoList(content, photos, count);

            tomato.setPhotos(photos);

            tomatoesService.addNewTomato(tomato);

            return getTomatoes(model);
        } catch (Exception e) {
            logger.error("*** TomatoesController.updateTomato():" +
                    "  wrong DB's values! ***" + e.getMessage() + " ||| " + e);
            return getTomatoes(model);
        }
    }

    @PostMapping("/deletetomato")
    public String deleteTomato(@RequestParam String id
            , Model model) {

        if (checker.checkAttribute(id)
        ) {
            logger.warn("*** TomatoesController.deleteTomato():" +
                    "  Attribute has a null value! ***");
            return getTomatoes(model);
        }

        try {
            long idCheck = Long.parseLong(id);
            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
                logger.warn("*** TomatoesController.deleteTomato(): id <<<< 0 ***");
                return getTomatoes(model);
            }

            String realId = getIdFromMap(idCheck);
            Tomatoes tomato = tomatoesService.getTomatoById(realId);
            tomatoesService.deleteTomato(tomato);
            return getTomatoes(model);

        } catch (Exception e) {
            logger.error("*** TomatoesController.deleteTomato(): wrong DB's values! *** "
                    + e.getMessage());
            return getTomatoes(model);
        }
    }

    private void createPhotoList(MultipartFile[] content, List<Photo> photos, long count) throws IOException {
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

        String specificityValue;
        if (!checker.checkAttribute(tomatoesSpecificity)) {
            specificityValue = tomatoesSpecificity.trim();
            tomato.setTomatoesSpecificity(specificityValue);
        } else {
            updateFieldIfProvided(tomato::setTomatoesSpecificity,
                    tomato.getTomatoesSpecificity());
        }

        updateFieldIfProvided(tomato::setTomatoesName, tomatoesName);
        updateFieldIfProvided(tomato::setTomatoesHeight, tomatoesHeight);
        updateFieldIfProvided(tomato::setTomatoesDiameter, tomatoesDiameter);
        updateFieldIfProvided(tomato::setTomatoesFruit, tomatoesFruit);
        updateFieldIfProvided(tomato::setTomatoesFlowerpot, tomatoesFlowerpot);
        updateFieldIfProvided(tomato::setTomatoesAgroTech, tomatoesAgroTech);
        updateFieldIfProvided(tomato::setTomatoesDescription, tomatoesDescription);
        updateFieldIfProvided(tomato::setTomatoesTaste, tomatoesTaste);
    }

    private void updateFieldIfProvided(Consumer<String> setter, String value) {
        if (!checker.checkAttribute(value)) {
            setter.accept(value.trim());
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
                    return new ResponseEntity<>(ph.getContent(), headers, HttpStatus.OK);
                }
            }
            throw new Exception("tomato is NULL");
        } catch (Exception e) {
            logger.error("*** TomatoesControllerClass.downloadTomatoesPhoto():" +
                    "  WRONG DB VALUES*** " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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
            String attrTrim = attr.trim();

            for (Tomatoes t : tomatoes
            ) {
                if ((t.getId() + "").contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getCategory().getCategory().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesName().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesHeight().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesDiameter().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesFruit().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesFlowerpot().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesAgroTech().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesDescription().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesTaste().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getTomatoesSpecificity().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if (t.getIsPresent().getPresent().contains(attrTrim)) {
                    tomatoesList.add(t);
                } else if ((t.getIdCount() + "").contains(attrTrim)) {
                    tomatoesList.add(t);
                }
            }

            if (tomatoesList.isEmpty()) {
                logger.debug("*** TomatoesController.findTomato():  DATA NOT FOUND IN DB***");
                return getTomatoes(model);
            }

            model.addAttribute("tomatoesList", tomatoesList);
            return "tomatoes";

        } catch (Exception e) {
            logger.error("*** TomatoesController.findTomato(): wrong DB's values! *** "
                    + e.getMessage());
            return getTomatoes(model);
        }
    }

    @PostMapping("/filtertomatoesbycategory")
    public String findTomatoesListByCategory(@RequestParam String category
            , Model model) {

        if (checker.checkAttribute(category)) {
            logger.warn("*** TomatoesController.findTomatoesListByCategory():" +
                    "  Attribute has a null value! ***");
            return getTomatoes(model);
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
            return getTomatoes(model);
        }
    }
}
