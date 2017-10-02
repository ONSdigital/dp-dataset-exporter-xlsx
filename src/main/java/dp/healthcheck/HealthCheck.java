package dp.healthcheck;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @RequestMapping("/healthcheck")
    HttpStatus healthCheck() {
       return HttpStatus.OK;
    }

}
