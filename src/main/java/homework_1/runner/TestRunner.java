package main.java.homework_1.runner;

import main.java.homework_1.annotations.AfterSuite;
import main.java.homework_1.annotations.BeforeSuite;
import main.java.homework_1.annotations.CsvSource;
import main.java.homework_1.annotations.Test;
import main.java.homework_1.annotations.support.CsvSourceData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

public class TestRunner {

    private static final List<String> STATIC_ANNOTATIONS = Stream.of(BeforeSuite.class, AfterSuite.class)
            .map(Class::getSimpleName)
            .toList();
    private static final int MAX_STATIC_ANNO_COUNT = 1;
    // Для удобства. Если ошибок будет несколько, то мы не будем ругаться на них по одной, а сформируем список и выведем сразу все
    private static final Set<String> ERRORS = new HashSet<>();
    // Мапа будет отсортирована в порядке возрастания ключей. Здесь есть целый простор для того, как можно реализовать задачу
    private static final NavigableMap<Integer, List<String>> TEST_METHODS_WITH_PRIORITY = new TreeMap<>(); // Ключ - приоритет, значение - список методов

    private static int beforeSuiteCount = 0;
    private static int afterSuiteCount = 0;
    private static String beforeSuiteName = "";
    private static String afterSuiteName = "";
    private static String csvMethod = "";
    private static CsvSourceData csvSourceData = null;

    public static void main(String[] args) {
        resetCounts(); // Сбрасываем счётчики. Это сделано для того, чтобы, например, мы могли модифицировать программу, передавая в runTests список классов
        runTests(TestRunner.class); // <<< Подставить сюда тестируемый класс
    }

    /**
     * Основной метод, в котором происходит вся работа.
     * На вход передаётся объект типа Class. Затем реализуется 2 этапа:
     * 1. Валидация всех методов/аннотаций на соответствие требованиям по количеству, статический/нестатический.
     * На этом шаге формируется "план выполнения" теста - список методов в соответствии с их приоритетом.
     * 2. Если ошибок после валидации нет, то происходит непосредственно сам запуск тестов. В случае ошибок, выбрасывается
     * исключение, в котором все найденные ошибки собраны в список.
     *
     * @param clazz тестируемый объект типа Class
     */
    public static void runTests(Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Передаваемый класс не может быть null");
        }
        // 1. Валидируем методы/аннотации, составляем план выполнения методов
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) { // Идём по всем методам
            Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            // У каждого метода анализируем аннотации. Формируем названия методов для последующего запуска.
            // Делаем всё в один проход
            for (Annotation annotation : declaredAnnotations) {
                validateAnnotation(method, annotation);
            }
        }
        if (!ERRORS.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", ERRORS));
        }
        // 2. Если ошибок нет, запускаем тесты согласно сформированному плану
        runTestsWithPriority(clazz);
    }

    private static void runTestsWithPriority(Class clazz) {
        // Сначала вызываем статический BeforeSuite, если он есть
        runMethodIfExists(beforeSuiteName, clazz, null);

        // Вызываем обычные методы в обратном порядке, так как приоритет 10 - самый высокий и должен быть вызван первым
        TEST_METHODS_WITH_PRIORITY.descendingMap()// Эта операция сортирует по ключу в обратном порядке
                .values()
                .forEach(methodList -> methodList.forEach(
                        testMethod -> runMethodIfExists(testMethod, clazz, null)));
        // * Не стал тут сильно заморачиваться с аргументами. И что их может быть несколько. А ещё перегруженные версии и т.п.
        if (csvSourceData != null) {
            Object[] args = new Object[4];
            args[0] = csvSourceData.getA();
            args[1] = csvSourceData.getB();
            args[2] = csvSourceData.getC();
            args[3] = csvSourceData.getD();
            runMethodIfExists(csvMethod, clazz, args);
        }

        // В конце вызываем статический AfterSuite, если он есть
        runMethodIfExists(afterSuiteName, clazz, null);
    }

    private static <T> void runMethodIfExists(String methodName, Class clazz, Object... args) {
        if (methodName == null || methodName.isEmpty()) {
            return;
        }
        System.out.println("Пытаюсь вызвать method = " + methodName);
        try {
            Constructor constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            // ВАЖНО! Не получится вызвать нестатический метод без экземпляра класса.
            // В свою очередь невозможно создать экземпляр класса без дженериков, не зная заранее какой класс будет передан.
            // В любом случае newInstance() возвращает T
            T instance = (T) constructor.newInstance();
            Method method;
            if (args != null) {
                method = clazz.getDeclaredMethod(methodName, int.class, String.class, int.class, boolean.class);
                method.setAccessible(true);
                method.invoke(instance, args);
            } else {
                method = clazz.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(instance);
            }
        } catch (NoSuchMethodException e) {
            System.out.printf("Невозможно! Метода [%s] не существует! Мы же только что проверяли!", methodName);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Невозможно вызвать метод %s с указанными аргументами", methodName), e);
        } catch (InstantiationException e) {
            throw new RuntimeException(String.format("Невозможно создать объект переданного класса, класс = %s", clazz.getSimpleName()));
        }
    }

    private static void validateAnnotation(Method method, Annotation annotation) {
        boolean isStaticAnnotation = false;
        // Запоминаем статические методы "ДО" и "ПОСЛЕ" всего
        if (annotation instanceof BeforeSuite) {
            beforeSuiteCount++;
            beforeSuiteName = method.getName();
            isStaticAnnotation = true;
        } else if (annotation instanceof AfterSuite) {
            afterSuiteCount++;
            afterSuiteName = method.getName();
            isStaticAnnotation = true;
        }
        // Статические и нестатические методы и аннотации должны совпадать
        boolean isStaticMethod = Modifier.isStatic(method.getModifiers());
        boolean methodAndAnnotationMatches = isStaticMethod == isStaticAnnotation;
        if (!methodAndAnnotationMatches) {
            ERRORS.add(String.format("Аннотации %s должны применяться " +
                            "только со статическими методами, остальные - только с нестатическими. Метод = %s",
                    STATIC_ANNOTATIONS, method.getName()));
        }
        // Статических методов не должно быть больше 1 каждого типа
        if (beforeSuiteCount > MAX_STATIC_ANNO_COUNT || afterSuiteCount > MAX_STATIC_ANNO_COUNT) {
            ERRORS.add(String.format("Методов со статическими аннотациями %s не должно быть " +
                    "больше одного для каждого типа", STATIC_ANNOTATIONS));
        }
        if (annotation instanceof Test) {
            int priority = ((Test) annotation).priority();
            if (priority < 1 || priority > 10) {
                ERRORS.add("Значение @Test.priority должно лежать в диапазоне [1..10]");
            }
            addTestMethod(method, priority);
        }
        if (annotation instanceof CsvSource) {
            // Я уже не стал тут заморачиваться сильно
            String params = ((CsvSource) annotation).params();
            // int a, String b, int c, boolean d
            String[] parsedParams = params.split(",");
            csvSourceData = new CsvSourceData(
                    Integer.parseInt(parsedParams[0].trim()), // a
                    parsedParams[1].trim(), // b
                    Integer.parseInt(parsedParams[2].trim()), // c
                    Boolean.parseBoolean(parsedParams[3]) // d
            );
            csvMethod = method.getName();
        }
    }

    private static void addTestMethod(Method method, int priority) {
        if (!TEST_METHODS_WITH_PRIORITY.containsKey(priority)) {
            TEST_METHODS_WITH_PRIORITY.put(priority, new ArrayList<>()); // Если ещё нет методов с таким приоритетом, то создаём список
        }
        TEST_METHODS_WITH_PRIORITY.get(priority).add(method.getName()); // Добавляем метод в соответствии с приоритетом
    }

    // Сброс всех счётчиков, списков, ошибок
    private static void resetCounts() {
        System.out.println("---> ResetCounts");
        ERRORS.clear();
        TEST_METHODS_WITH_PRIORITY.clear();
        afterSuiteCount = 0;
        beforeSuiteCount = 0;
        afterSuiteName = "";
        beforeSuiteName = "";
        csvMethod = "";
        csvSourceData = null;
    }

    // ===================== НИЖЕ ИДУТ МЕТОДЫ ДЛЯ ЗАПУСКА ПРИМЕРА =========================

    @BeforeSuite
    private static void beforeAll() {
        System.out.println("---------------> beforeAll");
    }

    @AfterSuite
    public static void afterAll() {
        System.out.println("---------------> afterAll");
    }

    @Test(priority = 1)
    public void test_1() {
        System.out.println("---> test_1");
    }

    @Test
    public void test_2() {
        System.out.println("---> test_2");
    }

    @Test(priority = 10)
    public void test_3() {
        System.out.println("---> test_3");
    }

    @Test(priority = 10)
    public void test_5() {
        System.out.println("---> test_5");
    }

    @Test(priority = 7)
    public void test_4() {
        System.out.println("---> test_4");
    }

    @CsvSource(params = "10, Java, 20, true")
    public void testMethod(int a, String b, int c, boolean d) {
        System.out.println("---> testMethod Csv");
        System.out.printf("a = %s, b = %s, c = %s, d = %s%n", a, b, c, d);
    }

}
