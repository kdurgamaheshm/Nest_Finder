package com.nestfinder.nestfinderbackend.payload.response;

import java.util.List;

import com.nestfinder.nestfinderbackend.model.Booking;
import com.nestfinder.nestfinderbackend.model.House;
import com.nestfinder.nestfinderbackend.model.Payment;

public class UserDashboardResponse {
    private String username;
    private long bookingsCount;
    private long wishlistCount;
    private long messagesCount;
    private List<House> availableHouses;
    private List<Booking> recentBookings;
    private List<Payment> payments;

    public UserDashboardResponse(String username, long bookingsCount, long wishlistCount, long messagesCount,
                                 List<House> availableHouses, List<Booking> recentBookings, List<Payment> payments) {
        this.username = username;
        this.bookingsCount = bookingsCount;
        this.wishlistCount = wishlistCount;
        this.messagesCount = messagesCount;
        this.availableHouses = availableHouses;
        this.recentBookings = recentBookings;
        this.payments = payments;
    }

    public String getUsername() {
        return username;
    }

    public long getBookingsCount() {
        return bookingsCount;
    }

    public long getWishlistCount() {
        return wishlistCount;
    }

    public long getMessagesCount() {
        return messagesCount;
    }

    public List<House> getAvailableHouses() {
        return availableHouses;
    }

    public List<Booking> getRecentBookings() {
        return recentBookings;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
