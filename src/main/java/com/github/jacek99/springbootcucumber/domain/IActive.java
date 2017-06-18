package com.github.jacek99.springbootcucumber.domain;

/**
 * Standard entity interface to determine if it has an "active" flag or not.
 * If implements the flag, then the DAO delete will automatically just set the active flag to false
 * instead of actually deleting it from the DB
 *
 * @author Jacek Furmankiewicz
 */
public interface IActive {

    /**
     * Entity active flag
     */
    boolean isActive();
}
