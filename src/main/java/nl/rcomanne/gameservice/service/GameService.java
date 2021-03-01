package nl.rcomanne.gameservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import nl.rcomanne.gameservice.domain.Answer;
import nl.rcomanne.gameservice.domain.Game;
import nl.rcomanne.gameservice.domain.GameState;
import nl.rcomanne.gameservice.domain.Letter;
import nl.rcomanne.gameservice.domain.LetterState;
import nl.rcomanne.gameservice.domain.Move;
import nl.rcomanne.gameservice.domain.Player;
import nl.rcomanne.gameservice.domain.Word;
import nl.rcomanne.gameservice.repository.AnswerRepository;
import nl.rcomanne.gameservice.repository.GameRepository;
import nl.rcomanne.gameservice.repository.LetterRepository;
import nl.rcomanne.gameservice.web.dto.GameDto;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private static final int DEFAULT_WORD_LENGTH = 6;
    private static final int DEFAULT_GAME_LENGTH = 6;

    private final GameRepository repository;
    private final AnswerRepository answerRepository;
    private final LetterRepository letterRepository;

    private final PlayerService playerService;
    private final WordService wordService;

    @Transactional
    public List<GameDto> findGamesToJoin() {
        final List<Game> games = repository.findAllByState(GameState.WAITING_FOR_PLAYER);
        final List<GameDto> gamesToJoin = new ArrayList<>();
        for (Game game : games) {
            gamesToJoin.add(new GameDto(game.getId(), game.getName()));
        }
        return gamesToJoin;
    }

    @Transactional
    public Game joinGame(long gameId, long playerId) {
        final Game gameToJoin = findGameById(gameId);
        if (gameToJoin.getState() == GameState.WAITING_FOR_PLAYER) {
            final Player playerTwo = playerService.findPlayerById(playerId);
            playerTwo.setTurn(false);
            playerTwo.setScore(0);
            gameToJoin.setPlayerTwo(playerTwo);
            gameToJoin.setState(GameState.ACTIVE);
            gameToJoin.setMessage(String.format("%s is erbij gekomen, beginnen maar!%n%s is aan de beurt", playerTwo.getName(), gameToJoin.activePlayer().getName()));
            return gameToJoin;
        } else {
            throw new IllegalArgumentException(String.format("Cannot join game %d as it has already started!", gameId));
        }
    }

    @Transactional
    public Game createGame(final GameDto gameDto) {
        final Player playerOne = playerService.findPlayerById(gameDto.getPlayerOneId());
        playerOne.setTurn(true);
        playerOne.setScore(0);

        final Game game = new Game(gameDto.getName(), playerOne, DEFAULT_WORD_LENGTH);
        setAnswerForGame(game);

        return repository.save(game);
    }

    @Transactional
    public Game resetGame(final long gameId) {
        // get the game to reset
        final Game game = this.findGameById(gameId);
        game.reset();

        // create a new answer for the session
        setAnswerForGame(game);

        return game;
    }

    @Transactional
    public Game setAnswerForGame(final Game game) {
        // create a new answer for the session
        final Answer answer = new Answer(wordService.findRandomWordWithLength(DEFAULT_WORD_LENGTH).getWord());
        answerRepository.save(answer);
        game.setAnswer(answer);

        // create the new placeholder
        final Map<Integer, Letter> letters = new HashMap<>(6);
        letters.put(0, letterRepository.save(new Letter(answer.getWord().charAt(0), LetterState.CORRECT)));
        for (int i = 1; i < 6; i++) {
            letters.put(i, letterRepository.save(new Letter('.', LetterState.UNKNOWN)));
        }
        game.setPlaceholder(letters);

        return game;
    }

    @Transactional
    public Game addMoveToGame(final long gameId, Move move) {
        final Game game = findGameById(gameId);
        try {
            validateMove(game, move);

            if (move.getWord().equalsIgnoreCase(game.getAnswer().getWord())) {
                // winner winner chicken dinner
                game.gameFinished();
                game.activePlayer().incrementScore(25);
            } else {
                // not correct, processing the rest
                game.setMessage("Helaas! Probeer het nog eens...");

                final String answer = game.getAnswer().getWord();
                final List<Letter> letters = move.getLetters();
                final Map<Integer, Letter> placeholder = game.getPlaceholder();

                final Map<Integer, Character> alreadyFound = new HashMap<>();
                for (int i = 0; i < letters.size(); i++) {
                    final Letter letter = letters.get(i);
                    // check if letter in position is the same as the one of the answer
                    if (answer.charAt(i) == letter.getLetter()) {
                        letter.setState(LetterState.CORRECT);
                        alreadyFound.replace(i, letter.getLetter());
                        placeholder.put(i, letter);
                    } else {
                        String toIndex = answer;
                        // check if found previously
                        for (Map.Entry<Integer, Character> entry : alreadyFound.entrySet()) {
                            if (entry.getValue().equals(letter.getLetter())) {
                                toIndex = toIndex.substring(entry.getKey());
                            }
                        }

                        // check if letter exists in answer
                        int index = toIndex.indexOf(letter.getLetter());
                        if (index >= 0) {
                            log.debug("answer contains letter [{}]", letter.getLetter());
                            letter.setState(LetterState.WRONG_PLACE);
                            alreadyFound.replace(index, letter.getLetter());
                        } else {
                            letter.setState(LetterState.WRONG);
                        }
                    }
                }

                game.addMove(move);
                if (game.getMoves().size() >= DEFAULT_GAME_LENGTH) {
                    game.setState(GameState.DONE);
                    game.setMessage("Helaas, geen beurten meer over...");
                    game.setPlaceholder(game.getAnswer().toMap());
                }
            }
        } catch (final IllegalArgumentException ex) {
            game.switchTurn(ex.getMessage());
        }
        return game;
    }

    public Game findGameById(final long gameId) {
        final Game game = repository.findById(gameId).orElseThrow(() -> new NoSuchElementException("No game found with id " + gameId));
        Hibernate.initialize(game.getPlaceholder());
        Hibernate.initialize(game.getMoves());
        return game;
    }

    public void validateMove(final Game game, final Move move) throws IllegalArgumentException {
        log.debug("validating move");
        if (game.getMoves().size() >= DEFAULT_GAME_LENGTH) {
            log.debug("no more moves left in the game");
            throw new IllegalStateException("Alle beurten zijn al geweest!");
        }

        // get and sanitize the word
        final String guess = move.getWord().trim().toLowerCase(Locale.ENGLISH);
        move.setWord(guess);

        // validate word length
        if (guess.length() != game.getWordLength()) {
            log.debug("guess length does not equal the answer length");
            throw new IllegalArgumentException(String.format("Het woord moet %d letters zijn!", game.getWordLength()));
        }

        // check if guess already made
        final boolean guessFound = game.getMoves().stream()
            .map(Move::getWord)
            .anyMatch(pastMove -> pastMove.equalsIgnoreCase(move.getWord()));
        if (guessFound) {
            log.debug("guess already made");
            throw new IllegalArgumentException(String.format("Sorry, %s is al geweest...", move.getWord()));
        }

        // check if valid word
        List<Word> validWords = wordService.findAllWordsWithLength(game.getWordLength());
        if (validWords.parallelStream().noneMatch(w -> w.getWord().equalsIgnoreCase(move.getWord()))) {
            log.debug("[{}] is not on our wordlist", move.getWord());
            throw new IllegalArgumentException(String.format("Sorry, %s staat niet in onze woordenlijst...", move.getWord()));
        }
    }
}
