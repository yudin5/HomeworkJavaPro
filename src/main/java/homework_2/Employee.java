package main.java.homework_2;

public record Employee(String name, int age, main.java.homework_2.Employee.Title title) {

    public enum Title {
        ENGINEER,
        MANAGER,
        DIRECTOR
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", title=" + title +
                '}';
    }

}