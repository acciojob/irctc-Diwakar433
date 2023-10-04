package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //getting train
        Optional<Train> optionalTrain = trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!optionalTrain.isPresent())
            return 0;

        Train train = optionalTrain.get();

        // find the no. of booked seat
        int bookedSeats = 0;
        List<Ticket> booked = train.getBookedTickets();

        for(Ticket ticket:booked){
            bookedSeats += ticket.getPassengersList().size();
        }
        // check for exception less ticket are available
        if(bookedSeats + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        String stations[] = train.getRoute().split(",");

        List<Passenger> passengerList = new ArrayList<>();
        List<Integer> passengerIds = bookTicketEntryDto.getPassengerIds();

        for(int id : passengerIds){
            passengerList.add(passengerRepository.findById(id).get());
        }
        int x = -1,y = -1;
        for(int i = 0; i < stations.length; i++){
            if(bookTicketEntryDto.getFromStation().toString().equals(stations[i])){
                x = i;
                break;
            }
        }
        for(int i=0;i<stations.length;i++){
            if(bookTicketEntryDto.getToStation().toString().equals(stations[i])){
                y = i;
                break;
            }
        }
        // check for invalid station
        if(x == -1 || y == -1 || (y - x) < 0){
            throw new Exception("Invalid stations");
        }
        // total fair
        int fair = 0;
        fair = bookTicketEntryDto.getNoOfSeats()*(y - x)*300;

        // set the data to ticket
        Ticket ticket=new Ticket();

        ticket.setPassengersList(passengerList); // list of passenger
        ticket.setFromStation(bookTicketEntryDto.getFromStation()); // from station
        ticket.setToStation(bookTicketEntryDto.getToStation()); // to station
        ticket.setTotalFare(fair); // total fair
        ticket.setTrain(train); // train

        train.getBookedTickets().add(ticket); // add ticket to list of Ticket
        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats()); // after booking available seats

        // update passenger relation part which is Ticket
        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);

        // save train
        trainRepository.save(train);

        // save ticket
        return ticketRepository.save(ticket).getTicketId();
    }
}
