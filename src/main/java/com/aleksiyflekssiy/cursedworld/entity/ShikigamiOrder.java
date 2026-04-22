package com.aleksiyflekssiy.cursedworld.entity;

public enum ShikigamiOrder {
    NONE("None"),
    ATTACK("Attack"),
    GRAB("Grab"),
    PULL("Pull"),
    SWING("Swing"),
    IMMOBILIZE("Immobilize"),
    CATCH("Catch"),
    SMASH("Smash"),
    THROW("Throw"),
    SURROUND("Surround"),
    PUSH("Push"),
    MOVE("Move");

    private final String order;

    ShikigamiOrder(String order){
        this.order = order;
    }

    public String getOrder(){
        return order;
    }
}
