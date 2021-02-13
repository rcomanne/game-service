let stompClient = null;
let connected = false;

function setConnected(connected) {
    this.connected = connected;
}

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
        setConnected(true);
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
    setConnected(false);
    console.log("Disconnected");
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
                console.log(player);
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
                for (i = 0; i < games.length; i++) {
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
                connect(game.id)
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
    $("#games").append(
        "<tr><td>" +
        game.name +
        `<button id="${game.id}" class="btn btn-default" type="submit" onclick='joinGame(this.id)'>Join game</button>` +
        "</td></tr>"
    )
}

function updateGame(game) {
    setGame(game);
    if (game !== undefined) {
        let message = []
        message.push('<tr id="game-message"><th>');
        if (game.message !== undefined) {
            message.push(`${game.message}`);
        } else {
            message.push(`Raad een ${game.wordLength} letter woord!`)
        }
        message.push('</th></tr>');
        $("#game-message").replaceWith(message.join(""));

        let html = [];
        html.push('<tbody id="moves">');

        if (game.moves !== undefined && game.moves.length !== 0) {
            for (let i = 0; i < game.moves.length; i++) {
                html.push('<tr><td>')
                for (let x = 0; x < game.moves[i].letters.length; x++) {
                    html.push(`<p class="${game.moves[i].letters[x].state.toLowerCase()} letter">${game.moves[i].letters[x].letter}</p>`);
                }
                html.push('</tr></td>')
                html.push("\n")
            }
        }

        if (game.placeholder !== undefined && game.placeholder.length !== 0) {
            html.push('<tr><td>')
            for (let i = 0; i < game.placeholder.length; i++) {
                if (game.placeholder[i].state === 'CORRECT') {
                    html.push(`<p class="${game.placeholder[i].state.toLowerCase()} letter">${game.placeholder[i].letter}</p>`);
                } else {
                    html.push(`<p class="empty letter">.</p>`);
                }
            }
            html.push('</tr></td>')
            html.push("\n")
        }

        html.push("</tbody>")
        $("#moves").replaceWith(html.join(""))

        if (game.state === "ACTIVE") {
            $("#word-input")[0].disabled = !hasTurn();
        } else {
            $("#word-input")[0].disabled = true;
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
    $("#overview-name").replaceWith(`<div id="overview-name"><h2>Hallo ${name}!</h2></div>`);
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