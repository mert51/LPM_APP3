package sau.lpm_v3.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

// @ControllerAdvice  (handled by sau.lpm_v3.exception.GlobalExceptionHandler)
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);
        if (isAjax) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } else {
            // Non-ajax: redirect to /403 page
            return "redirect:/403";
        }
    }
}