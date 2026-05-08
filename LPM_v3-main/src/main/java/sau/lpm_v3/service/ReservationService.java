package sau.lpm_v3.service;

import sau.lpm_v3.dtos.ReservationDTO;
import sau.lpm_v3.model.Reservation;

import java.util.List;

public interface ReservationService {
    public List<ReservationDTO> getAllReservations();
    public ReservationDTO getReservationById(Long id);
    // isAdmin ve currentUsername ile yetki kontrolleri yapılacak
    public ReservationDTO createReservation(ReservationDTO reservationDto, boolean isAdmin, String currentUsername);
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDto, boolean isAdmin, String currentUsername);
    public void deleteReservation(Long id, boolean isAdmin, String currentUsername);
}