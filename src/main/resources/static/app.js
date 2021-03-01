let stompClient = null;
let connected = false;

function initializeGame() {
    updateGame(getGame());
    connect();
}

function connect() {
    // Connect to WebSocket
    const socket = new SockJS('/letter-game');
    // Create the client
    stompClient = Stomp.over(socket);
    // Actually connect
    stompClient.connect({}, function (frame) {
        this.connected = true;
        console.log('Connected: ' + frame);
        // Subscribe to the topic
        stompClient.subscribe('/topic/game/' + getGame().id, function (response) {
            console.log("received move from WS: " + response)
            updateGame(JSON.parse(response.body));
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    this.connected = true;
    console.log("Disconnected");
}

function loadIndex() {
    let player = this.getPlayer();
    if (player !== null) {
        // check if player actually exists
        $.ajax({
            url: `/player/${player.id}`,
            headers: {
                'Content-Type': 'application/json'
            },
            method: 'GET',
            dataType: "json"
        })
            .done(function (receivedPlayer, status) {
                console.log(status)
                if (status === "success") {
                    setPlayer(receivedPlayer);
                    window.location.href = "overview.html";
                }
            })
            .fail(function (data, status) {
                console.log(`status: ${status}, response: ${data}`);
            })
    }
}

function loadOverview() {
    setName();
    getGames();
}

function createPlayer() {
    const playerName = $("#playerName").val();
    $.ajax({
        url: "/player/create",
        headers: {
            'Content-Type': 'application/json'
        },
        method: 'POST',
        data: JSON.stringify({'name': playerName}),
        dataType: "json"
    })
        .done(function (player, status) {
            console.log(status)
            if (status === "success") {
                setPlayer(player);
                window.location.href = "overview.html";
            } else {
                console.log("failed?");
            }
        })
        .fail(function (data, status) {
            alert("FAILED! Data: " + data + "\nStatus: " + status);
        })
}

function createGame() {
    const gameName = $("#gameName").val()
    $.ajax({
        url: "/game/create",
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({'name': gameName, 'playerOneId': getPlayer().id}),
        dataType: 'json'
    })
        .done(function (game, status) {
            console.log(status)
            if (status === "success") {
                console.log(game);
                setGame(game);
                window.location.href = "game.html";
            } else {
                console.log("failed?");
            }
        })
        .fail(function (data, success) {
            alert("FAILED! Data: " + data + "\nStatus: " + success);
        })
}

function createMove() {
    const word = $("#word-input").val()
    stompClient.send("/app/game/" + getGame().id, {}, JSON.stringify({
        'playerId': getPlayer().id,
        'word': word
    }))
}

function getGames() {
    $.ajax({
        url: '/game/list',
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
        dataType: 'json'
    })
        .done(function (games, status) {
            console.log(games, status)
            if (status === "success") {
                for (let i = 0; i < games.length; i++) {
                    addGameToOverview(games[i]);
                }
            } else {
                console.log("failed to get games?")
            }
        })
}

function joinGame(gameId) {
    console.log(`joining game with id ${gameId}`)
    $.ajax({
        url: `/game/join/${gameId}`,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify({"id": getPlayer().id}),
        dataType: 'json'
    })
        .done(function (game, status) {
            console.log(status)
            if (status === "success") {
                console.log(game);
                setGame(game);
                connect()
                window.location.href = "game.html";
            } else {
                console.log("failed?");
            }
        })
        .fail(function (data, success) {
            alert("FAILED! Data: " + data + "\nStatus: " + success);
        })
}

function addGameToOverview(game) {
    let html = [];
    html.push(`<tr><td>${game.name}</td>`);
    html.push(`<td><button id="${game.id}" class="btn btn-secondary" type="submit" onclick='joinGame(this.id)'>Join game</button></td></tr>`);

    $("#games").append(html.join(''));
}

function updateGame(game) {
    if (game !== undefined) {
        setGame(game);
        if (game.message !== undefined) {
            let message = [];
            message.push('<h1 id="game-message" class="mx-auto p-3">');
            message.push(`${game.message}`);
            message.push('</h1>');
            $("#game-message").replaceWith(message.join(""));
        }

        if (game.playerOne !== null && game.playerOne !== undefined) {
            let player = [];
            if (game.playerOne.turn) {
                player.push(`<div id="player-one" class="border border-success rounded active_player p3">`);
            } else {
                player.push(`<div id="player-one" class="border border-warning rounded p3">`);
            }
            player.push(`<h1 class="py-3">${game.playerOne.name}</h1>`)
            player.push(`<h2 class="py-2">${game.playerOne.score}</h2>`)
            player.push(`</div>`);
            $("#player-one").replaceWith(player.join(""));
        }

        if (game.playerTwo !== null && game.playerTwo !== undefined) {
            let player = [];
            if (game.playerTwo.turn) {
                player.push(`<div id="player-two" class="border border-success rounded active_player p3">`);
            } else {
                player.push(`<div id="player-two" class="border border-warning rounded p3">`);
            }
            player.push(`<h1 class="py-3">${game.playerTwo.name}</h1>`)
            player.push(`<h2 class="py-2">${game.playerTwo.score}</h2>`)
            player.push(`</div>`);
            $("#player-two").replaceWith(player.join(""));
        }

        let moves = [];
        moves.push('<tbody id="moves">');

        if (game.moves !== undefined && game.moves.length !== 0) {
            for (let i = 0; i < game.moves.length; i++) {
                moves.push('<tr><td>')
                for (let x = 0; x < game.moves[i].letters.length; x++) {
                    moves.push(`<p class="${game.moves[i].letters[x].state.toLowerCase()} letter">${game.moves[i].letters[x].letter}</p>`);
                }
                moves.push('</tr></td>')
                moves.push("\n")
            }
        }

        if (game.placeholder !== undefined) {
            moves.push('<tr><td>')
            for (const letter of Object.values(game.placeholder)) {
                switch (letter.state) {
                    case 'CORRECT':
                        moves.push(`<p class="correct letter">${letter.letter}</p>`);
                        break;
                    case 'WRONG_PLACE':
                        moves.push(`<p class="wrong_place letter">${letter.letter}</p>`);
                        break;
                    case 'WRONG':
                        moves.push(`<p class="wrong letter">${letter.letter}</p>`);
                        break;
                    default:
                        moves.push(`<p class="empty letter">.</p>`);
                        break;

                }
            }
            moves.push('</tr></td>')
            moves.push("\n")
        }

        moves.push("</tbody>")
        $("#moves").replaceWith(moves.join(""))

        if (game.state === "ACTIVE") {
            $("#word-input")[0].disabled = !hasTurn();
            $("#create-move")[0].disabled = !hasTurn();
        } else {
            $("#word-input")[0].disabled = true;
            $("#create-move")[0].disabled = true;
        }
    }
}

function hasTurn() {
    let activePlayerId = getActivePlayerId();
    let playerId = getPlayer().id;
    console.log(`playerId: ${playerId}, activePlayerId: ${activePlayerId}`)
    return getPlayer().id === getActivePlayerId()
}

function getActivePlayerId() {
    let game = getGame();
    if (game.playerOne !== undefined && game.playerTwo !== undefined) {
        if (game.playerOne.turn) {
            return game.playerOne.id;
        } else if (game.playerTwo.turn) {
            return game.playerTwo.id;
        }
    }
    return -1
}

function setName() {
    const name = getPlayer().name;
    console.log("setting name to " + name);
    $("#overview-name").replaceWith(`<div class="col" id="overview-name"><h2>Hallo ${name}!</h2></div>`);

}

function setPlayer(player) {
    localStorage.setItem('player', JSON.stringify(player));
}

function getPlayer() {
    return JSON.parse(localStorage.getItem('player'));
}

function setGame(game) {
    localStorage.setItem('game', JSON.stringify(game));
}

function getGame() {
    return JSON.parse(localStorage.getItem('game'));
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#join-game").click(function () {
        joinGame();
    });
    $("#create-game").click(function () {
        createGame();
    });
    $("#create-player").click(function () {
        createPlayer();
    });
    $("#create-move").click(function () {
        createMove();
    });
})