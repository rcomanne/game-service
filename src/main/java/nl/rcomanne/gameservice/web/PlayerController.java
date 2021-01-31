package nl.rcomanne.gameservice.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.Player;
import nl.rcomanne.gameservice.service.PlayerService;
import nl.rcomanne.gameservice.web.dto.PlayerDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/player")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<Player> createPlayer(@RequestBody PlayerDto dto) {
        log.info("creating player with name {}", dto.getName());
        return ResponseEntity.ok(playerService.save(dto.toPlayer()));
    }
}
