package accountingApp.service;

import accountingApp.entity.*;
import accountingApp.repository.PeppersRepository;
import accountingApp.usefulmethods.Checker;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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


    public Model preparePeppersModel(Model model) {
        List<Peppers> peppersList = peppersRepository.findAll()
                .stream()
                .sorted(Comparator.comparingLong(Peppers::getIdCount).reversed())
                .collect(Collectors.toList());

        List<String> categoryList = getCategoryList();
        List<String> isPresentList = getIsPresentList();

        model.addAttribute("peppersList", peppersList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("isPresentList", isPresentList);

        return model;
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
            , Model model) {

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

        String peppersNameTrim = peppersName.trim();
        String peppersHeightTrim = peppersHeight.trim();
        String peppersDiameterTrim = peppersDiameter.trim();
        String peppersFruitTrim = peppersFruit.trim();
        String peppersFlowerpotTrim = peppersFlowerpot.trim();
        String peppersAgroTechTrim = peppersAgroTech.trim();
        String peppersDescriptionTrim = peppersDescription.trim();
        String peppersTasteTrim = peppersTaste.trim();
        String peppersSpecificityTrim = "\uD83C\uDF36 \uD83C\uDF36 \uD83C\uDF36";

        if (!checker.checkAttribute(peppersSpecificity)) {
            peppersSpecificityTrim = peppersSpecificity.trim();
        }

        try {
            PeppersCategory peppersCategory = Arrays.stream(PEPPERS_CATEGORIES)
                    .filter(cat -> cat.getCategory().equals(category))
                    .findFirst()
                    .orElse(PeppersCategory.Сладкий);

            IsPresent pepperIsPresent = Arrays.stream(IS_PRESENTS)
                    .filter(isp -> isp.getPresent().equals(isPresent))
                    .findFirst()
                    .orElse(IsPresent.ДА);

            Peppers pepper = new Peppers(peppersCategory
                    , peppersNameTrim
                    , peppersHeightTrim
                    , peppersDiameterTrim
                    , peppersFruitTrim
                    , peppersFlowerpotTrim
                    , peppersAgroTechTrim
                    , peppersDescriptionTrim
                    , peppersTasteTrim
                    , peppersSpecificityTrim
                    , pepperIsPresent
            );

            peppersRepository.save(pepper);

            ObjectId objectId = pepper.getId();
            List<Photo> photos = new ArrayList<>();
            long count = 0;

            createPhotoList(content, photos, count);

            pepper.setPhotos(photos);

            long idCount;
            List<Peppers> peppersList = peppersRepository.findAll();

            Set<Long> idSet = peppersList.stream()
                    .map(Peppers::getIdCount)
                    .collect(Collectors.toSet());

            idCount = LongStream.rangeClosed(1, idSet.size() + +1_000_000_000_000_000_000L)
                    .filter(idc -> !idSet.contains(idc))
                    .findFirst()
                    .orElse(0L);

            Map<ObjectId, Long> idLongMap = new HashMap<>();
            idLongMap.put(objectId, idCount);
            pepper.setIdMap(idLongMap);
            pepper.setIdCount(idCount);

            peppersRepository.save(pepper);

            return model;
        } catch (Exception e) {
            logger.error("*** AdminPeppersService.addNewPepper():" +
                    "  wrong DB's values! ***" + e.getMessage());
            return model;
        }
    }

    public Model updateNewPepper(String id
            , String category
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
            , Model model) {
        try {
            String peppersIdTrim = id.trim();
            String realId = getIdFromMap(Long.parseLong(peppersIdTrim));
            Peppers pepper = getPepperById(realId);
            List<Photo> currentPhotos = pepper.getPhotos();

            updatePepperFields(pepper
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
                    , isPresent);

            List<Photo> photos = new ArrayList<>();
            long count = 0;

            if (content.length > 0) {
                createPhotoList(content, photos, count);
            }

            if (photos.size() > 1) {
                pepper.setPhotos(currentPhotos);
            }

            peppersRepository.save(pepper);

            return model;
        } catch (Exception e) {
            logger.error("*** AdminPeppersService.updateNewPepper():" +
                    "  wrong DB's values! ***" + e.getMessage());
            return model;
        }

    }

    public Model deletePepper(String id
            , Model model) {

        if (checker.checkAttribute(id)
        ) {
            logger.warn("*** AdminPeppersService.deletePepper():" +
                    "  Attribute has a null value! ***");
            return model;
        }

        try {
            long idCheck = Long.parseLong(id);
            if (idCheck <= 0 || checker.checkAttribute(idCheck + "")) {
                logger.warn("*** AdminPeppersService.deletePepper(): id <<<< 0 ***");
                return model;
            }

            String realId = getIdFromMap(idCheck);
            Peppers pepper = getPepperById(realId);
            peppersRepository.delete(pepper);

            List<Peppers> peppersList = peppersRepository.findAll();
            model.addAttribute("peppersList", peppersList);

            return model;

        } catch (Exception e) {
            logger.error("*** AdminPeppersService.deletePepper(): wrong DB's values! *** "
                    + e.getMessage());
            return model;
        }
    }

    public Model findPeppersForAdmin(String attr
    , Model model) {

        if (checker.checkAttribute(attr)) {
            logger.error("*** AdminPeppersService.findPeppersForAdmin():" +
                    "  WRONG DB VALUES*** ");
            return model;
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
                return model;
            }

            model.addAttribute("peppersList", peppersList);

            return model;

        } catch (Exception e) {
            logger.error("*** AdminPeppersService.findPeppersForAdmin(): wrong DB's values! *** "
                    + e.getMessage());
            return model;
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

        List<Peppers> classList = peppersRepository.findAll();
        if (classList.isEmpty()) {
            logger.error("*** AdminPeppersService.getIdFromMap():" +
                    "  WRONG DB VALUES*** ");
            return null;
        }
        String objectId = "";
        for (Peppers pep : classList
        ) {
            Map<ObjectId, Long> idLongMap = pep.getIdMap();

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

    private void updatePepperFields(Peppers pepper
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
        PeppersCategory peppersCategory = Arrays.stream(PEPPERS_CATEGORIES)
                .filter(cat -> cat.getCategory().equals(category))
                .findFirst()
                .orElse(pepper.getCategory());
        pepper.setCategory(peppersCategory);

        IsPresent pepperIsPresent = Arrays.stream(IS_PRESENTS)
                .filter(isp -> isp.getPresent().equals(isPresent))
                .findFirst()
                .orElse(pepper.getIsPresent());
        pepper.setIsPresent(pepperIsPresent);

        updateFieldIfProvided(pepper::setPeppersSpecificity, pepper::getPeppersSpecificity, tomatoesSpecificity);
        updateFieldIfProvided(pepper::setPeppersName, pepper::getPeppersName, tomatoesName);
        updateFieldIfProvided(pepper::setPeppersHeight, pepper::getPeppersHeight, tomatoesHeight);
        updateFieldIfProvided(pepper::setPeppersDiameter, pepper::getPeppersDiameter, tomatoesDiameter);
        updateFieldIfProvided(pepper::setPeppersFruit, pepper::getPeppersFruit, tomatoesFruit);
        updateFieldIfProvided(pepper::setPeppersFlowerpot, pepper::getPeppersFlowerpot, tomatoesFlowerpot);
        updateFieldIfProvided(pepper::setPeppersAgroTech, pepper::getPeppersAgroTech, tomatoesAgroTech);
        updateFieldIfProvided(pepper::setPeppersDescription, pepper::getPeppersDescription, tomatoesDescription);
        updateFieldIfProvided(pepper::setPeppersTaste, pepper::getPeppersTaste, tomatoesTaste);
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

    private Peppers getPepperById(String id) {
        return peppersRepository.findAll().stream()
                .filter(pepId -> (pepId.getId() + "").equals(id))
                .findFirst()
                .orElse(new Peppers());
    }

    public ResponseEntity<byte[]> downloadPeppersPhoto(String id) {
        try {
            Peppers peppers = getPepperById(id);
            List<Photo> photos = peppers.getPhotos();

            for (Photo ph : photos
            ) {
                HttpHeaders headers = new HttpHeaders();
                String contentType = peppers.getContentType();
                assert contentType != null;
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentDisposition(ContentDisposition.attachment()
                        .filename(peppers.getName())
                        .build());
                headers.setContentLength(ph.getContent().length);
                return new ResponseEntity<>(ph.getContent()
                        , headers
                        , HttpStatus.OK);
            }
            throw new Exception("peppers is NULL");
        } catch (Exception e) {
            logger.error("*** AdminPeppersService.downloadPeppersPhoto():" +
                    "  WRONG DB VALUES*** " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public Model findPeppersListByCategory(String category
            , Model model) {

        if (checker.checkAttribute(category)) {
            logger.warn("*** AdminPeppersService.findPeppersListByCategory():" +
                    "  Attribute has a null value! ***");
            return model;
        }

        try {
            List<Peppers> peppers = peppersRepository.findAll();
            List<Peppers> peppersList = new ArrayList<>();
            for (Peppers p : peppers
            ) {
                if (p.getCategory().getCategory().equals(category)) {
                    peppersList.add(p);
                }
            }
            model.addAttribute("peppersList", peppersList);

            return model;
        } catch (Exception e) {
            logger.error("*** AdminPeppersService.findPeppersListByCategory(): wrong DB's values! *** "
                    + e.getMessage());
            return model;
        }
    }

    public Model getFullCard(Long idCount
            , Model model) {

        try {
            String realId = getIdFromMap(idCount);
            Peppers pepper = getPepperById(realId);
            List<Peppers> cardList = new ArrayList<>();
            cardList.add(pepper);

            List<Peppers> peppersList = peppersRepository.findAll();
            model.addAttribute("cardList", cardList);
            model.addAttribute("peppersList", peppersList);
            model.addAttribute("photos", pepper.getPhotos());

            return model;

        } catch (Exception e) {
            logger.error("*** AdminPeppersService.getFullCard(): wrong DB's values! *** "
                    + e.getMessage());
            return model;
        }
    }
}
