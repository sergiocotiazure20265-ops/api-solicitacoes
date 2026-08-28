package br.com.cotiinformatica.api_solicitacoes.domain.interfaces;

import br.com.cotiinformatica.api_solicitacoes.domain.model.Auditoria;
import org.springframework.data.domain.Page;

public interface AuditoriaService {

    Page<Auditoria> consultar(int pagina, int tamanho);

}
