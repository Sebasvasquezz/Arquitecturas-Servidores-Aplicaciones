package edu.escuelaing.arep;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import edu.escuelaing.arep.annotations.GetMapping;
import edu.escuelaing.arep.annotations.RequestParam;
import edu.escuelaing.arep.annotations.RestController;

/**
 * The HelloService class provides a RESTful service that returns a greeting message.
 * It is annotated with @RestController to indicate that it is a controller class that
 * handles HTTP requests.
 */
@RestController
public class HelloService {

    /**
     * Handles HTTP GET requests to the /app/hello endpoint. This method returns a greeting
     * message that includes the name provided as a query parameter. If no name is provided,
     * the method defaults to using "Mundo".
     *
     * @param name the name to include in the greeting message. If not provided, "Mundo" is used as the default value.
     * @return a string that contains the greeting message, "Hola, " followed by the provided name.
     */
    @GetMapping("/app/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "Mundo") String name) {
        return "Hola, " + name;
    }
}
