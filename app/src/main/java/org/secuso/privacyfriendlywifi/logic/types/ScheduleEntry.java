package org.secuso.privacyfriendlywifi.logic.types;

import org.secuso.privacyfriendlywifi.logic.preconditions.ScheduleCondition;

import java.io.Serializable;

/**
 *
 */
public class ScheduleEntry extends PreconditionEntry implements Serializable {
    private String title;
    private ScheduleCondition scheduleCondition;

    public ScheduleEntry(String title, int start_hour, int start_minute, int end_hour, int end_minute) {
        this.title = title;
        this.scheduleCondition = new ScheduleCondition(start_hour, start_minute, end_hour, end_minute);
        this.scheduleCondition.addObserver(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.notifyChanged();
    }

    public ScheduleCondition getScheduleCondition() {
        return scheduleCondition;
    }

    public void setScheduleCondition(ScheduleCondition scheduleCondition) {
        this.scheduleCondition = scheduleCondition;
        this.notifyChanged();
    }

    @Override
    public int compareTo(PreconditionEntry another) {
        ScheduleCondition otherSchedCond;
        ScheduleCondition ownSchedCond = this.getScheduleCondition();

        if (another instanceof ScheduleEntry) {
            otherSchedCond = ((ScheduleEntry) another).getScheduleCondition();
        } else {
            return -1;
        }

        return ownSchedCond.getStartHour() < otherSchedCond.getStartHour() ? -1 : 1;
    }

    @Override
    public String toString() {
        ScheduleCondition schedCond = this.getScheduleCondition();
        return "Start: " + schedCond.getStartHour() + ":" + schedCond.getStartMinute() +
                "; End: " + schedCond.getEndHour() + ":" + schedCond.getEndMinute();
    }
}