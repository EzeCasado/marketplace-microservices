package grupo2.catalogodeproductos_tpi.validation;

import grupo2.catalogodeproductos_tpi.dto.CreateProductoDTO;
import grupo2.catalogodeproductos_tpi.model.TipoProducto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DigitalSinPesoNiVolumenValidator implements ConstraintValidator<DigitalSinPesoNiVolumen, CreateProductoDTO> {

    @Override
    public boolean isValid(CreateProductoDTO producto, ConstraintValidatorContext context) {
        if (producto == null || producto.tipo() != TipoProducto.DIGITAL) {
            return true;
        }

        boolean sobraElPeso = producto.peso() != null;
        boolean sobraElVolumen = producto.volumen() != null;

        if (sobraElPeso || sobraElVolumen) {
            // Se reemplaza el error de la clase por uno en cada campo que sobra.
            context.disableDefaultConstraintViolation();
            if (sobraElPeso) {
                marcar(context, "peso");
            }
            if (sobraElVolumen) {
                marcar(context, "volumen");
            }
        }
        return !sobraElPeso && !sobraElVolumen;
    }

    private static void marcar(ConstraintValidatorContext context, String campo) {
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(campo)
                .addConstraintViolation();
    }
}
