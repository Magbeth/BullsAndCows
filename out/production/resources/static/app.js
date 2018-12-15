var host = 'localhost';
var port = 8090;
// var path = 'chat';
var sock;

function say() {
    var msg = $('#msg').val();
    var msgData = JSON.stringify(
        {
            "topic": "answer",
            "data": {
                "msg": msg
            }
        });
    sock.send(msgData);
}

function startGame() {
    document.getElementById("start").disabled = true;
    sock = new SockJS('http://' + host + ':' + port + '/game');
    sock.onopen = function() {
        console.log('open');
        gameStart();
        // updateHistory();
    };

    sock.onmessage = function (e) {
        console.log(e.data);
        // check topic if you will provide other than "history"
        setHistory(e.data)
    };

    sock.onclose = function() {
        console.log('close');
        // updateHistory();
    };
}


function gameStart() {
    var start = JSON.stringify(
        {
            "topic": "start",
            "data": ""
        }
    );
    sock.send(start)
}





function setHistory(data) {
    $("#history").append(data).append("<br />");
    $("#history").scrollTop($("#history")[0].scrollHeight);
}