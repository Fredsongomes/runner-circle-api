package br.com.alura.runnercircleapi.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(Long id) {
        super("usuário " + id + " não encontrado");
    }
}
