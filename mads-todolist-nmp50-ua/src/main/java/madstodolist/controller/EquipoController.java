package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/equipos")
    public String listaEquipos(Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "equipos";
    }

    @GetMapping("/equipos/{equipoId}/usuarios")
    public String listaUsuariosEnEquipo(@PathVariable(value="equipoId") Long equipoId, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipoId);
        model.addAttribute("usuarios", usuarios);

        return "usuariosEnEquipo";
    }

    @PostMapping("/equipos/crear")
    public String crearEquipo(@RequestParam("nombreEquipo") String nombreEquipo, Model model, HttpSession session) {
        Long id = managerUserSession.usuarioLogeado();

        if (id == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(id);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        // Validación de nombre de equipo no vacío
        if (nombreEquipo == null || nombreEquipo.trim().isEmpty()) {
            model.addAttribute("errorCrearEquipo", "El nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
            return "equipos";
        }

        try {
            // Intenta crear el equipo
            equipoService.crearEquipo(nombreEquipo);
        } catch (EquipoServiceException e) {
            model.addAttribute("errorCrearEquipo", e.getMessage());
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);

        return "equipos";
    }

    @PostMapping("/equipos/{equipoId}/unirse")
    public String unirseAEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
            model.addAttribute("errorUnirse", "Ya perteneces a este equipo.");
        } else {
            equipoService.añadirUsuarioAEquipo(equipoId, idUsuario);
            model.addAttribute("correcto", "Te has unido al equipo.");
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @PostMapping("/equipos/{equipoId}/abandonar")
    public String abandonarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (usuario != null) {
            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);
        }

        if (equipoService.usuariosEquipo(equipoId).contains(usuario)) {
            equipoService.abandonarEquipo(equipoId, idUsuario);
            model.addAttribute("correctoAbandono", "Has abandonado el equipo.");
        } else {
            model.addAttribute("errorAbandonar", "No perteneces a este equipo.");
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @GetMapping("/equipos/{equipoId}/administrar")
    public String administrarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        // Verificar si el usuario es administrador antes de permitir la administración del equipo
        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/cambiarNombre")
    public String cambiarNombreEquipo(@PathVariable(value = "equipoId") Long equipoId, @RequestParam("nuevoNombre") String nuevoNombre, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            model.addAttribute("errorCambiarNombre", "El nuevo nombre del equipo no puede estar vacío o compuesto por espacios en blanco.");
        } else {
            try {
                equipoService.cambiarNombreEquipo(equipoId, nuevoNombre);
                model.addAttribute("correctoCambioNombre", "El nombre del equipo se ha cambiado con éxito.");
            } catch (EquipoServiceException e) {
                model.addAttribute("errorCambiarNombre", e.getMessage());
            }
        }

        EquipoData equipo = equipoService.recuperarEquipo(equipoId);
        model.addAttribute("equipo", equipo);

        return "administracionEquipo";
    }

    @PostMapping("/equipos/{equipoId}/eliminar")
    public String eliminarEquipo(@PathVariable(value = "equipoId") Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);

        if (!usuario.isAdmin()) {
            throw new UsuarioNoAutorizadoException();
        }

        try {
            equipoService.eliminarEquipo(equipoId);

            boolean esAdmin = usuario.isAdmin();
            model.addAttribute("esAdmin", esAdmin);

            List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
            model.addAttribute("equipos", equipos);

            return "redirect:/equipos";

        } catch (EquipoServiceException e) {
            model.addAttribute("errorEliminacion", e.getMessage());
            return "administracionEquipo";
        }
    }
}
