package main.java.com.lubover.singularity.api;

public interface Allocator {

    boolean Allocate(Actor actor, Slot slot);
}
