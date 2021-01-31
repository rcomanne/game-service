package nl.rcomanne.gameservice.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.rcomanne.gameservice.domain.Player;

@Getter
@Setter
@NoArgsConstructor
public class PlayerDto {
    private String name;

    public Player toPlayer() {
        return new Player(this.name);
    }
}
