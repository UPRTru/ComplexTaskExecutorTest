import java.util.Random;
import java.util.concurrent.*;

public class ComplexTaskExecutor {
    private final CyclicBarrier cyclicBarrier;

    public ComplexTaskExecutor(int taskCount) {
        this.cyclicBarrier = new CyclicBarrier(taskCount);
    }

    public class ComplexTask implements Runnable {
        @Override
        public void run() {
            System.out.println(task());
            try {
                cyclicBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        private Long task() {
            Long result = 1L;
            Random random = new Random();
            for(int i = 0; i < 1000000; i++) {
                int r = random.nextInt(555);
                for (int j = 0; j < r; j++) {
                    result += 1;
                }
            }
            return result;
        }
    }

    public void executeTasks(int numberOfTasks) {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfTasks);
        for (int i = 0; i < numberOfTasks; i++) {
            executorService.submit(new ComplexTask());
        }
        try {
            cyclicBarrier.await();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        ComplexTaskExecutor taskExecutor = new ComplexTaskExecutor(5);

        Runnable testRunnable = () -> {
            System.out.println(Thread.currentThread().getName() + " started the test.");
            taskExecutor.executeTasks(5);
            System.out.println(Thread.currentThread().getName() + " completed the test.");
        };

        Thread thread1 = new Thread(testRunnable, "TestThread-1");
        Thread thread2 = new Thread(testRunnable, "TestThread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
