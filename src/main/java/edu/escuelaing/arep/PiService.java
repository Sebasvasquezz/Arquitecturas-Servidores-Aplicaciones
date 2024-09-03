package edu.escuelaing.arep;

import edu.escuelaing.arep.annotations.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
public class PiService {

    @GetMapping("/app/pi")
    public String pi(@RequestParam(value = "decimals", defaultValue = "2") int decimals) {
        if (decimals < 0) {
            return "Error: El número de decimales no puede ser negativo.";
        }

        BigDecimal pi = new BigDecimal(Math.PI);
        pi = pi.setScale(decimals, RoundingMode.HALF_UP);

        return "Pi con " + decimals + " decimales: " + pi.toString();
    }
}
