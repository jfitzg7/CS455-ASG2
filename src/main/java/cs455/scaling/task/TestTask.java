package cs455.scaling.task;

public class TestTask implements Task {

    private final int id;

    public TestTask(int id) {
        this.id = id;
    }

    @Override
    public void executeTask() {
        System.out.println("TestTask #" + this.id);
    }
}
