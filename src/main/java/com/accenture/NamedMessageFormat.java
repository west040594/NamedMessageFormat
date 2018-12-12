package com.accenture;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * NamedMessageFormat класс
 * Содержит строку-шаблон формата {ArgumentName, FormatType , FormatStyle}
 * Метод format {@link NamedMessageFormat#format(Map)} получает коллекцию параметров
 * и подставляет их в шаблон. В результате возращается форматрованная строка с вставленными параметрами.
 * Если ArgumentName не соотвуестует требованиям(должен состоять из буквы, цифры, подчеркивание,
 * первый символ не может быть цифрой) то в этом случае будет выброшено исключение NamedMessageFormatException.
 */
public class NamedMessageFormat {

    private String pattern;
    //Паттерн  - {любая строка}
    private final Pattern PARAM_PATTERN = Pattern.compile("\\{(.*?)\\}");
    //Первый символ не должен начинаться цифры. Строка может содержать цифры, буквы, нижнее подчеркивание
    private final String NAME_PARAM_PATTERN = "^[a-zA-Z_][a-zA-Z0-9_]+$";

    /**
     * Стандартный конструктор. Устанавливает шаблон для форматировании строки
     * @param pattern Шаблон для форматирования строки
     */
    public NamedMessageFormat(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Форматирование коллекции Map для создания строки
     * @param namedArguments Колекция Map аргументов для форматирования
     * @return Форматированная строка
     * @throws NamedMessageFormatException Если параметр имени не соотвествует стандарту.
     * (Начинается с цифры)
     */
    public String format(Map<String, Object> namedArguments) throws NamedMessageFormatException {
        ConcurrentMap<String, Object> namedArgumentsConcurrentMap = new ConcurrentHashMap<>(namedArguments);
        List<Object> arguments = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();

        Matcher paramMatcher = PARAM_PATTERN.matcher(pattern);
        for (int i = 0; paramMatcher.find(); i++) {
            //Найденый параметр формата {... , ... , ...}
            String mathParam = paramMatcher.group();
            //Взятие первой части параметра(имени) до запятой и удаление начальной скобки
            String mathParamFirst = (mathParam.split(",")[0]).substring(1);

            //Если парамер имени не соотвествует требуемому стандарту - исключение
            if(!Pattern.matches(NAME_PARAM_PATTERN, mathParamFirst)) {
                throw new NamedMessageFormatException(
                        NamedMessageFormatException.FIRST_LETTER_IS_NUMBER, mathParamFirst);
            }
            //Замена первой части параметра(имени) на индекс
            String mathParamWithIndex =  mathParam.replace(mathParamFirst, String.valueOf(i));

            //Если в коллекции Map сущестует ключ с соотвествующим параметром имени
            // то добавляем в список аргументов для MessageFormat
            if (namedArgumentsConcurrentMap.containsKey(mathParamFirst)) {
                arguments.add(namedArgumentsConcurrentMap.get(mathParamFirst));
            }
            //Форматируем строку по параметру с индексом с помощью MessageFormat
            String messageFormatterStr = MessageFormat.format(mathParamWithIndex, arguments.toArray());
            //Добавление с заменой полученой строки после форматирования
            paramMatcher.appendReplacement(stringBuffer, messageFormatterStr);
        }
        //Добавить конец строки
        paramMatcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
