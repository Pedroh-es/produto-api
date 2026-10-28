package com.trabalho.api.exceptions;

import com.trabalho.api.logging.AppLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Converte exceções em respostas HTTP e registra ERRO no logger.
 *
 * Herda de ResponseEntityExceptionHandler para que erros do próprio Spring
 * (JSON inválido, id não numérico, método não suportado...) mantenham o
 * status correto (400, 405...) em vez de virarem 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ORIGEM = "GlobalExceptionHandler";
    private final AppLogger log = AppLogger.getInstance();

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        log.erro(ORIGEM, "Respondendo 404 Not Found", ex);
        return corpo(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> erroInesperado(Exception ex) {
        log.erro(ORIGEM, "Erro inesperado, respondendo 500", ex);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor");
    }

    // Erros padrão do Spring MVC passam por aqui: só registra ERRO e mantém o status
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        log.erro(ORIGEM, "Requisição inválida, respondendo " + statusCode.value(), ex);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ResponseEntity<Map<String, Object>> corpo(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "erro", mensagem));
    }
}
