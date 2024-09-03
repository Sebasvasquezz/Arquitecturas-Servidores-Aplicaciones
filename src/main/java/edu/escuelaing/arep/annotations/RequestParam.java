package edu.escuelaing.arep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @RequestParam annotation is used to bind a method parameter to a web request parameter.
 * It indicates that a method parameter should be bound to a query parameter from the HTTP request.
 * This annotation is applicable to method parameters.
 *
 * <p>Attributes:</p>
 * <ul>
 *   <li><strong>value:</strong> Specifies the name of the request parameter to bind to.</li>
 *   <li><strong>defaultValue:</strong> Specifies a default value to use as a fallback when the request parameter is not provided. The default is an empty string.</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@literal @}GetMapping("/greet")
 * public String greet(@RequestParam(value = "name", defaultValue = "World") String name) {
 *     return "Hello, " + name;
 * }
 * </pre>
 *
 * <p>This annotation is retained at runtime, allowing the server to dynamically bind request parameters to method parameters during the execution of the program.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    String value();
    String defaultValue() default "";
}
