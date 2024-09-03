package edu.escuelaing.arep;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import edu.escuelaing.arep.annotations.GetMapping;
import edu.escuelaing.arep.annotations.RequestParam;
import edu.escuelaing.arep.annotations.RestController;

@RestController
public class HelloService {

    @GetMapping("/app/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "Mundo") String name) {
        return "Hola, " + name;
    }
}
