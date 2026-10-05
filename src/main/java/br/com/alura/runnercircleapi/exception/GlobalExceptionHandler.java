package br.com.alura.runnercircleapi.exception;

import br.com.alura.runnercircleapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> tratarErroDeValidacao(MethodArgumentNotValidException exception,
                                                               HttpServletRequest request) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return responderErroEsperado(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler({
            TreinoNotFoundException.class,
            ComentarioNotFoundException.class,
            UsuarioNaoEncontradoException.class
    })
    public ResponseEntity<ErrorResponse> tratarRecursoNaoEncontrado(RuntimeException exception,
                                                                    HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException exception,
                                                                    HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(UsuarioJaExisteException.class)
    public ResponseEntity<ErrorResponse> tratarUsuarioJaExiste(UsuarioJaExisteException exception,
                                                               HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(ImagemInvalidaException.class)
    public ResponseEntity<ErrorResponse> tratarImagemInvalida(ImagemInvalidaException exception,
                                                              HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> tratarContentTypeNaoSuportado(HttpMediaTypeNotSupportedException exception,
                                                                       HttpServletRequest request) {
        String contentType = exception.getContentType() != null ? exception.getContentType().toString() : "ausente";
        return responderErroEsperado(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type não suportado: " + contentType, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tratarUploadAcimaDoLimite(MaxUploadSizeExceededException exception,
                                                                   HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.BAD_REQUEST, "o arquivo enviado excede o tamanho máximo permitido", request);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErrorResponse> tratarAcessoNegado(AcessoNegadoException exception,
                                                            HttpServletRequest request) {
        return responderErroEsperado(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    // Exceções do Spring Security lançadas dentro do controller (@PreAuthorize, UsuarioAutenticadoService).
    // Relançar devolve a exceção ao Spring Security, que responde 401 (sem autenticação) ou 403 (sem permissão);
    // sem isto, o handler de Exception daria 500.
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public void relancarExcecaoDeSeguranca(RuntimeException exception) {
        throw exception;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> tratarErroInesperado(Exception exception, HttpServletRequest request) {

        HttpStatusCode status = statusDeExcecaoDoFramework(exception);
        if (status != null) {
            String mensagem = status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "erro na requisição";
            return responder(status, mensagem, request);
        }

        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "erro interno no servidor", request);
    }

    private HttpStatusCode statusDeExcecaoDoFramework(Exception exception) {
        if (exception instanceof org.springframework.web.ErrorResponse errorResponse) {
            return errorResponse.getStatusCode();
        }
        if (exception instanceof HttpMessageNotReadableException || exception instanceof TypeMismatchException) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    // Uma linha, sem stack trace: são erros previstos, causados pela requisição do cliente.
    // Só o URI é registrado (sem query string) e a mensagem nunca traz dados do corpo da requisição.
    private ResponseEntity<ErrorResponse> responderErroEsperado(HttpStatusCode status, String mensagem,
                                                                HttpServletRequest request) {
        log.warn("{} {} -> {}: {}", request.getMethod(), request.getRequestURI(), status.value(), mensagem);
        return responder(status, mensagem, request);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatusCode status, String mensagem, HttpServletRequest request) {
        ErrorResponse corpo = new ErrorResponse(LocalDateTime.now(), status.value(), mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
