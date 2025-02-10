package main.java.homework_3;

public class PoolTestDrive {

    public static void main(String[] args) throws InterruptedException {

        CustomThreadPool pool = new CustomThreadPool(4);
        // Создание и запуск демонстрационных задач
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            Runnable task = () -> {
                try {
                    Thread.sleep(500L); // Задержка для наглядности работы
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Task №" + finalI + " completed");
            };
            pool.execute(task);
        }

        pool.awaitTermination();
        pool.shutdown();

//        Runnable taskImpossible = () -> System.out.println("Sorry I'm late! Please run me!"); // Задача после закрытия пула
//        pool.execute(taskImpossible); // Бросает исключение
    }

}