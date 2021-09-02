package com.mamunsproject.food_delevery.EventBus;

public class CounterCartEvent {
    private boolean success;

    public CounterCartEvent(boolean success) {
        this.success = success;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
