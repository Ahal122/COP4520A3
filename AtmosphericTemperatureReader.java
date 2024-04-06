import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TempModule {
    private final PriorityQueue<Integer> highestTemps = new PriorityQueue<>(10, Comparator.reverseOrder());
    private final PriorityQueue<Integer> lowestTemps = new PriorityQueue<>(10);
    private final Lock reLock = new ReentrantLock();

    public void insertTemperature(int temp) {
        reLock.lock();
        try {
            System.out.println("Inserting temperature value: " + temp);
            highestTemps.add(temp);
            lowestTemps.add(temp);
            if (highestTemps.size() > 5) highestTemps.poll();
            if (lowestTemps.size() > 5) lowestTemps.poll();
        } finally {
            reLock.unlock();
        }
    }

    public void displayTemperatureStats() {
        reLock.lock();
        try {
            System.out.println("Highest 5 Temperature Readings: " + highestTemps);
            System.out.println("Lowest 5 Temperature Readings: " + lowestTemps);
        } finally {
            reLock.unlock();
        }
    }
}

public class AtmosphericTemperatureReader {
    public static void main(String[] args) {
        TempModule tempModule = new TempModule();

        Thread[] temperatureSensors = new Thread[8];
        for (int i = 0; i < temperatureSensors.length; i++) {
            temperatureSensors[i] = new Thread(() -> {
                for (int j = 0; j < 60; j++) {
                    int readTemperature = -100 + (int) (Math.random() * 171);
                    tempModule.insertTemperature(readTemperature);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted temperature sensor thread: " + e.getMessage());
                    }
                }
            });
            temperatureSensors[i].start();
        }

        for (Thread sensor : temperatureSensors) {
            try {
                sensor.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted main control thread: " + e.getMessage());
            }
        }

        tempModule.displayTemperatureStats();
    }
}
