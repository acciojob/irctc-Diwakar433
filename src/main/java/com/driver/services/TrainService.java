package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        //create train object and set parameters from trainEntryDto
        Train train = new Train();
        //get station rout from list of train from dto
        List<Station> stationList = trainEntryDto.getStationRoute();
        StringBuilder rout = new StringBuilder();

        for(Station station : stationList)
            rout.append(station).append(",");

        // delete the last comma from rout
        rout.deleteCharAt(rout.length()-1);

        train.setRoute(rout.toString());
        train.setBookedTickets(new ArrayList<>());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available

        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.


        //getting train
        Optional<Train> optionalTrain = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if(!optionalTrain.isPresent())
            return 0;

        Train train = optionalTrain.get();

        //getting booked ticket
        List<Ticket> ticketList = train.getBookedTickets();

        //getting from and to station
        Station fromStation = seatAvailabilityEntryDto.getFromStation();
        Station toStation = seatAvailabilityEntryDto.getToStation();

        //getting the number of seats in the train
        Integer seatInTrain = train.getNoOfSeats();

        //getting the available seats
        int availableSeats = 0;

        String[] route = train.getRoute().split(",");
        Map<String, Integer> stationBookedSeatMap = new HashMap<>();
        for (String s : route) stationBookedSeatMap.put(s, 0);

        for (Ticket t : ticketList) {
            boolean flag = false;
            for (String s : route) {
                if (s.equals("" + t.getFromStation()))
                    flag = true;
                if (s.equals("" + t.getToStation()))
                    break;
                if (flag)
                    stationBookedSeatMap.put(s, (stationBookedSeatMap.get(s) + t.getPassengersList().size()));
            }
        }
        boolean bb = false;
        for (String s : route) {
            if (s.equals("" + fromStation))
                bb = true;
            if (s.equals("" + toStation))
                break;
            if (bb)
                availableSeats += (seatInTrain - stationBookedSeatMap.get(s));
        }

        return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        //getting train
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(!optionalTrain.isPresent())
            return 0;

        Train train = optionalTrain.get();

        //getting booked ticket
        List<Ticket> ticketList = train.getBookedTickets();

        // check station consists in this train rout
        String[] rout = train.getRoute().split(",");
        boolean routAvailable = false;

        for(String s : rout) {
            if(s.equals("" + station))
                routAvailable = true;
        }

        if(!routAvailable)
            throw new Exception("Train is not passing from this station");

        int noOfPeople = 0;

        for(Ticket ticket : ticketList) {
            if(station == ticket.getFromStation())
                noOfPeople += ticket.getPassengersList().size();
        }

        return noOfPeople;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        //getting train
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(!optionalTrain.isPresent())
            return 0;

        Train train = optionalTrain.get();

        //getting booked ticket
        List<Ticket> ticketList = train.getBookedTickets();

        // find the person which is oldest in this train
        int oldestPerson = 0;
        for(Ticket ticket : ticketList) {
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger passenger : passengerList) {
                if(oldestPerson < passenger.getAge())
                    oldestPerson = passenger.getAge();
            }
        }
        return oldestPerson;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milliseconds value will be 0 in a LocalTime format.

        List<Train> trainList = trainRepository.findAll();

        List<Integer> ans = new ArrayList<>();

        for (Train train : trainList) {
            String[] trainRoute = train.getRoute().split(",");
            int cnt = 0;
            for (String s : trainRoute) {
                if (s.equals("" + station)) {
                    LocalTime departureTime = train.getDepartureTime();
                    LocalTime crossingTime = departureTime.plusHours(cnt);
                    if ((crossingTime.isAfter(startTime) && crossingTime.isBefore(endTime)) || crossingTime.equals(startTime) || crossingTime.equals(endTime)) {
                        ans.add(train.getTrainId());
                    }
                }
                cnt++;
            }
        }
        return ans;
    }

}
