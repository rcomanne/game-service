package nl.rcomanne.gameservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Letter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;

    private char letter;
    private LetterState state;

    public Letter(final char letter, final LetterState state) {
        this.letter = letter;
        this.state = state;
    }

    public Letter(final char letter) {
        this(letter, LetterState.UNKNOWN);
    }

    @Override
    public String toString() {
        return String.valueOf(this.letter);
    }
}
