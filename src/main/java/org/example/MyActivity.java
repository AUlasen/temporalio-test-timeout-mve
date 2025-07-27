package org.example;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@ActivityInterface
public interface MyActivity {
    static MyActivity getStub() {
        return Workflow.newActivityStub(
                MyActivity.class,
                ActivityOptions.newBuilder().setScheduleToCloseTimeout(Duration.ofSeconds(30)).build());
    }

    void executeActivity();
}
