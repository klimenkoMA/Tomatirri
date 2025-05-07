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


        model.addAttribute("tomatoesList", tomatoesList);
        model.addAttribute("categoryList", categoryList);
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
            , @RequestParam String tomatoesPrice
            , @RequestParam("content") MultipartFile content
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
                || checker.checkAttribute(tomatoesPrice)
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
        String tomatoesPriceTrim = tomatoesPrice.trim();
        String tomatoesSpecificityTrim = "\uD83C\uDF45 \uD83C\uDF45 \uD83C\uDF45";

        if (!checker.checkAttribute(tomatoesSpecificity)) {
            tomatoesSpecificityTrim = tomatoesSpecificity.trim();
        }

        try {
            TomatoesCategory tomatoesCategory = Arrays.stream(TOMATOES_CATEGORIES)
                    .filter(cat -> cat.getCategory().equals(category))
                    .findFirst()
                    .orElse(TomatoesCategory.Штамбовый);

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
                    , tomatoesPriceTrim
            );

            tomatoesService.addNewTomato(tomato);
            ObjectId objectId = tomato.getId();
            byte[] tomatoContent;
            String contentType;

            if (!content.isEmpty()) {
                tomatoContent = content.getBytes();
                contentType = content.getContentType();
                tomato.setContent(tomatoContent);
                tomato.setContentType(contentType);
            }

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
            , @RequestParam(required = false) String tomatoesPrice
            , @RequestParam("content") MultipartFile content
            , Model model) {
        try {
            String tomatoesIdTrim = id.trim();
            String realId = getIdFromMap(Long.parseLong(tomatoesIdTrim));
            Tomatoes tomato = tomatoesService.getTomatoById(realId);
            byte[] tomatoContent;
            String contentType;

            if (!content.isEmpty()) {
                tomatoContent = content.getBytes();
                contentType = content.getContentType();
                tomato.setContent(tomatoContent);
                tomato.setContentType(contentType);
            }

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
                    , tomatoesPrice);

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
            , String tomatoesPrice
    ) {
        TomatoesCategory tomatoesCategory = Arrays.stream(TOMATOES_CATEGORIES)
                .filter(cat -> cat.getCategory().equals(category))
                .findFirst()
                .orElse(tomato.getCategory());
        tomato.setCategory(tomatoesCategory);

        String specificityValue;
        if (!checker.checkAttribute(tomatoesSpecificity)) {
            specificityValue = tomatoesSpecificity.trim();
            tomato.setTomatoesSpecificity(specificityValue);
        } else {
            updateFieldIfProvided(tomato::setTomatoesSpecificity, tomato.getTomatoesSpecificity());
        }

        updateFieldIfProvided(tomato::setTomatoesName, tomatoesName);
        updateFieldIfProvided(tomato::setTomatoesHeight, tomatoesHeight);
        updateFieldIfProvided(tomato::setTomatoesDiameter, tomatoesDiameter);
        updateFieldIfProvided(tomato::setTomatoesFruit, tomatoesFruit);
        updateFieldIfProvided(tomato::setTomatoesFlowerpot, tomatoesFlowerpot);
        updateFieldIfProvided(tomato::setTomatoesAgroTech, tomatoesAgroTech);
        updateFieldIfProvided(tomato::setTomatoesDescription, tomatoesDescription);
        updateFieldIfProvided(tomato::setTomatoesTaste, tomatoesTaste);
        updateFieldIfProvided(tomato::setTomatoesPrice, tomatoesPrice);
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

                HttpHeaders headers = new HttpHeaders();
                String docType = tomato.getContentType();
                assert docType != null;
                headers.setContentType(MediaType.parseMediaType(docType));
                headers.setContentDisposition(ContentDisposition.attachment()
                        .filename(tomato.getName())
                        .build());
                headers.setContentLength(tomato.getContent().length);

                return new ResponseEntity<>(tomato.getContent(), headers, HttpStatus.OK);
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
                } else if (t.getTomatoesPrice().contains(attrTrim)) {
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

//
//    @PostMapping("/finddevicebyname")
//    public String findDevicesById(@RequestParam String name,
//                                  Model model) {
//
//        if (checker.checkAttribute(name)
//        ) {
//            logger.warn("*** DevicesController.findDevicesById():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//
//        try {
//            ObjectId idCheck = Integer.parseInt(name);
//            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
//                logger.warn("*** DevicesController.findDevicesById(): dborn <<<< 0 ***");
//                return getDevices(model);
//            } else {
//                logger.debug("*** DevicesController.findDevicesById():" +
//                        "FOUND DEVICE BY ID ***");
//                List<Tomatoes> tomatoesList;
//                tomatoesList = devicesService.getDevicesById(idCheck);
//                model.addAttribute("devicesList", tomatoesList);
//                return "devices";
//            }
//        } catch (Exception e) {
//            try {
//                List<Tomatoes> tomatoesList = devicesService.getDevicesByName(name);
//                model.addAttribute("devicesList", tomatoesList);
//                logger.debug("*** DevicesController.findDevicesById():" +
//                        " FOUND DEVICE BY NAME *** " + e.getMessage());
//                return "devices";
//            } catch (Exception e1) {
//                logger.error("*** DevicesController.findDevicesById(): wrong DB's values! *** "
//                        + e1.getMessage());
//                return getDevices(model);
//            }
//        }
//    }
//
//    @PostMapping("/filterdevicesbycategory")
//    public String findDevicesListByCategory(@RequestParam String category
//            , Model model) {
//
//        if (checker.checkAttribute(category)) {
//            logger.warn("*** DevicesController.findDevicesListByCategory():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//        String categoryWithoutSpaces = category.trim();
//
//        try {
//
//            TomatoesCategory tomatoesCategory = Arrays.stream(DEVICE_CATEGORIES)
//                    .filter(cat -> cat.getCategory().equals(categoryWithoutSpaces))
//                    .findFirst()
//                    .orElse(TomatoesCategory.Штамбовый);
//
//            List<Tomatoes> tomatoesList = devicesService.getDevicesByCategory(tomatoesCategory);
//            model.addAttribute("devicesList", tomatoesList);
//
//            return "devices";
//        } catch (Exception e) {
//            logger.error("*** DevicesController.findDevicesListByCategory(): wrong DB's values! *** "
//                    + e.getMessage());
//            return getDevices(model);
//        }
//    }
//
//    @GetMapping("/maxownercountreport")
//    public String maxOwnerCountReport(Model model) {
//
//        List<MaxOwnerCountDTO> dtoList = devicesService.getOwnersCount();
//
//        String[] owners = dtoList.stream()
//                .map(MaxOwnerCountDTO::getOwner)
//                .toArray(String[]::new);
//
//        long[] counts = dtoList.stream()
//                .mapToLong(MaxOwnerCountDTO::getDevicesCount)
//                .toArray();
//
//        model.addAttribute("dtoList", dtoList);
//        model.addAttribute("owners", owners);
//        model.addAttribute("counts", counts);
//
//        return "/reports/devicesreports/reportmaxownercount";
//    }
}
