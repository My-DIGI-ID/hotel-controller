package com.ibm.ssi.controller.hotel.service.exceptions;

public class CannotFindMyHotelException extends Exception{

    public CannotFindMyHotelException() {
        super("User Hotel was not found.");
    }
}
