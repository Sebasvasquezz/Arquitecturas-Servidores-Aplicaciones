package edu.escuelaing.arep;

import edu.escuelaing.arep.annotations.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The PiService class provides a RESTful service that calculates the value of Pi
 * to a specified number of decimal places. It is annotated with @RestController
 * to indicate that it is a controller class that handles HTTP requests.
 */
@RestController
public class PiService {

    /**
     * Handles HTTP GET requests to the /app/pi endpoint. This method calculates
     * the value of Pi to the number of decimal places specified by the `decimals`
     * query parameter.
     *
     * @param decimals the number of decimal places to include in the calculated value of Pi.
     *                 If not provided, a default value of 2 decimal places is used.
     *                 If the number of decimals is negative, an error message is returned.
     * @return a string representation of Pi calculated to the specified number of decimal places,
     *         or an error message if the number of decimals is negative.
     */
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
