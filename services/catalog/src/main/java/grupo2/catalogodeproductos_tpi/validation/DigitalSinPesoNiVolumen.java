package grupo2.catalogodeproductos_tpi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Un producto DIGITAL no lleva peso ni volumen (PesoYVolumenSegunTipo en docs/api/catalog.yaml).
 *
 * Va sobre la clase y marca el error en el campo que sobra (peso o volumen), no en la clase entera.
 * Sale como un 400 con "campo: mensaje". Si el tipo es null no hace nada: ya lo avisa el @NotNull de tipo.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DigitalSinPesoNiVolumenValidator.class)
public @interface DigitalSinPesoNiVolumen {

    String message() default "no va en un producto DIGITAL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
