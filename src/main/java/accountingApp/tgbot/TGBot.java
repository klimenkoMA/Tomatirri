package accountingApp.tgbot;

import javax.persistence.*;

@Entity
@Table(name="channel_posts")
public class TGBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
}
