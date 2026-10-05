package br.com.alura.runnercircleapi.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("email ou senha inválidos");
    }
}
