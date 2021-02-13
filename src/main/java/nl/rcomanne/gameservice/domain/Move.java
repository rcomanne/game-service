package nl.rcomanne.gameservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "move")
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private long playerId;

    private String word;

    @OneToMany(cascade = { CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Letter> letters;

    public List<Letter> getLetters() {
        if (this.letters == null || this.letters.isEmpty()) {
            final List<Letter> letters = new ArrayList<>();
            for (char character : word.toCharArray()) {
                letters.add(new Letter(character));
            }
            this.letters = letters;
        }
        return this.letters;
    }
}
