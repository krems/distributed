package com.ovchinnikov.reactor;


public class RuntimeInterruptedException extends RuntimeException {
    public RuntimeInterruptedException(final InterruptedException e) {
        super("Interrupted", e, true, false);
    }
}
