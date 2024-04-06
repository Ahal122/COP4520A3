import java.util.Random;

class Gift {
    int identifier;
    Gift nextGift;

    Gift(int identifier) {
        this.identifier = identifier;
    }
}

class MinotaurHelper implements Runnable {
    private static Gift firstGift;
    private static final Object sharedLock = new Object();
    private final Random randomizer = new Random();
    private static int chainedGiftCount = 0;
    private static int thankedGiftCount = 0;
    private final int helperId;
    
    MinotaurHelper(int helperId) {
        this.helperId = helperId;
    }

    @Override
    public void run() {
        while (thankedGiftCount < 500000) {
            int selectedTask = randomizer.nextInt(3);
            switch (selectedTask) {
                case 0:
                    if (chainedGiftCount < 500000) {
                        enqueueGift(chainedGiftCount + 1);
                    } else {
                        System.out.println("Helper " + helperId + ": No more gifts to chain.");
                    }
                    break;
                case 1:
                    processThankYouCard();
                    break;
                case 2:
                    int giftIdToFind = randomizer.nextInt(500000) + 1;
                    locateGift(giftIdToFind);
                    break;
            }
        }
    }

    private void enqueueGift(int identifier) {
        synchronized (sharedLock) {
            Gift newGift = new Gift(identifier);
            if (firstGift == null || firstGift.identifier > identifier) {
                newGift.nextGift = firstGift;
                firstGift = newGift;
            } else {
                Gift current = firstGift;
                while (current.nextGift != null && current.nextGift.identifier < identifier) {
                    current = current.nextGift;
                }
                newGift.nextGift = current.nextGift;
                current.nextGift = newGift;
            }
            chainedGiftCount++;
            System.out.println("Helper " + helperId + ": Gift #" + identifier + " chained.");
        }
    }

    private void processThankYouCard() {
        synchronized (sharedLock) {
            if (firstGift == null) {
                System.out.println("Helper " + helperId + ": No gifts left for thank you cards.");
                return;
            }
            int identifier = firstGift.identifier;
            firstGift = firstGift.nextGift;
            thankedGiftCount++;
            System.out.println("Helper " + helperId + ": Wrote thank you card for gift #" + identifier + ".");
        }
    }

    private void locateGift(int identifier) {
        synchronized (sharedLock) {
            Gift current = firstGift;
            while (current != null) {
                if (current.identifier == identifier) {
                    System.out.println("Helper " + helperId + ": Gift #" + identifier + " found.");
                    return;
                }
                current = current.nextGift;
            }
            System.out.println("Helper " + helperId + ": Gift #" + identifier + " not found.");
        }
    }
}

public class MinotaurBirthdayParty {
    public static void main(String[] args) throws InterruptedException {
        MinotaurHelper[] helpers = new MinotaurHelper[4];
        Thread[] helperThreads = new Thread[4];

        for (int i = 0; i < 4; i++) {
            helpers[i] = new MinotaurHelper(i + 1);
            helperThreads[i] = new Thread(helpers[i]);
            helperThreads[i].start();
        }

        for (Thread thread : helperThreads) {
            thread.join();
        }

        System.out.println("Thank you cards completed for all gifts.");
    }
}
