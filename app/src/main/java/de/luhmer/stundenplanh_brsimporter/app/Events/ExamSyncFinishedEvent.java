package de.luhmer.stundenplanh_brsimporter.app.Events;

/**
 * Created by David on 06.07.2014.
 */
public class ExamSyncFinishedEvent {
    boolean mChangedSomething;
    boolean mSuccessful;

    public boolean isSuccessful() {
       return mSuccessful;
    }

    public ExamSyncFinishedEvent(boolean changedSomething, boolean successful) {
        this.mChangedSomething = changedSomething;
        this.mSuccessful = successful;
    }
}
