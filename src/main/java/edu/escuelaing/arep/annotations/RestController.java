package edu.escuelaing.arep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @RestController annotation is used to mark a class as a RESTful controller.
 * Classes annotated with @RestController are recognized as controllers that handle
 * HTTP requests in the context of a web application. This annotation should be
 * applied at the class level.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}RestController
 * public class MyController {
 *     // Define request-handling methods here
 * }
 * </pre>
 *
 * <p>This annotation is retained at runtime, meaning it is accessible via reflection
 * during the execution of the program.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestController {
    
}
