package dev.cryss.forum.controller;

import com.sun.tools.jconsole.JConsoleContext;
import dev.cryss.forum.config.CustomRedisTemplate;
import dev.cryss.forum.controller.dto.DetalhesDoTopicoDto;
import dev.cryss.forum.controller.dto.RestPage;
import dev.cryss.forum.controller.dto.TopicoDto;
import dev.cryss.forum.controller.form.AtualizacaoTopicoForm;
import dev.cryss.forum.controller.form.TopicoForm;
import dev.cryss.forum.modelo.Topico;
import dev.cryss.forum.repository.CursoRepository;
import dev.cryss.forum.repository.TopicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.SecondaryTable;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;
	
	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	@Qualifier("redisTemplate")
	private CustomRedisTemplate redisTemplate;


	@GetMapping
	@Cacheable(value = "listaDeTopicos")
	public RestPage<TopicoDto> lista(@RequestParam(required = false) String nomeCurso,
									 @PageableDefault(sort =  "id", direction = Sort.Direction.DESC) Pageable paginacao) {

		CustomRedisTemplate customRedisTemplate = new CustomRedisTemplate ();

		if (nomeCurso == null) {
			Page<Topico> topicos = topicoRepository.findAll (paginacao);
			return TopicoDto.converter(topicos);
		} else {
			Page<Topico> topicos = topicoRepository.findByCursoNome(nomeCurso, paginacao);


			return TopicoDto.converter(topicos);
		}
	}

	@GetMapping("/limpa")
	public ResponseEntity<Void>  limpa() {


		Set<String> keysToDelete = new HashSet<> ();


		Cursor<String>  cursorToDelete = redisTemplate.scan (ScanOptions.scanOptions ().match ("listaDeTopicos:*").build ());




		if(Objects.nonNull (cursorToDelete)){
			while (cursorToDelete.hasNext ()){
				keysToDelete.add (cursorToDelete.next ());

			}
		}

		if(Objects.nonNull (keysToDelete) && !keysToDelete.isEmpty ()){
			redisTemplate.unlink (keysToDelete);
		}

		return ResponseEntity.noContent ().build ();

		}

//
//	@GetMapping
//	@Cacheable(value = "listaDeTopicos")
//	public List<TopicoDto> lista(@RequestParam(required = false) String nomeCurso) {
//
//			return  TopicoDto.converter (topicoRepository.findAll ());
//
//	}





	@PostMapping
	@Transactional
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder uriBuilder) {
		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico);
		
		URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicoDto(topico));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<DetalhesDoTopicoDto> detalhar(@PathVariable Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		if (topico.isPresent()) {
			return ResponseEntity.ok(new DetalhesDoTopicoDto (topico.get()));
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@PutMapping("/{id}")
	@Transactional
	public ResponseEntity<TopicoDto> atualizar(@PathVariable Long id, @RequestBody @Valid AtualizacaoTopicoForm form) {
		Optional<Topico> optional = topicoRepository.findById(id);
		if (optional.isPresent()) {
			Topico topico = form.atualizar(id, topicoRepository);
			return ResponseEntity.ok(new TopicoDto(topico));
		}
		
		return ResponseEntity.notFound().build();
	}
	
	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<?> remover(@PathVariable Long id) {
		Optional<Topico> optional = topicoRepository.findById(id);
		if (optional.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}
		
		return ResponseEntity.notFound().build();
	}

}







