package nl.rcomanne.gameservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Move> moves;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Map<Integer, Letter> placeholder;

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

    public void switchTurn(final String message) {
        this.playerOne.setTurn(!this.playerOne.isTurn());
        this.playerTwo.setTurn(!this.playerTwo.isTurn());
        this.message = String.format("%s%n%s is aan de beurt", message, this.activePlayer().getName());
    }

    public Player activePlayer() {
        if (this.playerOne.isTurn()) {
            return this.playerOne;
        } else if (this.playerTwo.isTurn()) {
            return this.playerTwo;
        } else {
            throw new IllegalStateException("Geen actieve speler in de sessie");
        }
    }

    public void gameFinished() {
        this.state = GameState.DONE;
        this.setPlaceholder(this.answer.toMap());
        this.message = String.format("Gefeliciteerd %s, je hebt gewonnen!", this.activePlayer().getName());
    }

    public void reset() {
        this.state = GameState.ACTIVE;
        this.message = String.format("Nieuwe ronde! %s is aan de beurt", this.activePlayer().getName());
    }
}
