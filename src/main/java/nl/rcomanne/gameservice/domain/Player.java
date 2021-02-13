package nl.rcomanne.gameservice.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Game activeGame;
    private int score;

    private String name;
    private boolean turn;

    public Player(final String name) {
        this.name = name;
        this.turn = false;
    }

    public void incrementScore(int toAdd) {
        this.score += toAdd;
    }
}
