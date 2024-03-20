package madstodolist.repository;

import madstodolist.model.Equipo;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoTest {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        equipo.setDescripcion("esto es una descripcion");
        assertThat(equipo.getNombre()).isEqualTo("Proyecto P1");
        assertThat(equipo.getDescripcion()).isEqualTo("esto es una descripcion");
    }

    @Test
    @Transactional
    public void grabarYBuscarEquipo() {
        // GIVEN
        // Un equipo nuevo
        // Equipo equipo = new Equipo("Proyecto P1");

        // Probamos el constructor vacío, necesario para que funcione JPA/Hibernate
        Equipo equipo = new Equipo();

        // Creamos ya el equipo nuevo
        equipo = new Equipo("Proyecto P1");
        equipo.setDescripcion("esto es una descripcion");

        // WHEN
        // Salvamos el equipo en la base de datos
        equipoRepository.save(equipo);

        // THEN
        // Su identificador se ha actualizado y lo podemos
        // usar para recuperarlo de la base de datos
        Long equipoId = equipo.getId();
        assertThat(equipoId).isNotNull();
        Equipo equipoDB = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getNombre()).isEqualTo("Proyecto P1");
        assertThat(equipoDB.getDescripcion()).isEqualTo("esto es una descripcion");
    }

    @Test
    public void comprobarIgualdadEquipos() {
        // GIVEN
        // Creamos tres equipos sin id, sólo con el nombre
        Equipo equipo1 = new Equipo("Proyecto P1");
        Equipo equipo2 = new Equipo("Proyecto P2");
        Equipo equipo3 = new Equipo("Proyecto P2");

        // THEN
        // Comprobamos igualdad basada en el atributo nombre y que el
        // hashCode es el mismo para dos equipos con igual nombre
        assertThat(equipo1).isNotEqualTo(equipo2);
        assertThat(equipo2).isEqualTo(equipo3);
        assertThat(equipo2.hashCode()).isEqualTo(equipo3.hashCode());

        // WHEN
        // Añadimos identificadores y comprobamos igualdad por identificadores
        equipo1.setId(1L);
        equipo2.setId(1L);
        equipo3.setId(2L);

        // THEN
        // Comprobamos igualdad basada en el atributo nombre
        assertThat(equipo1).isEqualTo(equipo2);
        assertThat(equipo2).isNotEqualTo(equipo3);
    }

    @Test
    @Transactional
    public void comprobarRelacionBaseDatos() {
        // GIVEN
        // Un equipo y un usuario en la BD
        Equipo equipo = new Equipo("Proyecto 1");
        equipoRepository.save(equipo);

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        // WHEN
        // Añadimos el usuario al equipo

        equipo.addUsuario(usuario);

        // THEN
        // La relación entre usuario y equipo pqueda actualizada en BD

        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);

        assertThat(equipo.getUsuarios()).hasSize(1);
        assertThat(equipo.getUsuarios()).contains(usuario);
        assertThat(usuario.getEquipos()).hasSize(1);
        assertThat(usuario.getEquipos()).contains(equipo);
    }

    @Test
    @Transactional
    public void comprobarFindAll() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoRepository.save(new Equipo("Proyecto 2"));
        equipoRepository.save(new Equipo("Proyecto 3"));

        // WHEN
        List<Equipo> equipos = equipoRepository.findAll();

        // THEN
        assertThat(equipos).hasSize(2);
    }

    @Test
    @Transactional
    public void comprobarAddTareas() {
        // GIVEN
        Equipo equipo = new Equipo("Proyecto 1");
        equipoRepository.save(equipo);

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Tarea 1");
        tareaRepository.save(tarea1);
        Tarea tarea2 = new Tarea(usuario, "Tarea 2");
        tareaRepository.save(tarea2);

        usuario.addTarea(tarea1);
        usuario.addTarea(tarea2);

        equipo.addUsuario(usuario);
        equipo.setAdminUsuario(usuario);

        equipo.addTarea(tarea1);
        equipo.addTarea(tarea2);
        // WHEN
        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);

        // THEN
        assert equipoBD != null;
        assertThat(equipoBD.getTareas()).hasSize(2);
    }

    public void comprobarRemoveTareas() {
        // GIVEN
        Equipo equipo = new Equipo("Proyecto 1");
        equipoRepository.save(equipo);

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "Tarea 1");
        tareaRepository.save(tarea1);
        Tarea tarea2 = new Tarea(usuario, "Tarea 2");
        tareaRepository.save(tarea2);

        usuario.addTarea(tarea1);
        usuario.addTarea(tarea2);

        equipo.addUsuario(usuario);
        equipo.setAdminUsuario(usuario);

        equipo.addTarea(tarea1);
        equipo.addTarea(tarea2);
        equipo.removeTarea(tarea1);
        // WHEN
        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);

        // THEN
        assert equipoBD != null;
        assertThat(equipoBD.getTareas()).hasSize(1);
    }
}
