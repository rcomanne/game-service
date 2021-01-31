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
    private String name;
    private Long playerOneId;
    private Long playerTwoId;
}
