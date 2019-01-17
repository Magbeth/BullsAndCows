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
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;


@Component
public class EventHandler extends TextWebSocketHandler implements WebSocketHandler {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventHandler.class);

    private static Set<WebSocketSession> chatEndpoints = new HashSet<>();
    private Map<WebSocketSession, App> sessions= new HashMap();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("Socket Connected: " + session);
        chatEndpoints.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String topic = JsonHelper.getJsonNode(payload).get("topic").textValue().toLowerCase();
        switch (topic) {
            case "start":
                broadcast(start(session), session);
                break;
            case "answer":
                broadcast(answer(payload, session), session);
                gameOverCheck(payload, session);
                break;
            default:
                broadcast(new TextMessage("Unknown topic!"), session);
                break;
        }
        log.info("Received " + payload);
    }

    private TextMessage start(WebSocketSession session) {
        App app = new App();
        sessions.put(session, app);
//        System.err.println("saved " + app.getSecretWord()+" for " + session);
        return new TextMessage("Я загадал слово из " + app.getSecretWordLength() + " букв");
    }

    private TextMessage answer(String payload, WebSocketSession session) {
        String ans = JsonHelper.getJsonNode(payload).get("data").get("msg").textValue();
        String quiz = sessions.get(session).getSecretWord();
        System.err.println("get " + sessions.get(session).getSecretWord()+" for " + session);
        TextMessage msg = new TextMessage("Попытка № "+(11 - sessions.get(session).getAttemtsNumber()) + "\n " + "Ваш ответ: " + ans + "\n " + sessions.get(session).attempt(quiz, ans) + "From "+ session);
        return msg;

    }
    private void gameOverCheck(String payload, WebSocketSession session) {
        String ans = JsonHelper.getJsonNode(payload).get("data").get("msg").textValue();
        String quiz = sessions.get(session).getSecretWord();
        if ((11 - sessions.get(session).getAttemtsNumber()) > 10 || sessions.get(session).checkForWin(quiz, ans)) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void broadcast(TextMessage message, WebSocketSession session) {

//            synchronized (this.session) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.error("Socket Closed: [" + closeStatus.getCode() + "] " + closeStatus.getReason());
        super.afterConnectionClosed(session, closeStatus);
        sessions.remove(session, sessions.get(session));
        chatEndpoints.remove(session);
    }
}



