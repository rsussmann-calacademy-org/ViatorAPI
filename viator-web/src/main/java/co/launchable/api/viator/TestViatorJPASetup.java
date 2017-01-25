package co.launchable.api.viator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
public class TestViatorJPASetup {

    public static void main(String[] args) {
        System.out.println("************** BEGINNING PROGRAM **************");

        //ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        ApplicationContext context = new FileSystemXmlApplicationContext("C:\\Projects\\ASViatorIntegration\\src\\main\\webapp\\WEB-INF\\mvc-dispatcher-servlet.xml");

        ServiceBooking serviceBooking = (ServiceBooking) context.getBean("serviceBooking");

        Booking booking = new Booking();
        booking.setBookingReference("1234567890");

        BookingAction bookingAction = new BookingAction();
        bookingAction.setBooking(booking);
        bookingAction.setConfirmation("confirmation-code");
        bookingAction.setStatus("transaction-status");
        booking.addAction(bookingAction);
        booking.addToComments("test comment, please save");

        serviceBooking.addBooking(booking);
        System.out.println("Booking : " + booking + " added successfully");

        Booking bookingSaved = serviceBooking.getBookingByBookingReference("1234567890");
        List<Booking> bookings = serviceBooking.fetchAllBookings();
        System.out.println("The list of all bookings = " + bookings);

        System.out.println("************** ENDING PROGRAM *****************");
    }
}
