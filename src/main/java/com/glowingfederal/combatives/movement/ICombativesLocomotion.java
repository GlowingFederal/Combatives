package com.glowingfederal.combatives.movement;

public interface ICombativesLocomotion {
    LocomotionState getLocomotionState();
    void setLocomotionState(LocomotionState state);
    int getSlideTicks();
    void setSlideTicks(int ticks);
    float getLean();
    void setLean(float lean);
}
