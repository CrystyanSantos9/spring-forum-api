package dev.cryss.forum.controller.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.cryss.forum.modelo.Topico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.querydsl.QPageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


public class TopicoDto implements Serializable {

	private Long id;
	private String titulo;
	private String mensagem;
	private LocalDateTime dataCriacao;

	public TopicoDto() {
	}

	public TopicoDto(Topico topico) {
		this.id = topico.getId();
		this.titulo = topico.getTitulo();
		this.mensagem = topico.getMensagem();
		this.dataCriacao = topico.getDataCriacao();
	}

	public Long getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getMensagem() {
		return mensagem;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public static RestPage<TopicoDto> converter(Page<Topico> topicos) {

		List<TopicoDto> topicsDtos = topicos.getContent ().stream().map (TopicoDto::new).collect(Collectors.toList());

		return new RestPage<> (topicsDtos, topicos.getPageable ().getPageNumber (), topicos.getSize (), topicos.getTotalElements ());

	}

}
