 package br.com.cotiinformatica.api_solicitacoes.domain.services;

import br.com.cotiinformatica.api_solicitacoes.domain.dtos.SolicitacaoRequest;
import br.com.cotiinformatica.api_solicitacoes.domain.dtos.SolicitacaoResponse;
import br.com.cotiinformatica.api_solicitacoes.domain.enums.PrioridadeSolicitacao;
import br.com.cotiinformatica.api_solicitacoes.domain.enums.StatusSolicitacao;
import br.com.cotiinformatica.api_solicitacoes.domain.exceptions.RegistroNaoEncontradoException;
import br.com.cotiinformatica.api_solicitacoes.domain.interfaces.SolicitacaoService;
import br.com.cotiinformatica.api_solicitacoes.domain.model.Auditoria;
import br.com.cotiinformatica.api_solicitacoes.domain.model.Solicitacao;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.components.MessageProducerComponent;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.repositories.AuditoriaRepository;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.repositories.SolicitacaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolicitacaoServiceImpl implements SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final MessageProducerComponent messageProducerComponent;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public SolicitacaoResponse criar(SolicitacaoRequest request) throws Exception {

        var solicitacao = new Solicitacao();

        solicitacao.setSolicitante(request.solicitante());
        solicitacao.setDescricao(request.descricao());
        solicitacao.setPrioridade(PrioridadeSolicitacao.valueOf(request.prioridade()));

        solicitacao.setDataHora(LocalDateTime.now());
        solicitacao.setStatus(StatusSolicitacao.ABERTA);

        solicitacaoRepository.save(solicitacao);

        var mensagem = objectMapper.writeValueAsString(solicitacao);

        messageProducerComponent.sendMessage(mensagem);

        var auditoria = new Auditoria();
        auditoria.setId(UUID.randomUUID().toString());
        auditoria.setDataHora(LocalDateTime.now());
        auditoria.setSolicitacao(mensagem);
        auditoria.setOperacao("CADASTRO");

        auditoriaRepository.save(auditoria);

        return toResponse(solicitacao);
    }

    @Override
    public SolicitacaoResponse alterar(
            UUID id,
            SolicitacaoRequest request) throws Exception {

        var solicitacao = solicitacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Solicitação não encontrada"
                        )
                );

        solicitacao.setSolicitante(request.solicitante());
        solicitacao.setDescricao(request.descricao());
        solicitacao.setPrioridade(PrioridadeSolicitacao.valueOf(request.prioridade()));

        solicitacaoRepository.save(solicitacao);

        return toResponse(solicitacao);
    }

    @Override
    public SolicitacaoResponse alterarStatus(
            UUID id,
            String status) throws Exception {

        var solicitacao = solicitacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Solicitação não encontrada"
                        )
                );

        solicitacao.setStatus(
                StatusSolicitacao.valueOf(status)
        );

        solicitacaoRepository.save(solicitacao);

        return toResponse(solicitacao);
    }

    @Override
    public SolicitacaoResponse excluir(UUID id) throws Exception {

        var solicitacao = solicitacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Solicitação não encontrada"
                        )
                );

        solicitacaoRepository.delete(solicitacao);

        return toResponse(solicitacao);
    }

    @Override
    public Page<SolicitacaoResponse> consultar(
            int page,
            int size) throws Exception {

        var pageable = PageRequest.of(page, size);

        return solicitacaoRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public SolicitacaoResponse obterPorId(UUID id) throws Exception {

        var solicitacao = solicitacaoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Solicitação não encontrada"
                        )
                );

        return toResponse(solicitacao);
    }

    /**
     * Converte a entidade Solicitação para o DTO
     * utilizado como resposta da API.
     */
    private SolicitacaoResponse toResponse(Solicitacao solicitacao) {

        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getSolicitante(),
                solicitacao.getDescricao(),
                solicitacao.getDataHora(),
                solicitacao.getStatus().name(),
                solicitacao.getPrioridade().toString()
        );
    }
}
