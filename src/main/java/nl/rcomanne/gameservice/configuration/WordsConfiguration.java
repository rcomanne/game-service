package nl.rcomanne.gameservice.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "words")
public class WordsConfiguration {
    private boolean init;
}
