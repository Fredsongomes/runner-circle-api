package br.com.alura.runnercircleapi.mapper;

import br.com.alura.runnercircleapi.dto.AutorResponseDTO;
import br.com.alura.runnercircleapi.dto.UserAdminResponseDTO;
import br.com.alura.runnercircleapi.dto.UserResponseDTO;
import br.com.alura.runnercircleapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AutorResponseDTO toAutorDTO(User autor) {
        if (autor == null) {
            return null;
        }
        return new AutorResponseDTO(
                autor.getNome(),
                autor.getUsername(),
                autor.getAvatarUrl()
        );
    }

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNome(),
                user.getUsername(),
                user.getEmail(),
                user.getBio()
        );
    }

    public UserAdminResponseDTO toAdminResponseDTO(User user) {
        return new UserAdminResponseDTO(
                user.getId(),
                user.getNome(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
