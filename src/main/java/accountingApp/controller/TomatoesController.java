package accountingApp.controller;

import accountingApp.entity.*;
import accountingApp.service.*;
import accountingApp.usefulmethods.Checker;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
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
        List<Tomatoes> tomatoesList = tomatoesService.findAllTomatoes();
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

            long idCount = 1L;
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
//
//    @PostMapping("/adddevices")
//    public String addDevice(@RequestParam(required = false) String category,
//                            @RequestParam String name,
//                            @RequestParam String description,
//                            @RequestParam String inventory,
//                            @RequestParam String serial,
//                            @RequestParam(required = false) Room room,
//                            @RequestParam(required = false) Employee employee,
//                            @RequestParam(required = false) ITStaff itstaff,
//                            Model model) {
//
//        if (checker.checkAttribute(category)
//                || checker.checkAttribute(name)
//                || checker.checkAttribute(description)
//                || checker.checkAttribute(inventory)
//                || checker.checkAttribute(serial)
//                || room == null
//                || employee == null
//                || itstaff == null
//        ) {
//            logger.warn("*** DevicesController.addDevice():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//
//        String categoryWithoutSpaces = category.trim();
//        String nameWithoutSpaces = name.trim();
//        String descriptionWithoutSpaces = description.trim();
//        String inventoryWithoutSpaces = inventory.trim();
//        String serialWithoutSpaces = serial.trim();
//
//        try {
//            long inventoryCheck = Long.parseLong(inventoryWithoutSpaces);
//            if (!checker.checkAttribute(categoryWithoutSpaces)
//                    && !checker.checkAttribute(nameWithoutSpaces)
//                    && !checker.checkAttribute(descriptionWithoutSpaces)
//                    && !checker.checkAttribute(inventoryWithoutSpaces)
//                    && !checker.checkAttribute(serialWithoutSpaces)
//            ) {
//
//                TomatoesCategory tomatoesCategory = Arrays.stream(DEVICE_CATEGORIES)
//                        .filter(cat -> cat.getCategory().equals(categoryWithoutSpaces))
//                        .findFirst()
//                        .orElse(TomatoesCategory.Штамбовый);
//
//                Tomatoes tomatoes = new Tomatoes(tomatoesCategory
//                        , nameWithoutSpaces
//                        , descriptionWithoutSpaces
//                        , inventoryCheck
//                        , serialWithoutSpaces
//                        , room
//                        , employee
//                        , itstaff);
//                devicesService.addNewDevice(tomatoes);
//
//                List<Tomatoes> tomatoesList = devicesService.findAllDevices();
//                Tomatoes dev = new Tomatoes();
//
//                for (Tomatoes dv : tomatoesList
//                ) {
//                    if (dv.getName().equals(nameWithoutSpaces)
//                            && dv.getCategory().getCategory().equals(tomatoesCategory.getCategory())
//                            && dv.getSerial().equals(serialWithoutSpaces)
//                            && dv.getRoom().equals(room)
//                            && dv.getItstaff().equals(itstaff)
//                            && dv.getEmployee().equals(employee)
//                            && dv.getInventory() == inventoryCheck
//                            && dv.getDescription().equals(descriptionWithoutSpaces)) {
//                        dev = dv;
//                        break;
//                    }
//                }
//
//                LocalDate currentDate = LocalDate.now();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                Repair repair = new Repair(currentDate.format(formatter), dev);
//                repairService.createRepair(repair);
//                dev.setRepair(repair);
//                devicesService.addNewDevice(dev);
//                return getDevices(model);
//            }
//            throw new Exception("Attribute is empty!");
//        } catch (Exception e) {
//            logger.error("*** DevicesController.addDevice(): wrong DB's values! *** "
//                    + e.getMessage());
//            return getDevices(model);
//        }
//    }
//
//    @PostMapping("/deletedevice")
//    public String deleteDevice(@RequestParam String id,
//                               Model model) {
//
//        if (checker.checkAttribute(id)
//        ) {
//            logger.warn("*** DevicesController.deleteDevice():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//
//        try {
//            ObjectId idCheck = Integer.parseInt(id);
//            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
//                logger.warn("*** DevicesController.deleteDevice(): dborn <<<< 0 ***");
//                return getDevices(model);
//            }
//
//            List<Tomatoes> devices = devicesService.getDevicesById(idCheck);
//            Tomatoes device = devices.get(0);
//            device.setEmployee(null);
//            device.setRoom(null);
//            device.setItstaff(null);
//            device.setEvents(null);
//
//            Repair repair = device.getRepair();
//            device.setRepair(null);
//            repairService.deleteRepair(repair.getId());
//
//            devicesService.updateDevice(device);
//
//            devicesService.deleteDeviceById(idCheck);
//            return getDevices(model);
//        } catch (Exception e) {
//            logger.error("*** DevicesController.deleteDevice(): wrong DB's values! *** "
//                    + e.getMessage());
//            return getDevices(model);
//        }
//    }
//
//    @PostMapping("/updatedevice")
//    public String updateDevice(@RequestParam String id,
//                               @RequestParam(required = false) String category,
//                               @RequestParam String name,
//                               @RequestParam String description,
//                               @RequestParam String inventory,
//                               @RequestParam String serial,
//                               @RequestParam(required = false) Room room,
//                               @RequestParam(required = false) Employee employee,
//                               @RequestParam(required = false) ITStaff itstaff,
//                               Model model) {
//
//        if (checker.checkAttribute(category)
//                || checker.checkAttribute(id)
//                || checker.checkAttribute(name)
//                || checker.checkAttribute(description)
//                || checker.checkAttribute(inventory)
//                || checker.checkAttribute(serial)
//                || room == null
//                || employee == null
//                || itstaff == null
//        ) {
//            logger.warn("*** DevicesController.updateDevice():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//
//        try {
//            String categoryWithoutSpaces = category.trim();
//            String nameWithoutSpaces = name.trim();
//            String descriptionWithoutSpaces = description.trim();
//            String inventoryWithoutSpaces = inventory.trim();
//            ObjectId idCheck = Integer.parseInt(id);
//            long inventoryCheck = Long.parseLong(inventoryWithoutSpaces);
//            String serialWithoutSpaces = serial.trim();
//
//            if (idCheck <= 0
//                    || checker.checkAttribute(idCheck + "")
//                    || inventoryCheck <= 0
//                    || checker.checkAttribute(inventoryCheck + "")
//                    || checker.checkAttribute(serialWithoutSpaces)
//            ) {
//                logger.warn("*** DevicesController.updateDevice(): dborn <<<< 0 ***");
//                return getDevices(model);
//            }
//
//            if (!checker.checkAttribute(categoryWithoutSpaces)
//                    && !checker.checkAttribute(nameWithoutSpaces)
//                    && !checker.checkAttribute(descriptionWithoutSpaces)
//            ) {
//
//                TomatoesCategory tomatoesCategory = Arrays.stream(DEVICE_CATEGORIES)
//                        .filter(cat -> cat.getCategory().equals(categoryWithoutSpaces))
//                        .findFirst()
//                        .orElse(TomatoesCategory.Штамбовый);
//
//                Repair repair = devicesService.getDevicesById(idCheck).get(0).getRepair();
//
//                Tomatoes tomatoes = new Tomatoes(idCheck
//                        , tomatoesCategory
//                        , nameWithoutSpaces
//                        , descriptionWithoutSpaces
//                        , inventoryCheck
//                        , serialWithoutSpaces
//                        , room
//                        , employee
//                        , itstaff);
//                tomatoes.setRepair(repair);
//                devicesService.updateDevice(tomatoes);
//                return getDevices(model);
//            }
//            throw new Exception("Attribute is empty!");
//        } catch (Exception e) {
//            logger.error("*** DevicesController.updateDevice(): wrong DB's values! *** "
//                    + e.getMessage());
//            return getDevices(model);
//        }
//    }
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
//    @PostMapping("/filterdevicesbyattrs")
//    public String findDevicesListByAttrs(@RequestParam String attrs
//            , Model model) {
//
//        if (checker.checkAttribute(attrs)) {
//            logger.warn("*** DevicesController.findDevicesListByAttrs():" +
//                    "  Attribute has a null value! ***");
//            return getDevices(model);
//        }
//        String attrsWithoutSpaces = attrs.trim().toLowerCase(Locale.ROOT);
//
//        try {
//            List<Tomatoes> devices = devicesService.findAllDevices();
//            List<Tomatoes> tomatoesList = new ArrayList<>();
//
//            for (Tomatoes dev : devices
//            ) {
//                if ((dev.getId() + "").contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getName().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getCategory().getCategory().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getDescription().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getEmployee().getFio().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if ((dev.getInventory() + "").toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getItstaff().getName().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if ((dev.getRepair().getDurability() + "").toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getRoom().getNumber().contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                } else if (dev.getSerial().toLowerCase(Locale.ROOT)
//                        .contains(attrsWithoutSpaces)) {
//                    tomatoesList.add(dev);
//                }
//            }
//
//            model.addAttribute("devicesList", tomatoesList);
//            if (tomatoesList.isEmpty()) {
//                logger.debug("*** DevicesController.findDevicesListByAttrs():  DATA NOT FOUND IN DB***");
//                return getDevices(model);
//            }
//            return "devices";
//        } catch (Exception e) {
//            logger.error("*** DevicesController.findDevicesListByAttrs(): wrong DB's values! *** "
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
