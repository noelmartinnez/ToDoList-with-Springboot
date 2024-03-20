package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    /*
    * En el test no hay que añadir la anotación @Transactional
    * porque queremos probar el uso de los métodos de servicio en un contexto similar al que usaremos cuando los llamemos desde el controller.
    *
    * Cuando llamemos desde el controller a los métodos de servicio no se usará la anotación @Transactional
    * para evitar en el código del controller se pueda acceder a los objetos repository y modificar directamente la base de datos.
    * */
    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }

    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void excepcionesCrearEquipo() {
        equipoService.crearEquipo("Proyecto");

        assertThatThrownBy(() -> equipoService.crearEquipo(""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo("Proyecto"))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void abandonarEquipo() {
        // GIVEN
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN
        equipoService.abandonarEquipo(equipo.getId(), usuario.getId());

        // THEN
        List<UsuarioData> usuariosEnEquipo = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuariosEnEquipo).isEmpty();
    }

    @Test
    public void cambiarNombreEquipo() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.cambiarNombreEquipo(equipo.getId(), "Nuevo Proyecto");

        // WHEN
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        assertThat(equipos).hasSize(1);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Nuevo Proyecto");
    }

    @Test
    public void excepcionesCambiarNombreEquipo() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.crearEquipo("Nuevo Proyecto");

        // THEN
        // Verificar la excepción cuando el equipo no existe en la base de datos
        assertThatThrownBy(() -> equipoService.cambiarNombreEquipo(100L, "Nombre Cambiado"))
                .isInstanceOf(EquipoServiceException.class);

        // Verificar la excepción cuando se intenta cambiar a un nombre que ya existe en otro equipo
        assertThatThrownBy(() -> equipoService.cambiarNombreEquipo(equipo.getId(), "Nuevo Proyecto"))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarEquipo() {
        // GIVEN
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        equipoService.eliminarEquipo(equipo.getId());

        // WHEN
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        assertThat(equipos).hasSize(0);
    }
}
