package br.com.alura.runnercircleapi.exception;

public class ComentarioNotFoundException extends RuntimeException {

    public ComentarioNotFoundException(Long id) {
        super("comentário " + id + " não encontrado");
    }
}
