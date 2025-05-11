package accountingApp.entity;

import org.apache.tomcat.util.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.hibernate.annotations.Target;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Семена томатов
 */
@Cacheable

@Document(collection = "tomatoes")
public class Tomatoes extends MultipartFileAdapter {
    @Id
    private ObjectId id;
    @Field
    private TomatoesCategory category;
    @Field
    private String tomatoesName;
    @Field
    private String tomatoesHeight;
    @Field
    private String tomatoesDiameter;
    @Field
    private String tomatoesFruit;
    @Field
    private String tomatoesFlowerpot;
    @Field
    private String tomatoesAgroTech;
    @Field
    private String tomatoesDescription;
    @Field
    private String tomatoesTaste;
    @Field
    private String tomatoesSpecificity;
    @Field
    private Long idCount;
    @Field
    private Map<ObjectId, Long> idMap = new HashMap<>();
    @Field
    private IsPresent isPresent;
    @Field
    @Lazy
    private List<Photo> photos = new ArrayList<>();

    public Tomatoes() {
    }

    public Tomatoes(TomatoesCategory category,
                    String tomatoesName,
                    String tomatoesHeight,
                    String tomatoesDiameter,
                    String tomatoesFruit,
                    String tomatoesFlowerpot,
                    String tomatoesAgroTech,
                    String tomatoesDescription,
                    String tomatoesTaste,
                    String tomatoesSpecificity,
                    IsPresent isPresent
    ) {
        this.tomatoesName = tomatoesName;
        this.category = category;
        this.tomatoesHeight = tomatoesHeight;
        this.tomatoesDiameter = tomatoesDiameter;
        this.tomatoesFruit = tomatoesFruit;
        this.tomatoesFlowerpot = tomatoesFlowerpot;
        this.tomatoesAgroTech = tomatoesAgroTech;
        this.tomatoesDescription = tomatoesDescription;
        this.tomatoesTaste = tomatoesTaste;
        this.tomatoesSpecificity = tomatoesSpecificity;
        this.isPresent = isPresent;
    }

    public Tomatoes(ObjectId id,
                    TomatoesCategory category,
                    String tomatoesName,
                    String tomatoesHeight,
                    String tomatoesDiameter,
                    String tomatoesFruit,
                    String tomatoesFlowerpot,
                    String tomatoesAgroTech,
                    String tomatoesDescription,
                    String tomatoesTaste,
                    String tomatoesSpecificity,
                    IsPresent isPresent
    ) {
        this.id = id;
        this.category = category;
        this.tomatoesName = tomatoesName;
        this.tomatoesHeight = tomatoesHeight;
        this.tomatoesDiameter = tomatoesDiameter;
        this.tomatoesFruit = tomatoesFruit;
        this.tomatoesFlowerpot = tomatoesFlowerpot;
        this.tomatoesAgroTech = tomatoesAgroTech;
        this.tomatoesDescription = tomatoesDescription;
        this.tomatoesTaste = tomatoesTaste;
        this.tomatoesSpecificity = tomatoesSpecificity;
        this.isPresent = isPresent;
    }

    public List<String> getListOfImagesDataBase64() {

        return this.photos.stream()
                .map(ph -> Base64.encodeBase64String(ph.getContent()))
                .collect(Collectors.toList());
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Long getIdCount() {
        return idCount;
    }

    public void setIdCount(Long idCount) {
        this.idCount = idCount;
    }

    public Map<ObjectId, Long> getIdMap() {
        return idMap;
    }

    public void setIdMap(Map<ObjectId, Long> idMap) {
        this.idMap = idMap;
    }

    public String getTomatoesName() {
        return tomatoesName;
    }

    public void setTomatoesName(String tomatoesName) {
        this.tomatoesName = tomatoesName;
    }

    public String getTomatoesHeight() {
        return tomatoesHeight;
    }

    public void setTomatoesHeight(String tomatoesHeight) {
        this.tomatoesHeight = tomatoesHeight;
    }

    public String getTomatoesDiameter() {
        return tomatoesDiameter;
    }

    public void setTomatoesDiameter(String tomatoesDiameter) {
        this.tomatoesDiameter = tomatoesDiameter;
    }

    public String getTomatoesFruit() {
        return tomatoesFruit;
    }

    public void setTomatoesFruit(String tomatoesFruit) {
        this.tomatoesFruit = tomatoesFruit;
    }

    public String getTomatoesFlowerpot() {
        return tomatoesFlowerpot;
    }

    public void setTomatoesFlowerpot(String tomatoesFlowerpot) {
        this.tomatoesFlowerpot = tomatoesFlowerpot;
    }

    public String getTomatoesAgroTech() {
        return tomatoesAgroTech;
    }

    public void setTomatoesAgroTech(String tomatoesAgroTech) {
        this.tomatoesAgroTech = tomatoesAgroTech;
    }

    public String getTomatoesDescription() {
        return tomatoesDescription;
    }

    public void setTomatoesDescription(String tomatoesDescription) {
        this.tomatoesDescription = tomatoesDescription;
    }

    public String getTomatoesTaste() {
        return tomatoesTaste;
    }

    public void setTomatoesTaste(String tomatoesTaste) {
        this.tomatoesTaste = tomatoesTaste;
    }

    public String getTomatoesSpecificity() {
        return tomatoesSpecificity;
    }

    public void setTomatoesSpecificity(String tomatoesSpecificity) {
        this.tomatoesSpecificity = tomatoesSpecificity;
    }

    public TomatoesCategory getCategory() {
        return category;
    }

    public void setCategory(TomatoesCategory category) {
        this.category = category;
    }

    public IsPresent getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(IsPresent isPresent) {
        this.isPresent = isPresent;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tomatoes)) return false;
        Tomatoes tomatoes = (Tomatoes) o;
        return getId().equals(tomatoes.getId())
                && getTomatoesName().equals(tomatoes.getTomatoesName())
                && getTomatoesHeight().equals(tomatoes.getTomatoesHeight())
                && getTomatoesDiameter().equals(tomatoes.getTomatoesDiameter())
                && getTomatoesFruit().equals(tomatoes.getTomatoesFruit())
                && getTomatoesFlowerpot().equals(tomatoes.getTomatoesFlowerpot())
                && getTomatoesAgroTech().equals(tomatoes.getTomatoesAgroTech())
                && getTomatoesDescription().equals(tomatoes.getTomatoesDescription())
                && getTomatoesTaste().equals(tomatoes.getTomatoesTaste())
                && Objects.equals(getTomatoesSpecificity(),
                tomatoes.getTomatoesSpecificity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId()
                , getTomatoesName()
                , getTomatoesHeight()
                , getTomatoesDiameter()
                , getTomatoesFruit()
                , getTomatoesFlowerpot()
                , getTomatoesAgroTech()
                , getTomatoesDescription()
                , getTomatoesTaste()
                , getTomatoesSpecificity());
    }

    @Override
    public String toString() {
        return "Tomatoes{" +
                "id=" + id +
                ", tomatoesName='" + tomatoesName + '\'' +
                ", tomatoesHeight='" + tomatoesHeight + '\'' +
                ", tomatoesDiameter='" + tomatoesDiameter + '\'' +
                ", tomatoesFruit='" + tomatoesFruit + '\'' +
                ", tomatoesFlowerpot='" + tomatoesFlowerpot + '\'' +
                ", tomatoesAgroTech='" + tomatoesAgroTech + '\'' +
                ", tomatoesDescription='" + tomatoesDescription + '\'' +
                ", tomatoesTaste='" + tomatoesTaste + '\'' +
                ", tomatoesSpecificity='" + tomatoesSpecificity + '\'' +
                '}';
    }
}

