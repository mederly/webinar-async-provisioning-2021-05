package dorm;

public class TestListener {

    public static void main(String[] args) throws InterruptedException {
        QueueListener.start(System.out::println);
        Thread.sleep(60000);
    }
}
