package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Player;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlayerRepository extends PagingAndSortingRepository<Player, Long> {
}
