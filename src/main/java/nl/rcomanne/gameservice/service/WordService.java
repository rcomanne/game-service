package nl.rcomanne.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.Word;
import nl.rcomanne.gameservice.repository.WordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {
    @Value("${words.init}")
    private boolean initialize;

    private final WordRepository repository;

    private final ResourceLoader resourceLoader;
    private final Random r = new Random();

    @PostConstruct
    public void init() {
        if (initialize) {
            log.info("initializing the words");
            final List<Word> words = new ArrayList<>();
            try (
                    final InputStream is = resourceLoader.getResource("classpath:wordlist.txt").getInputStream();
                    final BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
            ) {
                br.lines().forEach(w -> {
                    if (w.contains("-") || w.contains("'") || Character.isUpperCase(w.charAt(0))) {
                        log.debug("found 'invalid' word [{}]", w);
                        return;
                    }
                    log.debug("adding word [{}] to list", w);
                    if (w.length() == 5 || w.length() == 6) {
                        words.add(new Word(w, w.length()));
                    }
                });

                log.info("now saving all the words...");
                repository.saveAll(words);
                log.info("DONE initializing the words");
            } catch (IOException ex) {
                log.error(ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            log.info("words do not have to be initialized");
        }

    }

    public Word findRandomWordWithLength(final int length) {
        final List<Word> words = findAllWordsWithLength(length);
        if (words == null || words.isEmpty()) {
            throw new IllegalStateException("No word found");
        }
        return words.get(r.nextInt(words.size()));
    }

    public List<Word> findAllWordsWithLength(final int length) {
        return repository.findAllByLength(length);
    }

}
