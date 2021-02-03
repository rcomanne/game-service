package nl.rcomanne.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.*;
import nl.rcomanne.gameservice.repository.AnswerRepository;
import nl.rcomanne.gameservice.repository.GameRepository;
import nl.rcomanne.gameservice.repository.LetterRepository;
import nl.rcomanne.gameservice.web.dto.GameDto;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private static final int DEFAULT_WORD_LENGTH = 6;

    private final GameRepository repository;
    private final AnswerRepository answerRepository;
    private final LetterRepository letterRepository;

    private final PlayerService playerService;
    private final WordService wordService;

    @Transactional
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
        final List<Letter> letters = new ArrayList<>();
        letters.add(letterRepository.save(new Letter(answer.getWord().charAt(0), LetterState.CORRECT)));
        game.setPlaceholder(letters);
        playerOne.setTurn(true);
        return repository.save(game);
    }

    @Transactional
    public Game resetGame(final long gameId) {
        // get the game to reset
        final Game game = this.findGameById(gameId);
        game.reset();

        // create a new answer for the session
        final Answer answer = new Answer(wordService.findRandomWordWithLength(DEFAULT_WORD_LENGTH).getWord());
        answerRepository.save(answer);
        game.setAnswer(answer);
        // create the new placeholder
        final List<Letter> letters = new ArrayList<>();
        letters.add(letterRepository.save(new Letter(answer.getWord().charAt(0), LetterState.CORRECT)));
        game.setPlaceholder(letters);

        return game;
    }

    @Transactional
    public Game addMoveToGame(final long gameId, final Move move) {
        final Game game = findGameById(gameId);
        try {
            validateMove(game, move);
            game.addMove(move);

            if (move.getWord().equalsIgnoreCase(game.getAnswer().getWord())) {
                // winner winner chicken dinner
                game.gameFinished();
            } else {
                // not correct, processing the rest
                game.setMessage("try again");

                final String answer = game.getAnswer().getWord();
                final List<Letter> letters = move.getLetters();

                final Map<Integer, Character> alreadyFound = new HashMap<>();
                for (int i = 0; i < letters.size(); i++) {
                    final Letter letter = letters.get(i);

                    // check if letter in position is the same as the one of the answer
                    if (answer.charAt(i) == letter.getLetter()) {
                        letter.setState(LetterState.CORRECT);
                        alreadyFound.put(i, letter.getLetter());
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
                            alreadyFound.put(index, letter.getLetter());
                        } {
                            letter.setState(LetterState.WRONG);
                        }
                    }
                }
                game.setPlaceholder(letters);
            }
        } catch (final IllegalArgumentException ex) {
            game.switchTurn(ex.getMessage());
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
