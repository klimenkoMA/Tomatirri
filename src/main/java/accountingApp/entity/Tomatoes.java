package accountingApp.entity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.*;


/**
 * Устройства для выдачи
 */

@Document(collection = "tomatoes")
public class Tomatoes {
    @Id
    private ObjectId id;


}

