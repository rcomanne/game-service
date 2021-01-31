package nl.rcomanne.gameservice.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.Game;
import nl.rcomanne.gameservice.domain.Player;
import nl.rcomanne.gameservice.service.GameService;
import nl.rcomanne.gameservice.service.PlayerService;
import nl.rcomanne.gameservice.web.dto.GameDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;

    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/list")
    public ResponseEntity<List<Game>> listActiveGames() {
        log.info("returning all active games");
        return ResponseEntity.ok(gameService.findGamesToJoin());
    }

    @PostMapping("/create")
    public ResponseEntity<Game> createGame(@RequestBody final GameDto gameDto) {
        log.info("creating game for [{}]", gameDto.toString());
        return ResponseEntity.ok(gameService.createGame(gameDto));
    }

    @PostMapping("/join/{id}")
    public ResponseEntity<Game> joinGame(@PathVariable("id") long gameId, @RequestBody final Player player) {
        log.info("player [{}] is joining game [{}]", player.getId(), gameId);
        final Game game = gameService.joinGame(gameId, player.getId());
        messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        return ResponseEntity.ok(game);
    }
}
