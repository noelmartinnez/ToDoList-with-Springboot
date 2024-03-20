package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD, USUARIO_BLOQUEADO}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else if (usuario.get().isBloqueado()) {
            return LoginStatus.USUARIO_BLOQUEADO; // AÑADIDO PARA INDICAR QUE EL USUARIO ESTÁ BLOQUEADO
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        else if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else {
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
            usuarioNuevo.setBloqueado(usuario.isBloqueado()); // AÑADIDO PARA PODER CREAR AL USUARIO CON EL ATRIBUTO "BLOQUEADO"
            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    // @Transactional(readOnly = true) significa que en este método no hay ninguna operación en
    // la base de datos que la modifique, solo son operaciones de lectura.
    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    // Método que busca un Usuario en una lista de Usuarios pasado por parámetro y un id concreto a buscar
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorId(List<Usuario> usuarios, Long idBuscado) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId().equals(idBuscado)) {
                return usuario; // Devuelve el usuario si se encuentra
            }
        }
        return null; // Devuelve null si no se encuentra el usuario
    }

    // Método que actualiza el atributo "bloqueado" de un usuario concreto.
    @Transactional
    public UsuarioData actualizarUsuario(UsuarioData usuarioData) {
        // Verifica si el usuario existe en la base de datos
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuarioData.getId());

        if (!usuarioExistente.isPresent()) {
            throw new UsuarioServiceException("El usuario con ID " + usuarioData.getId() + " no existe en la base de datos");
        }

        // Actualiza los campos relevantes del usuario existente con los datos proporcionados en usuarioData
        Usuario usuarioActualizado = usuarioExistente.get();
        usuarioActualizado.setBloqueado(usuarioData.isBloqueado());

        // Guarda los cambios en la base de datos
        usuarioActualizado = usuarioRepository.save(usuarioActualizado);

        // Devuelve el usuario actualizado en forma de UsuarioData
        return modelMapper.map(usuarioActualizado, UsuarioData.class);
    }


    // Método que devuelve el listado completo de objetos Usuario que hay en la base de datos.
    @Transactional(readOnly = true)
    public List<Usuario> listadoCompleto(){
        return (List<Usuario>) usuarioRepository.findAll();
    }
}


