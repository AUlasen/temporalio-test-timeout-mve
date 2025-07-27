package org.example;

import io.temporal.activity.Activity;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ScheduleToCloseTimeoutTest {

    private final static String TASK_QUEUE = "testQueue";
    Logger logger = LoggerFactory.getLogger(ScheduleToCloseTimeoutTest.class);

    static TestWorkflowEnvironment testEnv = TestWorkflowEnvironment.newInstance();
    static WorkflowClient client = testEnv.getWorkflowClient();

    @BeforeAll
    public static void setup() {

        Worker worker = testEnv.newWorker(TASK_QUEUE);

        MyActivity activities = new TestActivityImpl();
        worker.registerActivitiesImplementations(activities);

        worker.registerWorkflowImplementationFactory(MyWorkflow.class, () -> new MyWorkflow() {
            @Override
            public void run() {
                var activity = MyActivity.getStub();
                try {
                    activity.executeActivity();
                } catch (Exception e) {
                    //ignore
                }
            }
        });

        // Start test environment
        testEnv.start();
    }

    @RepeatedTest(10000)
    @Timeout(30)
    public void executeTest() {

        // Create the workflow stub
        MyWorkflow workflow =
                client.newWorkflowStub(
                        MyWorkflow.class, WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());

        logger.info("Before run: {}", new Date(testEnv.currentTimeMillis()));
        workflow.run();
        logger.info("After run: {}", new Date(testEnv.currentTimeMillis()));

    }
}

class TestActivityImpl implements MyActivity {
    Logger logger = LoggerFactory.getLogger(ScheduleToCloseTimeoutTest.class);

    @Override
    public void executeActivity() {

        var context = Activity.getExecutionContext();
        var attempt = context.getInfo().getAttempt();
        var scheduled = context.getInfo().getCurrentAttemptScheduledTimestamp();
        logger.info("Attempt {} scheduled at: {}", attempt, new Date(scheduled));

        throw new RuntimeException("Activity failure");
    }
}
