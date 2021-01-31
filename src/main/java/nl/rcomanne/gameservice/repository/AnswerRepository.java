package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Answer;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnswerRepository extends PagingAndSortingRepository<Answer, Long> {
}
