package com.sneha.airbnbAppC.service;

import com.sneha.airbnbAppC.entity.Booking;

public interface CheckOutService {

    String getCheckOutSession(Booking booking,String successUrl, String failureUrl );
}
