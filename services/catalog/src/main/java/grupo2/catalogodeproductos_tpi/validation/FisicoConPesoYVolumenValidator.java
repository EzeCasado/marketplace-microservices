package grupo2.catalogodeproductos_tpi.validation;

import grupo2.catalogodeproductos_tpi.dto.CreateProductoDTO;
import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FisicoConPesoYVolumenValidator implements ConstraintValidator<FisicoConPesoYVolumen, CreateProductoDTO> {

    @Override
    public boolean isValid(CreateProductoDTO producto, ConstraintValidatorContext context) {
        if (producto == null || producto.tipo() != TipoProducto.FISICO) {
            return true;
        }

        boolean faltaElPeso = producto.peso() == null;
        boolean faltaElVolumen = producto.volumen() == null;

        if (faltaElPeso || faltaElVolumen) {
            // Se reemplaza el error de la clase por uno en cada campo que falta.
            context.disableDefaultConstraintViolation();
            if (faltaElPeso) {
                marcar(context, "peso");
            }
            if (faltaElVolumen) {
                marcar(context, "volumen");
            }
        }
        return !faltaElPeso && !faltaElVolumen;
    }

    private static void marcar(ConstraintValidatorContext context, String campo) {
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(campo)
                .addConstraintViolation();
    }
}
