package nl.rcomanne.gameservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private String message;

    @JsonIgnore
    @OneToOne
    private Answer answer;
    private int wordLength;

    @OneToOne
    private Player playerOne;

    @OneToOne
    private Player playerTwo;

    private GameState state;

    @OneToMany(cascade = { CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Move> moves;

    public Game(final String name, final Player player, final int wordLength) {
        this.state = GameState.WAITING_FOR_PLAYER;
        this.name = name;
        this.playerOne = player;
        this.wordLength = wordLength;
    }

    public void addMove(final Move move) {
        if (this.moves == null) {
            this.moves = new ArrayList<>();
        }
        this.moves.add(move);
    }

    public void switchTurn() {
        this.playerOne.setTurn(!this.playerOne.isTurn());
        this.playerTwo.setTurn(!this.playerTwo.isTurn());
    }

    public Player activePlayer() {
        if (this.playerOne.isTurn()) {
            return this.playerOne;
        } else if (this.playerTwo.isTurn()) {
            return this.playerTwo;
        } else {
            throw new IllegalStateException("no active player found in session");
        }
    }
}
