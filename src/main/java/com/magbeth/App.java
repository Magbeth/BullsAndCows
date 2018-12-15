package com.magbeth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App{
    private static List<String> words;
    private static final Random rand = new Random();
    private static String secretWord;
    private int attemtsNumber;

    static {
        try {
            fillWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getAttemtsNumber() {
        return attemtsNumber;
    }

    private static void fillWords() throws IOException {
        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("dictionary.txt");
        words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        }
    }

    private static int randInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    private void setSecretWord() {

        int index = randInt(0, words.size() - 1);
        secretWord = words.get(index);
    }

    private int getSecretWordLength() {
        return secretWord.length();
    }

    private String getSecretWord() {
        return secretWord;
    }

    String getNewGame() {
        attemtsNumber = 10;
        setSecretWord();
        int wordLength = getSecretWordLength();
        String word = getSecretWord();
        System.err.println(word);
        return "Я загадал слово из " + wordLength + " букв.";
    }

     String attempt(String answer) {
        String attemptResult = "";
        int bulls = 0;
        int cows = 0;
        if (attemtsNumber == 1 && !answer.equals(secretWord)) {
            attemptResult += "Вы проиграли.";
            attemtsNumber--;
        }
        else if (answer.length() != secretWord.length()) {
            attemptResult += "Неправильная длина!";
            attemtsNumber--;
        }
        else if (answer.equals(secretWord)) attemptResult += "Угадали!";
        else {
            for(int i = 0; i < answer.length(); i++) {

                char chA = answer.charAt(i);
                char chB = secretWord.charAt(i);
                if (chA == chB) bulls++;
                if (secretWord.indexOf(chA) != -1) cows++;
            }
            attemptResult = "Неверно. Попробуй еще \n" + "Быков: " + bulls + " Коров: "+ cows;
            attemtsNumber--;
        }
        return attemptResult;
    }

    boolean checkForWin(String ans) {
        return ans.equals(secretWord);
    }

}
