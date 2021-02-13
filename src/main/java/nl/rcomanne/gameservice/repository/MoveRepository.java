package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Move;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MoveRepository extends PagingAndSortingRepository<Move, Long> {
}
