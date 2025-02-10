package main.java.homework_3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CustomThreadPool {

    private volatile boolean isShutdown;
    private final List<Runnable> taskQueue = Collections.synchronizedList(new LinkedList<>()); // Очередь задач
    private final List<Thread> workers; // Список воркеров

    // Конструктор. В момент инициализации создаём воркеров и запускаем пул
    public CustomThreadPool(int workerCount) {
        workers = new ArrayList<>(workerCount);
        for (int i = 0; i < workerCount; i++) {
            // Каждый воркер в бесконечном цикле пытается взять очередную задачу из списка
            // Если очередь пустая, то просто ждём. Задача есть - обрабатываем её
            Runnable work = () -> {
                Runnable nextTask = null;
                while (true) {
                    synchronized (taskQueue) {
                        if (taskQueue.isEmpty()) {
                            if (isShutdown) {
                                break;
                            }
                            try {
                                taskQueue.wait(); // Если очередь пустая, то просто ждём
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            nextTask = taskQueue.removeFirst(); // Задача есть - обрабатываем её
                            taskQueue.notifyAll();
                        }
                    }
                    if (nextTask != null) {
                        System.out.println("Task is performed by " + Thread.currentThread().getName());
                        nextTask.run();
                    }
                }
            };
            Thread worker = new Thread(work, "Worker-" + i);
            workers.add(worker);
        }
        System.out.println("===> Pool is starting...");
        workers.forEach(Thread::start);
    }

    public void execute(Runnable task) {
        if (isShutdown) {
            throw new IllegalStateException("===> Pool is closed. No more tasks can be executed.");
        }
        synchronized (taskQueue) {
            taskQueue.add(task);
            System.out.println("Task was added. Queue size = " + taskQueue.size());
            taskQueue.notifyAll();
        }
    }

    public void awaitTermination() {
        System.out.println("===> Pool is awaiting termination...");
        while (!taskQueue.isEmpty()) {
        }
    }

    public void shutdown() {
        System.out.println("===> Pool is shutting down...");
        isShutdown = true;
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }

}