package nl.rcomanne.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.Player;
import nl.rcomanne.gameservice.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository repository;

    public Player save(Player player) {
        return repository.save(player);
    }

    public Player findPlayerById(final long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("No player with id " + id));
    }
}
