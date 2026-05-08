package sau.lpm_v3.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        log.warn("404 Not Found triggered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(value = ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleStudentAlreadyExistsException(Exception ex){
        log.warn("StudentAlreadyExists triggered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

        String uri = request.getRequestURI() == null ? "" : request.getRequestURI();
        String redirectUrl = "/403";
        if (uri.contains("/reservation/delete")) {
            redirectUrl = "/reservation-forbidden-delete";
        } else if (uri.contains("/reservation/update") || uri.contains("/reservation/add")) {
            redirectUrl = "/reservation-forbidden-edit";
        }

        if (isAjax) {
            // For AJAX callers, include the friendly redirect URL in a header and return JSON body
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Redirect-URL", redirectUrl)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
        } else {
            return "redirect:" + redirectUrl;
        }
    }
}
