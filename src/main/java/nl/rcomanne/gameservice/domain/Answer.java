package nl.rcomanne.gameservice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String word;

    public Answer(final String word) {
        this.word = word;
    }

    public List<Letter> toLetters() {
        return word.chars().mapToObj(c -> new Letter((char) c, LetterState.CORRECT)).collect(Collectors.toList());
    }
}
