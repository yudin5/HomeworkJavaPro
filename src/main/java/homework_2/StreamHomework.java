package main.java.homework_2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamHomework {

    public static void main(String[] args) {

        // Реализуйте удаление из листа всех дубликатов
        List<Integer> listWithDuplicates = List.of(1, 2, 3, 1, 2, 3, 4, 4, 4);
        List<Integer> listWithoutDuplicates = listWithDuplicates.stream()
                .distinct()
                .toList();
        System.out.println("listWithoutDuplicates = " + listWithoutDuplicates);

        // Найдите в списке целых чисел 3-е наибольшее число (пример: 5 2 10 9 4 3 10 1 13 => 10)
        List<Integer> numbers = List.of(5, 2, 10, 9, 4, 3, 10, 1, 13);
        Integer thirdMax = numbers.stream()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .toList()
                .getFirst();
        System.out.println("thirdMax = " + thirdMax);

        // Найдите в списке целых чисел 3-е наибольшее «уникальное» число (пример: 5 2 10 9 4 3 10 1 13 => 9,
        // в отличие от прошлой задачи здесь разные 10 считает за одно число)
        Integer thirdMaxUnique = numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(2)
                .toList()
                .getFirst();
        System.out.println("thirdMaxUnique = " + thirdMaxUnique);

        // Имеется список объектов типа Сотрудник (имя, возраст, должность), необходимо получить список
        // имен 3 самых старших сотрудников с должностью «Инженер», в порядке убывания возраста
        List<Employee> employeeList = List.of(
                new Employee("Марина", 37, Employee.Title.MANAGER),
                new Employee("Владислав", 53, Employee.Title.DIRECTOR),
                new Employee("Татьяна", 40, Employee.Title.MANAGER),
                new Employee("Григорий", 35, Employee.Title.ENGINEER),
                new Employee("Илья", 59, Employee.Title.ENGINEER),
                new Employee("Андрей", 44, Employee.Title.ENGINEER),
                new Employee("Анастасия", 38, Employee.Title.ENGINEER),
                new Employee("Иван", 25, Employee.Title.ENGINEER));
        List<String> oldestEngineersNames = employeeList.stream()
                .filter(employee -> employee.title() == Employee.Title.ENGINEER)
                .sorted(Comparator.comparingInt(Employee::age).reversed())
                .limit(3)
                .map(Employee::name)
                .toList();
        System.out.println("oldestEngineersNames = " + oldestEngineersNames);

        // Имеется список объектов типа Сотрудник (имя, возраст, должность),
        // посчитайте средний возраст сотрудников с должностью «Инженер»
        double averageAge = employeeList.stream()
                .filter(employee -> employee.title() == Employee.Title.ENGINEER)
                .mapToInt(Employee::age)
                .average()
                .orElseThrow(() -> new RuntimeException("Не смогли посчитать средний возраст. Проверьте исходные данные"));
        System.out.println("averageAge = " + averageAge);

        // Найдите в списке слов самое длинное
        List<String> words = List.of("a", "hm", "cat", "bb", "java", "compiler", "fun");
        String longestWord = words.stream()
                .max(Comparator.comparingInt(String::length))
                .orElse("Самое длинное слово не найдено");
        System.out.println("longestWord = " + longestWord);

        // Имеется строка с набором слов в нижнем регистре, разделенных пробелом. Постройте хеш-мапы,
        // в которой будут храниться пары: слово - сколько раз оно встречается во входной строке
        String stringWithWords = "cat dog bird dog flat map hash hash cat fly cat whole world hello cat";
        Map<String, Integer> wordsCountMap = Arrays.stream(stringWithWords.split(" "))
                .collect(Collectors.toMap(key -> key, value -> 1, Integer::sum));
        System.out.println("wordsCountMap = " + wordsCountMap);

        // Отпечатайте в консоль строки из списка в порядке увеличения длины слова, если слова имеют
        // одинаковую длину, то должен быть сохранен алфавитный порядок
        List<String> someWords = List.of("cat", "bee", "dog", "house", "flat", "hi", "joke", "jupiter", "world");
        someWords.stream()
                .sorted(Comparator.comparingInt(String::length)
                        .thenComparing(Comparator.naturalOrder()))
                .forEach(System.out::println);

        // Имеется массив строк, в каждой из которых лежит набор из 5 строк, разделенных пробелом,
        // найдите среди всех слов самое длинное, если таких слов несколько, получите любое из них
        List<String> stringsWithWords = List.of(
                "cat dog flat map hash",
                "hi may unit epsilon drum",
                "book huge magazine town city",
                "unit final volatile hummer craft",
                "strange start tiger lamp door");

        String maxLengthWord = stringsWithWords.stream()
                .flatMap(string -> Arrays.stream(string.split(" ")))
                .max(Comparator.comparingInt(String::length))
                .orElse("Не найдено самое длинное слово");
        System.out.println("maxLengthWord = " + maxLengthWord);

    }

}