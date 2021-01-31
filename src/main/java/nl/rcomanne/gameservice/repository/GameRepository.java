package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Game;
import nl.rcomanne.gameservice.domain.GameState;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GameRepository extends PagingAndSortingRepository<Game, Long> {
    List<Game> findAllByState(final GameState state);
}
