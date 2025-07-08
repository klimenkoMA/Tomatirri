package accountingApp.entity;

import org.bson.types.ObjectId;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.*;
import java.util.*;

/**
 * Семена перцев
 */

@Document(collection = "peppers")
public class Peppers extends MultipartFileAdapter implements Seed {
    @Id
    private ObjectId id;
    @Field
    private PeppersCategory category;
    @Field
    private String peppersName;
    @Field
    private String peppersHeight;
    @Field
    private String peppersDiameter;
    @Field
    private String peppersFruit;
    @Field
    private String peppersFlowerpot;
    @Field
    private String peppersAgroTech;
    @Field
    private String peppersDescription;
    @Field
    private String peppersTaste;
    @Field
    private String peppersSpecificity;
    @Field
    private Long idCount;
    @Field
    private Map<ObjectId, Long> idMap = new HashMap<>();
    @Field
    private IsPresent isPresent;
    @Field
    @Lazy
    private List<Photo> photos = new ArrayList<>();

    public Peppers() {
    }

    public Peppers(PeppersCategory category
            , String peppersName
            , String peppersHeight
            , String peppersDiameter
            , String peppersFruit
            , String peppersFlowerpot
            , String peppersAgroTech
            , String peppersDescription
            , String peppersTaste
            , String peppersSpecificity
            , IsPresent isPresent) {
        this.category = category;
        this.peppersName = peppersName;
        this.peppersHeight = peppersHeight;
        this.peppersDiameter = peppersDiameter;
        this.peppersFruit = peppersFruit;
        this.peppersFlowerpot = peppersFlowerpot;
        this.peppersAgroTech = peppersAgroTech;
        this.peppersDescription = peppersDescription;
        this.peppersTaste = peppersTaste;
        this.peppersSpecificity = peppersSpecificity;
        this.isPresent = isPresent;
    }

    public Peppers(ObjectId id
            , PeppersCategory category
            , String peppersName
            , String peppersHeight
            , String peppersDiameter
            , String peppersFruit
            , String peppersFlowerpot
            , String peppersAgroTech
            , String peppersDescription
            , String peppersTaste
            , String peppersSpecificity
            , IsPresent isPresent) {
        this.id = id;
        this.category = category;
        this.peppersName = peppersName;
        this.peppersHeight = peppersHeight;
        this.peppersDiameter = peppersDiameter;
        this.peppersFruit = peppersFruit;
        this.peppersFlowerpot = peppersFlowerpot;
        this.peppersAgroTech = peppersAgroTech;
        this.peppersDescription = peppersDescription;
        this.peppersTaste = peppersTaste;
        this.peppersSpecificity = peppersSpecificity;
        this.isPresent = isPresent;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public PeppersCategory getCategory() {
        return category;
    }

    public void setCategory(PeppersCategory category) {
        this.category = category;
    }

    public String getPeppersName() {
        return peppersName;
    }

    public void setPeppersName(String peppersName) {
        this.peppersName = peppersName;
    }

    public String getPeppersHeight() {
        return peppersHeight;
    }

    public void setPeppersHeight(String tomatoesHeight) {
        this.peppersHeight = tomatoesHeight;
    }

    public String getPeppersDiameter() {
        return peppersDiameter;
    }

    public void setPeppersDiameter(String peppersDiameter) {
        this.peppersDiameter = peppersDiameter;
    }

    public String getPeppersFruit() {
        return peppersFruit;
    }

    public void setPeppersFruit(String peppersFruit) {
        this.peppersFruit = peppersFruit;
    }

    public String getPeppersFlowerpot() {
        return peppersFlowerpot;
    }

    public void setPeppersFlowerpot(String peppersFlowerpot) {
        this.peppersFlowerpot = peppersFlowerpot;
    }

    public String getPeppersAgroTech() {
        return peppersAgroTech;
    }

    public void setPeppersAgroTech(String peppersAgroTech) {
        this.peppersAgroTech = peppersAgroTech;
    }

    public String getPeppersDescription() {
        return peppersDescription;
    }

    public void setPeppersDescription(String peppersDescription) {
        this.peppersDescription = peppersDescription;
    }

    public String getPeppersTaste() {
        return peppersTaste;
    }

    public void setPeppersTaste(String peppersTaste) {
        this.peppersTaste = peppersTaste;
    }

    public String getPeppersSpecificity() {
        return peppersSpecificity;
    }

    public void setPeppersSpecificity(String peppersSpecificity) {
        this.peppersSpecificity = peppersSpecificity;
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
        if (!(o instanceof Peppers)) return false;
        Peppers peppers = (Peppers) o;
        return getId().equals(peppers.getId())
                && getCategory() == peppers.getCategory()
                && getPeppersName().equals(peppers.getPeppersName())
                && getPeppersHeight().equals(peppers.getPeppersHeight())
                && getPeppersDiameter().equals(peppers.getPeppersDiameter())
                && getPeppersFruit().equals(peppers.getPeppersFruit())
                && getPeppersFlowerpot().equals(peppers.getPeppersFlowerpot())
                && getPeppersAgroTech().equals(peppers.getPeppersAgroTech())
                && getPeppersDescription().equals(peppers.getPeppersDescription())
                && getPeppersTaste().equals(peppers.getPeppersTaste())
                && Objects.equals(getPeppersSpecificity(), peppers.getPeppersSpecificity())
                && getIdCount().equals(peppers.getIdCount())
                && getIdMap().equals(peppers.getIdMap())
                && getIsPresent() == peppers.getIsPresent()
                && getPhotos().equals(peppers.getPhotos());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId()
                , getCategory()
                , getPeppersName()
                , getPeppersHeight()
                , getPeppersDiameter()
                , getPeppersFruit()
                , getPeppersFlowerpot()
                , getPeppersAgroTech()
                , getPeppersDescription()
                , getPeppersTaste()
                , getPeppersSpecificity()
                , getIdCount()
                , getIdMap()
                , getIsPresent()
                , getPhotos());
    }

    @Override
    public String toString() {
        return "Peppers{" +
                "id=" + id +
                ", category=" + category +
                ", tomatoesName='" + peppersName + '\'' +
                ", tomatoesHeight='" + peppersHeight + '\'' +
                ", tomatoesDiameter='" + peppersDiameter + '\'' +
                ", tomatoesFruit='" + peppersFruit + '\'' +
                ", tomatoesFlowerpot='" + peppersFlowerpot + '\'' +
                ", tomatoesAgroTech='" + peppersAgroTech + '\'' +
                ", tomatoesDescription='" + peppersDescription + '\'' +
                ", tomatoesTaste='" + peppersTaste + '\'' +
                ", tomatoesSpecificity='" + peppersSpecificity + '\'' +
                ", idCount=" + idCount +
                ", idMap=" + idMap +
                ", isPresent=" + isPresent +
                ", photos=" + photos +
                '}';
    }
}
