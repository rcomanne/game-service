package nl.rcomanne.gameservice.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GameDto {
    private long id;
    private String name;
    private long playerOneId;
    private long playerTwoId;

    public GameDto(final long id, final String name) {
        this.id = id;
        this.name = name;
    }
}
