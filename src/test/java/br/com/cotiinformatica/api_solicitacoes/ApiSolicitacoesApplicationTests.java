package br.com.cotiinformatica.api_solicitacoes;

import br.com.cotiinformatica.api_solicitacoes.domain.dtos.SolicitacaoRequest;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.components.MessageProducerComponent;
import br.com.cotiinformatica.api_solicitacoes.infrastructure.repositories.AuditoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração da API de solicitações.
 * <p>
 * Os testes utilizam:
 * <p>
 * - MockMvc para realizar requisições HTTP contra a aplicação;
 * - ObjectMapper para converter objetos Java em JSON;
 * - Implementação real da camada de serviço;
 * - Implementação real dos repositories;
 * - H2 como banco de dados em memória;
 * - Profile "test" definido em application-test.yaml.
 * <p>
 * Nenhuma camada de negócio é mockada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Testes de integração da API de Solicitações")
class ApiSolicitacoesApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /*
     * Mock do MongoDB.
     *
     * Impede que os testes precisem de um MongoDB real.
     */
    @MockitoBean
    AuditoriaRepository auditoriaRepository;

    /*
     * Mock do RabbitMQ.
     *
     * Impede que os testes tentem enviar mensagens
     * para um servidor RabbitMQ real.
     */
    @MockitoBean
    MessageProducerComponent messageProducerComponent;

    private static String solicitacaoId;

    // restante dos testes...

    /**
     * Verifica se o contexto completo do Spring Boot é carregado.
     */
    @Test
    @Order(1)
    @DisplayName("Deve carregar o contexto da aplicação")
    void deveCarregarContexto() {
    }

    /**
     * Testa o cadastro real de uma solicitação.
     * <p>
     * Fluxo:
     * Controller -> Service -> Repository -> H2
     * <p>
     * Resultado esperado:
     * HTTP 201 Created.
     */
    @Test
    @Order(2)
    @DisplayName("POST - Deve cadastrar uma solicitação")
    void deveCadastrarSolicitacao() throws Exception {

        var request = new SolicitacaoRequest(
                "João da Silva",
                "Computador não está conectando à internet.",
                "ALTA"
        );

        var result = mockMvc.perform(
                        post("/api/v1/solicitacoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.solicitante", is("João da Silva")))
                .andExpect(jsonPath(
                        "$.descricao",
                        is("Computador não está conectando à internet.")
                ))
                .andExpect(jsonPath("$.prioridade", is("ALTA")))
                .andReturn();

        var json = result.getResponse().getContentAsString();

        var response = objectMapper.readTree(json);

        solicitacaoId = response.get("id").asText();
    }

    /**
     * Verifica a validação do request.
     * <p>
     * Como os campos obrigatórios estão inválidos,
     * a API deve responder com HTTP 400.
     */
    @Test
    @Order(3)
    @DisplayName("POST - Deve retornar 400 para dados inválidos")
    void deveRetornarBadRequestAoCadastrarDadosInvalidos() throws Exception {

        var request = new SolicitacaoRequest(
                "",
                "",
                ""
        );

        mockMvc.perform(
                        post("/api/v1/solicitacoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Consulta a solicitação criada anteriormente pelo seu UUID.
     * <p>
     * Resultado esperado:
     * HTTP 200 e os dados persistidos no banco H2.
     */
    @Test
    @Order(4)
    @DisplayName("GET /{id} - Deve consultar uma solicitação pelo ID")
    void deveConsultarSolicitacaoPorId() throws Exception {

        mockMvc.perform(
                        get("/api/v1/solicitacoes/{id}", solicitacaoId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitacaoId)))
                .andExpect(jsonPath("$.solicitante", is("João da Silva")))
                .andExpect(jsonPath("$.prioridade", is("ALTA")));
    }

    /**
     * Testa a consulta paginada utilizando os dados realmente
     * persistidos no H2.
     */
    @Test
    @Order(5)
    @DisplayName("GET - Deve consultar solicitações paginadas")
    void deveConsultarSolicitacoes() throws Exception {

        mockMvc.perform(
                        get("/api/v1/solicitacoes")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(solicitacaoId)))
                .andExpect(jsonPath("$.content[0].solicitante", is("João da Silva")))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    /**
     * Testa a alteração completa de uma solicitação.
     * <p>
     * O registro existente é atualizado utilizando a implementação
     * real da camada de serviço.
     */
    @Test
    @Order(6)
    @DisplayName("PUT /{id} - Deve alterar uma solicitação")
    void deveAlterarSolicitacao() throws Exception {

        var request = new SolicitacaoRequest(
                "Maria da Silva",
                "Computador foi encaminhado para manutenção técnica.",
                "MEDIA"
        );

        mockMvc.perform(
                        put("/api/v1/solicitacoes/{id}", solicitacaoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitacaoId)))
                .andExpect(jsonPath("$.solicitante", is("Maria da Silva")))
                .andExpect(jsonPath(
                        "$.descricao",
                        is("Computador foi encaminhado para manutenção técnica.")
                ))
                .andExpect(jsonPath("$.prioridade", is("MEDIA")));
    }

    /**
     * Testa a alteração isolada do status da solicitação.
     * <p>
     * Resultado esperado:
     * HTTP 200 e o novo status no response.
     */
    @Test
    @Order(7)
    @DisplayName("PATCH /{id}/status - Deve alterar o status")
    void deveAlterarStatusSolicitacao() throws Exception {

        mockMvc.perform(
                        patch("/api/v1/solicitacoes/{id}/status", solicitacaoId)
                                .param("status", "APROVADA")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitacaoId)))
                .andExpect(jsonPath("$.status", is("APROVADA")));
    }

    /**
     * Testa a exclusão da solicitação.
     * <p>
     * O registro é removido do banco H2 utilizando o fluxo
     * completo da aplicação.
     */
    @Test
    @Order(8)
    @DisplayName("DELETE /{id} - Deve excluir uma solicitação")
    void deveExcluirSolicitacao() throws Exception {

        mockMvc.perform(
                        delete("/api/v1/solicitacoes/{id}", solicitacaoId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(solicitacaoId)));
    }

    /**
     * Verifica que, após a exclusão, a listagem não possui
     * mais registros.
     */
    @Test
    @Order(9)
    @DisplayName("GET - Deve retornar lista vazia após exclusão")
    void deveRetornarListaVaziaAposExclusao() throws Exception {

        mockMvc.perform(
                        get("/api/v1/solicitacoes")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }
}
