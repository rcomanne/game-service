package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Letter;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LetterRepository extends PagingAndSortingRepository<Letter, Long> {
}
