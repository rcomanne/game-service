package nl.rcomanne.gameservice.repository;

import nl.rcomanne.gameservice.domain.Word;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WordRepository extends PagingAndSortingRepository<Word, String> {
    List<Word> findAllByLength(final int length);
}
