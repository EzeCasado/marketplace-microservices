package grupo2.catalogodeproductos_tpi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Un producto FISICO necesita peso y volumen (PesoYVolumenSegunTipo en docs/api/catalog.yaml).
 *
 * Va sobre la clase y marca el error en el campo que falta (peso o volumen), no en la clase entera.
 * Así sale como FieldError, con el nombre de esta anotación como código, y el manejador de
 * excepciones lo suma a los campos que faltan (422). Si el tipo es null no hace nada: ya lo
 * avisa el @NotNull de tipo.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FisicoConPesoYVolumenValidator.class)
public @interface FisicoConPesoYVolumen {

    String message() default "es obligatorio en un producto FISICO";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
