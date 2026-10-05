package br.com.alura.runnercircleapi.exception;

public class UsuarioJaExisteException extends RuntimeException {

    public UsuarioJaExisteException(String mensagem) {
        super(mensagem);
    }
}
