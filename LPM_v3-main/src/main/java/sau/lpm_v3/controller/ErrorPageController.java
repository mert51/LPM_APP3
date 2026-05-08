package sau.lpm_v3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

    @GetMapping("/reservation-forbidden-delete")
    public String reservationDeleteForbidden() {
        return "reservation-forbidden-delete";
    }

    @GetMapping("/reservation-forbidden-edit")
    public String reservationEditForbidden() {
        return "reservation-forbidden-edit";
    }
}
