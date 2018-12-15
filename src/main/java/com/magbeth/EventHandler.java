package com.magbeth;

import com.magbeth.util.JsonHelper;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.HashSet;

import java.util.Set;


@Component
public class EventHandler extends TextWebSocketHandler implements WebSocketHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventHandler.class);

    private static Set<WebSocketSession> chatEndpoints = new HashSet<>();
    private WebSocketSession session;

    private App app = new App();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        this.session = session;
        System.out.println("Socket Connected: " + this.session);
        chatEndpoints.add(this.session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String topic = JsonHelper.getJsonNode(payload).get("topic").textValue().toLowerCase();
        switch (topic) {
            case "start":
                broadcast(start());
                break;
            case "answer":
                broadcast(answer(payload));
                gameOverCheck(payload);
                break;
            default:
                broadcast(new TextMessage("Unknown topic!"));
                break;
        }
        log.info("Received " + payload);
    }

    private TextMessage start() {
        return new TextMessage(app.getNewGame());
    }

    private TextMessage answer(String payload) {
        String ans = JsonHelper.getJsonNode(payload).get("data").get("msg").textValue();
        TextMessage msg = new TextMessage("Попытка № "+(11 - app.getAttemtsNumber()) + "\n " + "Ваш ответ: " + ans + "\n " + app.attempt(ans));
        return msg;

    }
    private void gameOverCheck(String payload) {
        String ans = JsonHelper.getJsonNode(payload).get("data").get("msg").textValue();
        if ((11 - app.getAttemtsNumber()) > 10 || app.checkForWin(ans)) {
            try {
                this.session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void broadcast(TextMessage message) {
        chatEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.error("Socket Closed: [" + closeStatus.getCode() + "] " + closeStatus.getReason());
        super.afterConnectionClosed(session, closeStatus);
        chatEndpoints.remove(session);
    }
}



