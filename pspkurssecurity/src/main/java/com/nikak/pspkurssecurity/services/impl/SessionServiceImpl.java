package com.nikak.pspkurssecurity.services.impl;

import com.nikak.pspkurssecurity.dto.SessionRequest;
import com.nikak.pspkurssecurity.entities.*;
import com.nikak.pspkurssecurity.repositories.*;
import com.nikak.pspkurssecurity.services.SessionService;
import com.nikak.pspkurssecurity.services.SpecialistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SpecialistRepository specialistRepository;

    private final FavorRepository favorRepository;

    private final SessionRepository sessionRepository;
    private final TimeRepository timeRepository;
private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    public String addNewSession(SessionRequest sessionRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("no such user"));
        Client client = clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("no such client"));

        Specialist specialist = specialistRepository.findById(sessionRequest.getSpecialistId())
                .orElseThrow(() -> new IllegalStateException(
                        "specialist with id " + sessionRequest.getSpecialistId() + " does not exist"
                ));

        Favor favor = favorRepository.findById(sessionRequest.getFavorId())
                .orElseThrow(() -> new IllegalStateException(
                        "favor with id " + sessionRequest.getFavorId() + " does not exist"
                ));

        Time time= timeRepository.findById(sessionRequest.getTimeId())
                .orElseThrow(() -> new IllegalStateException(
                        "time with id " + sessionRequest.getTimeId() + " does not exist"
                ));



        LocalDate sessionDate = LocalDate.parse(sessionRequest.getSessionDate().toString());
        LocalDate currentDate = LocalDate.now();
        if (sessionDate.isBefore(currentDate)) {
            throw new IllegalStateException("session date cannot be earlier than the current date");
        }

        boolean sessionIsPresent = sessionRepository.findSessionBySessionDateAndSessionTime(sessionRequest.getSpecialistId(),sessionRequest.getSessionDate(),sessionRequest.getTimeId()).isPresent();
        if(sessionIsPresent){
            throw new  IllegalStateException("session already taken!!!!!!");
        }
        Session sessionToSave = new Session();
        sessionToSave.setSpecialist(specialist);
        sessionToSave.setClient(client);
        sessionToSave.setFavor(favor);
        sessionToSave.setPayment(sessionRequest.getPayment());
        sessionToSave.setSessionDate(sessionRequest.getSessionDate());
        sessionToSave.setTime(time);
        System.out.println(sessionToSave);
        Session savedData;
        savedData = sessionRepository.save(sessionToSave);

        if(savedData!=null) return "session added successfully";
        return null;
    }



    public Optional<Session> findSessionById(Long id) {
        return sessionRepository.findSessionById(id);
    }
    public Optional<List<Session>> findSessionsBySpecialistId(Long specialistId) {
        return sessionRepository.findSessionsBySpecialistId(specialistId);
    }

    public Optional<List<Time>> findFreeTime(Date date, Long specialistId) {
        if(timeRepository.findFreeTime(date,specialistId).get().isEmpty()){
           System.out.println(timeRepository.getAllTime().get());
            return timeRepository.getAllTime();
       }else {
            return timeRepository.findFreeTime(date,specialistId);
        }
   }



    public String deleteSession(Long id) throws IOException{
        Session session = sessionRepository.findById(id)
                .orElseThrow(()->
                        new IllegalStateException("session does not exist")
                );
        sessionRepository.deleteById(id);
        return "session "+ session.getId()+ " deleted successfully";
    }


    public List<Session> getSessions() {
        return sessionRepository.findAllSessions();
    }
}
