import java.util.concurrent.TimeUnit;

interface Calculator {
    long calculateSum(int n, long N);
}

class FormulaCalculator implements Calculator {
    public long calculateSum(int n, long N) {
        return (N * (N + 1) / 2) * n;
    }
}

class SingleThreadCalculator implements Calculator {
    public long calculateSum(int n, long N) {
        long sum = 0;
        for (int i = 1; i <= N; i++) {
            sum += n * i;
        }
        return sum;
    }
}

class ParallelCalculator implements Calculator {
    private int threads;

    public ParallelCalculator(int threads) {
        this.threads = threads;
    }

    public long calculateSum(int n, long N) {
        final long[] sum = {0};
        Thread[] threadPool = new Thread[threads];
        for (int k = 0; k < threads; k++) {
            final int start = k;
            threadPool[k] = new Thread(new Runnable() {
                @Override
                public void run() {
                    long localSum = 0;
                    for (long i = start + 1; i <= N; i += threads) {
                        localSum += n * i;
                    }
                    synchronized (sum) {
                        sum[0] += localSum;
                    }
                }
            });
            threadPool[k].start();
        }
        try {
            for (Thread thread : threadPool) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sum[0];
    }
}


public class Main {
    public static void main(String[] args) {
        int n = 1;
        long N = 100_000_000;

        Calculator formulaCalculator = new FormulaCalculator();
        calculateAndPrint("Formula", formulaCalculator, n, N);

        Calculator singleThreadCalculator = new SingleThreadCalculator();
        calculateAndPrint("Single thread", singleThreadCalculator, n, N);

        for (int threads : new int[]{2, 4, 8, 16, 32}) {
            Calculator parallelCalculator = new ParallelCalculator(threads);
            calculateAndPrint("Parallel with " + threads + " threads", parallelCalculator, n, N);
        }
    }

    private static void calculateAndPrint(String method, Calculator calculator, int n, long N) {
        long startTime = System.nanoTime();
        long result = calculator.calculateSum(n, N);
        long endTime = System.nanoTime();
        long time = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        System.out.println(method + " result: " + result);
        System.out.println(method + " time: " + time + "ms");
    }
}
