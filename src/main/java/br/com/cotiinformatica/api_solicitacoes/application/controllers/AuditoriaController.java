package br.com.cotiinformatica.api_solicitacoes.application.controllers;

import br.com.cotiinformatica.api_solicitacoes.domain.interfaces.AuditoriaService;
import br.com.cotiinformatica.api_solicitacoes.domain.model.Auditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public Page<Auditoria> consultar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho
    ) {
        return auditoriaService.consultar(pagina, tamanho);
    }
}
