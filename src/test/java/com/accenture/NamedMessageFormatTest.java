package com.accenture;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тестирование класса NamedMessageFormat {@link NamedMessageFormat}
 */
@RunWith(JUnitPlatform.class)
public class NamedMessageFormatTest {

    private String messageFormatPattern;
    private String namedMessageFormatPattern;
    private String namedMessageFormatPatternCorrupt;
    private Object[] messageFormatParam;
    private Map<String, Object> namedMessageFormatParam;
    private MessageFormat messageFormat;
    private NamedMessageFormat namedMessageFormat;

    @BeforeEach
    public void setUp() throws Exception {
        Date date = new Date();
        messageFormatPattern = "Test Date: {0, time, dd.MM.yyyy}. Test Integer: {2, number, integer}. " +
                "Test Percent: {1, number, percent}";
        namedMessageFormatPattern = "Test Date: {myDate, time, dd.MM.yyyy}. Test Integer: " +
                "{simpleInt, number, integer}. Test Percent: {numberPercent, number, percent}";

        namedMessageFormatPatternCorrupt = "Test Date: {!myDate, time, dd.MM.yyyy}. Test Integer: " +
                "{213simpleInt, number, integer}. Test Percent: {..numberPercent, number, percent}";

        //Параметры для MessageFormat
        messageFormatParam = new Object[]{date, 1, 100};

        //Параметры для NamedMessageFormat
        namedMessageFormatParam = new HashMap<>();
        namedMessageFormatParam.put("myDate", date);
        namedMessageFormatParam.put("numberPercent", 1);
        namedMessageFormatParam.put("simpleInt", 100);

        //Формируем MessageFormat по паттерну с индексами
        messageFormat = new MessageFormat(messageFormatPattern);

        //Формируем NamedMessageFormat по паттерну с именами
        namedMessageFormat = new NamedMessageFormat(namedMessageFormatPattern);
    }

    /**
     * Тестиирование метод format на корректность
     */
    @Test
    public void format() throws NamedMessageFormatException {
        //Замена параметров в строке на значения
        String messageFormatStr = messageFormat.format(messageFormatParam);
        String namedMessageFormatStr = namedMessageFormat.format(namedMessageFormatParam);
        //Сравнение результатов вывода MessageFormat и NamedMessageFormat
        assertEquals(messageFormatStr, namedMessageFormatStr);

        //Проверка на ожидаемое исключение, в случае некорректного параметра имени
        namedMessageFormat.setPattern(namedMessageFormatPatternCorrupt);
        assertThrows(NamedMessageFormatException.class, () -> {
            namedMessageFormat.format(namedMessageFormatParam);
        });
    }

    /**
     * Тестирование Потоко-безопасность метода format
     * @throws InterruptedException Если какой либо поток прервал текущий
     */
    @Test
    public void formatThreadSafe() throws InterruptedException, ExecutionException {
        int threadsSize = 10;
        ExecutorService service = Executors.newFixedThreadPool(threadsSize);
        Collection<Future<String>> futures = new ArrayList<>(threadsSize);

        //Выполняем метод format для всех потоков
        for (int t = 0; t < threadsSize; t++) {
            futures.add(service.submit(() -> namedMessageFormat.format(namedMessageFormatParam)));
        }
        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        //Суммируем успешно выполненные задачи потоков
        int successfulThreads = 0;
        for (Future<String> f : futures) {
            if(f.isDone()) successfulThreads++;
        }
        //Сравнение количества потоков с успешно выполненными
        assertEquals(threadsSize, successfulThreads);
    }
}