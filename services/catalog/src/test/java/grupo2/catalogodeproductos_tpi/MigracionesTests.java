package grupo2.catalogodeproductos_tpi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Test
    void laBaseQuedaEnLaUltimaMigracion() {
        MigrationInfo actual = flyway.info().current();

        // Sube con cada migración nueva.
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion().getVersion()).isEqualTo("1");
    }

    @Test
    void noQuedanMigracionesPendientesNiFallidas() {
        MigrationInfoService info = flyway.info();

        assertThat(info.pending()).isEmpty();
        assertThat(info.all()).noneMatch(migracion -> migracion.getState().isFailed());
    }
}
