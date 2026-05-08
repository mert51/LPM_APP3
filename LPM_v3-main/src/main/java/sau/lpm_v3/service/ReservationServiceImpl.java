package sau.lpm_v3.service;

import sau.lpm_v3.dtos.ReservationDTO;
import sau.lpm_v3.exception.ErrorMessages;
import sau.lpm_v3.exception.ResourceAlreadyExistsException;
import sau.lpm_v3.exception.ResourceNotFoundException;
import sau.lpm_v3.model.*;
import sau.lpm_v3.repository.PlaceRepository;
import sau.lpm_v3.repository.ReservationRepository;
import sau.lpm_v3.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final StudentRepository studentRepository;
    private final PlaceRepository placeRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, StudentRepository studentRepository, PlaceRepository placeRepository) {
        this.reservationRepository = reservationRepository;
        this.studentRepository = studentRepository;
        this.placeRepository = placeRepository;
    }

    @Override
    public ReservationDTO getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ERROR_RESERVATION_NOT_FOUND + ": " + id)).viewAsReservationDTO();
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream().map(Reservation::viewAsReservationDTO).toList();
    }

    @Override
    public ReservationDTO createReservation(ReservationDTO reservationDto, boolean isAdmin, String currentUsername) {
        // Convert DTO to Entity internally
        Reservation reservation = reservationDto.toEntity();

        // Resolve student: admin can set any student, user can only create for self
        Student student = null;
        if (isAdmin && reservationDto.getStudentDto() != null && reservationDto.getStudentDto().getId() > 0) {
            student = studentRepository.findById(reservationDto.getStudentDto().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + reservationDto.getStudentDto().getId()));
        } else {
            student = studentRepository.findByUsername(currentUsername);
            if (student == null) {
                throw new ResourceNotFoundException("Current student not found: " + currentUsername);
            }
        }

        Place place = placeRepository.findById(reservationDto.getPlaceDto().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Place not found: " + reservationDto.getPlaceDto().getId()));

        reservation.setStudent(student);
        reservation.setPlace(place);

        return reservationRepository.save(reservation).viewAsReservationDTO();
    }

    @Override
    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDto, boolean isAdmin, String currentUsername) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorMessages.ERROR_RESERVATION_NOT_FOUND + ": " + id));

        // Authorization: only admin or owner can update
        if (!isAdmin && !existing.getStudent().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Yetkisiz işlem! Sadece kendi rezervasyonunuzu güncelleyebilirsiniz.");
        }

        Reservation reservation = reservationDto.toEntity();
        reservation.setId(id); // URL'den gelen güvenli ID

        // If admin, allow changing student; otherwise keep existing owner
        Student student;
        if (isAdmin && reservationDto.getStudentDto() != null && reservationDto.getStudentDto().getId() > 0) {
            student = studentRepository.findById(reservationDto.getStudentDto().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + reservationDto.getStudentDto().getId()));
        } else {
            student = existing.getStudent();
        }
        Place place = placeRepository.findById(reservationDto.getPlaceDto().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Place not found: " + reservationDto.getPlaceDto().getId()));

        reservation.setStudent(student);
        reservation.setPlace(place);

        return reservationRepository.save(reservation).viewAsReservationDTO();
    }

    @Override
    public void deleteReservation(Long id, boolean isAdmin, String currentUsername) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ERROR_RESERVATION_NOT_FOUND + ": " + id));

        if (!isAdmin && !existing.getStudent().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("Yetkisiz işlem! Sadece kendi rezervasyonunuzu silebilirsiniz.");
        }

        reservationRepository.deleteById(id);
    }
}