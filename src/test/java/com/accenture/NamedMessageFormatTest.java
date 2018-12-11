package com.accenture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Тестирование класса NamedMessageFormat {@link NamedMessageFormat}
 */
@RunWith(JUnit4.class)
public class NamedMessageFormatTest {

    private String messageFormatPattern;
    private String namedMessageFormatPattern;
    private Object[] messageFormatParam;
    private Map<String, Object> namedMessageFormatParam;
    private MessageFormat messageFormat;
    private NamedMessageFormat namedMessageFormat;

    @Before
    public void setUp() throws Exception {
        Date date = new Date();
        messageFormatPattern = "Test Date: {0, time, dd.MM.yyyy}. Test Integer: {2, number, integer}. " +
                "Test Percent: {1, number, percent}";
        namedMessageFormatPattern = "Test Date: {myDate, time, dd.MM.yyyy}. Test Integer: " +
                "{simpleInt, number, integer}. Test Percent: {numberPercent, number, percent}";

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
    public void format() {
        //Замена параметров в строке на значения
        String messageFormatStr = messageFormat.format(messageFormatParam);
        String namedMessageFormatStr = null;
        try {
            namedMessageFormatStr = namedMessageFormat.format(namedMessageFormatParam);
        } catch (NamedMessageFormatException e) {
            e.printStackTrace();
        }
        //Сравнение результатов вывода MessageFormat и NamedMessageFormat
        Assert.assertEquals(messageFormatStr, namedMessageFormatStr);
    }

    /**
     * Тестирование Потоко-безопасность метода format
     * @throws InterruptedException Если какой либо поток прервал текущий
     */
    @Test
    public void formatThreadSafe() throws InterruptedException {
        int threadsSize = 10;
        ExecutorService service = Executors.newFixedThreadPool(threadsSize);
        Collection<Future<String>> futures = new ArrayList<>(threadsSize);

        //Выполняем метод format для всех потоков
        for (int t = 0; t < threadsSize; ++t) {
            futures.add(service.submit(() -> namedMessageFormat.format(namedMessageFormatParam)));
        }
        Thread.sleep(1000);
        //Суммируем успешно выполненные задачи потоков
        int successfulThreads = 0;
        for (Future<String> f : futures) {
            if(f.isDone()) successfulThreads++;
        }
        //Сравнение количества потоков с успешно выполненными
        Assert.assertEquals(threadsSize, successfulThreads);
    }
}