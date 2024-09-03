package edu.escuelaing.arep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @GetMapping annotation is used to map HTTP GET requests onto specific handler methods.
 * It is applied to methods in a class annotated with @RestController to indicate which method
 * should handle GET requests for a specific URL path.
 *
 * <p>Attributes:</p>
 * <ul>
 *   <li><strong>value:</strong> Specifies the URI path that the method should handle. This path is relative to the base URL of the application.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}GetMapping("/app/hello")
 * public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
 *     return "Hello, " + name;
 * }
 * </pre>
 *
 * <p>This annotation is retained at runtime, allowing the server to map incoming GET requests to the appropriate method during the execution of the program.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetMapping {

    /**
     * Specifies the URI path that the annotated method should handle.
     * 
     * @return the URI path as a string.
     */
    public String value();
}
