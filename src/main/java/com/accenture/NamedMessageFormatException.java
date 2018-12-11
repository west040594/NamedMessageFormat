package com.accenture;

import java.util.Map;

/**
 * Исключение для метода format {@link NamedMessageFormat}.
 * Класс - NamedMessageFormat {@link NamedMessageFormat#format(Map)}
 */
public class NamedMessageFormatException extends Exception {
    public static final String FIRST_LETTER_IS_NUMBER = "Первая буква парамерта имени начинается с цифры";
    public NamedMessageFormatException(String message, String namedKey) {
        super(message + ":" + namedKey);
    }
}
