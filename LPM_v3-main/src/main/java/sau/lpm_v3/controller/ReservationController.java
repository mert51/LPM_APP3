package sau.lpm_v3.controller;

import sau.lpm_v3.dtos.ReservationDTO;
import sau.lpm_v3.service.PlaceService;
import sau.lpm_v3.service.ReservationService;
import sau.lpm_v3.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import sau.lpm_v3.exception.ErrorResponse;

import java.util.List;

@Controller
@RequestMapping("/reservation")
public class ReservationController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;
    private final StudentService studentService;
    private final PlaceService placeService;

    public ReservationController(ReservationService reservationService, StudentService studentService, PlaceService placeService) {
        this.reservationService = reservationService;
        this.studentService = studentService;
        this.placeService = placeService;
    }

    @GetMapping("/all")
    public String getAllReservations(Model model) {
        List<ReservationDTO> reservationDtos = reservationService.getAllReservations();
        model.addAttribute("reservations", reservationDtos);
        return "reservations/all";
    }

    @GetMapping("/{id}")
    public String getReservation(@PathVariable Long id, Model model) {
        model.addAttribute("reservation", reservationService.getReservationById(id));
        return "reservations/_show";
    }

    @GetMapping(value = "/add")
    public String addReservation(Model model) {
        model.addAttribute("reservation", new ReservationDTO());
        model.addAttribute("student", studentService.getAllStudents());
        model.addAttribute("place", placeService.getAllPlaces());
        return "reservations/_add";
    }

    @PostMapping(value = "add")
    public String addReservation(@ModelAttribute ReservationDTO reservationDto,
                                 @RequestParam(required = false) Long studentId,
                                 @RequestParam Long placeId,
                                 Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Admin ise formdan gönderilen studentId kullanılır, değilse servis işlem sırasında mevcut kullanıcıya ait öğrenci atanır
        if (isAdmin && studentId != null) {
            reservationDto.setStudentDto(studentService.getStudentById(studentId));
        }
        reservationDto.setPlaceDto(placeService.getPlaceById(placeId));

        // Yeni kayıtta müsaitlik durumunu otomatik false (rezerve edildi) yapıyoruz
        reservationDto.setReserved(true);

        logger.info("A new Reservation that ID is [{}] ADDED.", reservationDto.getId());

        // Servis yetki kontrolü ile birlikte çağrılıyor
        reservationService.createReservation(reservationDto, isAdmin, auth.getName());
        return "redirect:/reservation/all";
    }

    @GetMapping("/update/{id}")
    public String updateReservation(@PathVariable Long id, Model model) {
        // Already getReservationById converts DTO
        model.addAttribute("reservation", reservationService.getReservationById(id));
        model.addAttribute("student", studentService.getAllStudents());
        model.addAttribute("place", placeService.getAllPlaces());
        return "reservations/_update";
    }

    @PostMapping("/update/{id}")
    public String updateReservation(@PathVariable Long id,
                                    @ModelAttribute ReservationDTO reservationDto,
                                    @RequestParam(required = false) Long studentId,
                                    @RequestParam Long placeId,
                                    Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Logs when update an entity
        // It is going to add USER DETAILS who performed to action
        logger.info("Reservation that ID is [{}] UPDATED", reservationDto.getId());

        // Admin ise öğrenci değişikliği kabul edilir, değilse servis mevcut sahibini korur
        if (isAdmin && studentId != null) {
            reservationDto.setStudentDto(studentService.getStudentById(studentId));
        }
        reservationDto.setPlaceDto(placeService.getPlaceById(placeId));
        reservationDto.setId(id); // ID'yi URL'den güvenli şekilde alıp atıyoruz
        reservationService.updateReservation(id, reservationDto, isAdmin, auth.getName());
        return "redirect:/reservation/all";
    }

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReservation(@PathVariable Long id, Authentication auth) {
        // Logs when delete an entity
        // It is going to add USER DETAILS who performed to action
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        logger.warn("Reservation that ID is [{}] DELETED", id);

        // Prefer to let GlobalExceptionHandler handle AccessDeniedException, but add a local fallback
        // to ensure AJAX callers receive the X-Redirect-URL header when an AccessDeniedException occurs.
        try {
            reservationService.deleteReservation(id, isAdmin, auth.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (AccessDeniedException ex) {
            // AJAX clients will read this header and redirect accordingly; non-AJAX will be redirected by controller advice.
            ErrorResponse body = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Redirect-URL", "/reservation-forbidden-delete")
                    .body(body);
        }
    }
}