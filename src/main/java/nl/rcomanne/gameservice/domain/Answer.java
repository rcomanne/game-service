package nl.rcomanne.gameservice.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public Map<Integer, Letter> toMap() {
        final Map<Integer, Letter> letterMap = new HashMap<>(6);
        for (int i = 0; i < word.length(); i++) {
            letterMap.put(i, new Letter(word.charAt(i), LetterState.CORRECT));
        }
        return letterMap;
    }
}
