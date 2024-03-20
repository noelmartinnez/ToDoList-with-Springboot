package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.repository.TareaRepository;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.dto.TareaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;


@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ModelMapper modelMapper;

    /* Se anotan con @Transactional actualizar correctamente la base de datos
       y las conexiones lazy y para garantizar la transaccionalidad. */
    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        Tarea tarea = new Tarea(usuario, tituloTarea);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    // Este método muestra cómo listar todas las tareas de un usuario específico.
    // Se obtiene el usuario con el método findById y, si no se encuentra, se lanza una excepción.
    // Si el usuario existe, se transforma la lista de tareas del usuario en una lista de DTOs TareaData.
    // Finalmente, la lista se ordena por el id de la tarea.
    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        logger.debug("Devolviendo todas las tareas del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        List<TareaData> tareas = usuario.getTareas().stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
        // Ordenamos la lista por id de tarea
        Collections.sort(tareas, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
        return tareas;
    }

    // El método findById se encarga de buscar una tarea específica en la base de datos utilizando su tareaId.
    // Si la tarea no se encuentra, devuelve null.
    // En caso contrario, utiliza ModelMapper para convertir la entidad Tarea a su representación DTO TareaData.
    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }
}
