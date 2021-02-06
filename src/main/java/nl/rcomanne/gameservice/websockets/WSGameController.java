package nl.rcomanne.gameservice.websockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.rcomanne.gameservice.domain.Game;
import nl.rcomanne.gameservice.domain.Move;
import nl.rcomanne.gameservice.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WSGameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game")
    @SendTo("/topic/game")
    public void test() {
        log.info("processing test");
    }

    @MessageMapping("/game/{gameId}")
    public void addMove(@DestinationVariable final long gameId, final Move move) {
        log.info("processing move for game [{}]", gameId);
        try {
            final Game game = gameService.addMoveToGame(gameId, move);
            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
        } catch (final IllegalArgumentException ex) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, gameService.findGameById(gameId));
        }
    }

//    @MessageMapping("/game/{gameId}")
//    public void addMove(@DestinationVariable final long gameId, final boolean reset) {
//        log.info("resetting game [{}]", gameId);
//        try {
//            Game game;
//            if (reset) {
//                game = gameService.resetGame(gameId);
//            } else {
//                game = gameService.findGameById(gameId);
//            }
//            messagingTemplate.convertAndSend("/topic/game/" + gameId, game);
//        } catch (final IllegalArgumentException ex) {
//            messagingTemplate.convertAndSend("/topic/game/" + gameId, gameService.findGameById(gameId));
//        }
//    }
}
