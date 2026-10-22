package grupo2.catalogodeproductos_tpi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * MySQL en un contenedor para los tests que levantan la aplicación.
 * Lo importa TestDeIntegracion, la clase base de esos tests.
 *
 * @ServiceConnection hace que el DataSource use la URL, el usuario y la
 * contraseña del contenedor en lugar de los de application.properties.
 *
 * Spring arranca el contenedor al crear el bean y lo apaga al cerrar el
 * contexto. Como el contexto se reusa entre clases de test con la misma
 * configuración, todas comparten el mismo contenedor.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer mysqlContainer() {
        return new MySQLContainer(DockerImageName.parse("mysql:8.4"));
    }
}
