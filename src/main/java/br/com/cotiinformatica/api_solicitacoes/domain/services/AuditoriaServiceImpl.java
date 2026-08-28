package br.com.cotiinformatica.api_solicitacoes.domain.services;

import br.com.cotiinformatica.api_solicitacoes.domain.interfaces.AuditoriaService;
import br.com.cotiinformatica.api_solicitacoes.domain.model.Auditoria;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.repositories.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Override
    public Page<Auditoria> consultar(int pagina, int tamanho) {

        var pageable = PageRequest.of(
            pagina,
            tamanho,
            Sort.by(Sort.Direction.DESC, "dataHora")
        );

        return auditoriaRepository.findAll(pageable);
    }
}
