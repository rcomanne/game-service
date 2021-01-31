package nl.rcomanne.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.*;
import nl.rcomanne.gameservice.repository.AnswerRepository;
import nl.rcomanne.gameservice.repository.GameRepository;
import nl.rcomanne.gameservice.web.dto.GameDto;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private static final int DEFAULT_WORD_LENGTH = 6;

    private final GameRepository repository;
    private final AnswerRepository answerRepository;

    private final PlayerService playerService;
    private final WordService wordService;

    public List<Game> findGamesToJoin() {
        return repository.findAllByState(GameState.WAITING_FOR_PLAYER);
    }

    @Transactional
    public Game joinGame(long gameId, long playerId) {
        final Game gameToJoin = findGameById(gameId);
        if (gameToJoin.getState() == GameState.WAITING_FOR_PLAYER) {
            final Player playerTwo = playerService.findPlayerById(playerId);
            playerTwo.setTurn(false);
            gameToJoin.setPlayerTwo(playerTwo);
            gameToJoin.setState(GameState.ACTIVE);
            return gameToJoin;
        } else {
            throw new IllegalArgumentException(String.format("Cannot join game %d as it has already started!", gameId));
        }
    }

    @Transactional
    public Game createGame(final GameDto gameDto) {
        final Player playerOne = playerService.findPlayerById(gameDto.getPlayerOneId());
        final Answer answer = new Answer(wordService.findRandomWordWithLength(DEFAULT_WORD_LENGTH).getWord());
        answerRepository.save(answer);
        final Game game = new Game(gameDto.getName(), playerOne, DEFAULT_WORD_LENGTH);
        game.setAnswer(answer);
        playerOne.setTurn(true);
        return repository.save(game);
    }

    @Transactional
    public Game addMoveToGame(final long gameId, final Move move) {
        final Game game = findGameById(gameId);
        try {
            validateMove(game, move);
            game.addMove(move);
            if (move.getWord().equalsIgnoreCase(game.getAnswer().getWord())) {
                game.setState(GameState.DONE);
                game.setMessage("correct, you win!");
            } else {
                game.setMessage("try again");
            }
        } catch (final IllegalArgumentException ex) {
            game.switchTurn();
            game.setMessage(ex.getMessage());
        }
        return game;
    }

    public Game findGameById(final long gameId) {
        return repository.findById(gameId).orElseThrow(() -> new NoSuchElementException("No game found with id " + gameId));
    }

    @Transactional
    public void validateMove(final Game game, final Move move) throws IllegalArgumentException {
        // get and sanitize the word;
        final String guess = move.getWord().trim().toLowerCase(Locale.ENGLISH);
        move.setWord(guess);

        // validate word length
        if (guess.length() != game.getWordLength()) {
            throw new IllegalArgumentException("guess has to be 6 characters");
        }

        // check if guess already made
        if (game.getMoves().stream()
                .map(Move::getWord)
                .anyMatch(pastMove -> pastMove.equalsIgnoreCase(move.getWord()))) {
            throw new IllegalArgumentException("guess already made");
        }

        // check if valid word
        List<Word> validWords = wordService.findAllWordsWithLength(game.getWordLength());
        if (validWords.parallelStream().noneMatch(w -> w.getWord().equalsIgnoreCase(move.getWord()))) {
            throw new IllegalArgumentException("not a valid word");
        }
    }
}
