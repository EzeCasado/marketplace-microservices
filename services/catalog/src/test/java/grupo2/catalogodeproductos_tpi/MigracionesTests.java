package grupo2.catalogodeproductos_tpi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Las migraciones de db/migration sobre el MySQL vacío del contenedor (ver
 * TestDeIntegracion). Flyway las corre al arrancar la app y después Hibernate
 * valida las entidades contra lo que quedó: si no coinciden, la app no arranca
 * y fallan todos los tests, no solo estos.
 */
class MigracionesTests extends TestDeIntegracion {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void laBaseQuedaEnLaUltimaMigracion() {
        MigrationInfo actual = flyway.info().current();

        // Sube con cada migración nueva.
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void noQuedanMigracionesPendientesNiFallidas() {
        MigrationInfoService info = flyway.info();

        assertThat(info.pending()).isEmpty();
        assertThat(info.all()).noneMatch(migracion -> migracion.getState().isFailed());
    }

    /**
     * Hibernate (ddl-auto=validate) comprueba que existan las columnas y que su tipo
     * coincida, pero no mira si aceptan null, si tienen un valor por defecto ni si
     * sobra alguna. Esas cosas de la V2 se revisan acá.
     */
    @Test
    void laTablaDeProductosQuedaConLasColumnasDeLaV2() {
        Map<String, Map<String, Object>> columnas = jdbcTemplate.queryForList("""
                select column_name as nombre, is_nullable as acepta_null, column_default as valor_por_defecto,
                       numeric_precision as digitos, numeric_scale as decimales
                from information_schema.columns
                where table_schema = database() and table_name = 'productos'
                """).stream()
                .collect(Collectors.toMap(columna -> (String) columna.get("nombre"), Function.identity()));

        // `dimensiones` sale; `tipo` y `volumen` entran.
        assertThat(columnas).doesNotContainKey("dimensiones").containsKeys("tipo", "volumen", "peso");

        // El FISICO por defecto era solo para que el ADD COLUMN no fallara: el alta tiene que mandar el tipo.
        assertThat(columnas.get("tipo")).containsEntry("acepta_null", "NO").containsEntry("valor_por_defecto", null);

        // Un DIGITAL no lleva peso ni volumen: las dos columnas aceptan null.
        assertThat(columnas.get("peso")).containsEntry("acepta_null", "YES");
        assertThat(columnas.get("volumen")).containsEntry("acepta_null", "YES");
        // decimal(10,3) en kg y decimal(10,6) en m3 (1 cm3): lo que fijan @Digits en el alta.
        assertThat(digitosYDecimales(columnas.get("peso"))).containsExactly(10, 3);
        assertThat(digitosYDecimales(columnas.get("volumen"))).containsExactly(10, 6);
    }

    private static List<Integer> digitosYDecimales(Map<String, Object> columna) {
        return List.of(((Number) columna.get("digitos")).intValue(), ((Number) columna.get("decimales")).intValue());
    }
}
