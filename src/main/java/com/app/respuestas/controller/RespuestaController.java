package com.app.respuestas.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.app.respuestas.clients.PreguntasFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.respuestas.clients.EstadisticaFeignClient;
import com.app.respuestas.clients.ProyectosFeignClient;
import com.app.respuestas.models.Formularios;
import com.app.respuestas.models.Respuestas;
import com.app.respuestas.repository.FormulariosRepository;
import com.app.respuestas.repository.RespuestasRepository;
import com.app.respuestas.services.IFormulariosServices;
import com.app.respuestas.services.IRespuestasServices;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RespuestaController {

	@SuppressWarnings("rawtypes")
	@Autowired
	private CircuitBreakerFactory cbFactory;

	@Autowired
	RespuestasRepository rRepository;

	@Autowired
	ProyectosFeignClient prClient;

	@Autowired
	FormulariosRepository fRepository;

	@Autowired
	IRespuestasServices rServices;

	@Autowired
	IFormulariosServices fServices;

	@Autowired
	EstadisticaFeignClient eClient;

	@Autowired
	PreguntasFeignClient preguntasClient;

//  ****************************	RESPUESTAS	***********************************  //

	// CREAR LISTA DE RESPUESTAS
	@PostMapping("/respuestas/proyecto/{idProyecto}/lista")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Boolean crearListaRespuestas(@PathVariable("idProyecto") Integer idProyecto,
										@RequestBody @Validated List<Respuestas> listaRespuestas) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			rServices.creaRespuestasTotal(listaRespuestas);
			/*
			 * if (cbFactory.create("respuestas").run( () ->
			 * eClient.obtenerEstadistica(idProyecto,
			 * listaRespuestas.get(0).getFormulario()), e -> errorConexion(e))) {
			 * log.info("Obtencion estadisticas Correcta"); }
			 */
			return true;
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// CREAR UNA RESPUESTA
	@PostMapping("/respuestas/proyecto/{idProyecto}/sencilla")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Boolean crearRespuestas(@RequestBody @Validated Respuestas respuesta) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(respuesta.getIdProyecto()),
				e -> encontrarProyecto(respuesta.getIdProyecto(), e))) {
			rServices.crearRespuesta(respuesta);
			if (cbFactory.create("respuestas").run(() -> eClient.obtenerEstadisticaResultado(respuesta.getIdProyecto(),
					respuesta.getFormulario(), respuesta.getNumeroPregunta()), e -> errorConexion(e))) {
				log.info("Creacion Correcta");
			}
			return true;
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// VER TODAS LAS PREGUNTAS DE UN USUARIO EN UN FORMULARIO
	@GetMapping("/respuestas/ver/todas/username/{idProyecto}")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Respuestas> verRespuestasUsuario(@PathVariable("idProyecto") Integer idProyecto,
												 @RequestParam(value = "username") String username,
												 @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (rServices.existIdFormularioUsername(idProyecto, formulario, username))
				return rServices.findIdFormularioUsername(idProyecto, formulario, username);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Las respuestas del usuario no existen");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// VER UNA RESPUESTA DE UN USUARIO DE UNA PREGUNTA EN UN FORMULARIO
	@GetMapping("/respuestas/ver/una/username/{idProyecto}")
	@ResponseStatus(code = HttpStatus.OK)
	public Respuestas verRespuestasUsuario(@PathVariable("idProyecto") Integer idProyecto,
										   @RequestParam("username") String username, @RequestParam("numeroPregunta") Integer numeroPregunta,
										   @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (rServices.existIdNumeroFormularioUsername(idProyecto, numeroPregunta, formulario, username))
				return rServices.findIdNumeroFormularioUsername(idProyecto, numeroPregunta, formulario, username);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La respuesta del usuario no existe");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// VER TODAS LAS RESPUESTAS DE UN PROYECTO
	@GetMapping("/respuestas/ver/todas/pregunta/proyecto/{idProyecto}")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Respuestas> verRespuestasPreguntaProyecto(@PathVariable("idProyecto") Integer idProyecto,
														  @RequestParam("numeroPregunta") Integer numeroPregunta,
														  @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (rServices.existIdFormularioNumero(idProyecto, formulario, numeroPregunta))
				return rServices.findIdFormularioNumero(idProyecto, formulario, numeroPregunta);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Las respuestas del proyecto no existen");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	// MICROSERVICIO ESTADISTICAS -> OBTENER RESPUESTAS PROYECTO
	// VER RESPUESTAS DE UN PROYECTO
	@GetMapping("/respuestas/proyecto/{idProyecto}")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Respuestas> verRespuestasProyecto(@PathVariable("idProyecto") Integer idProyecto,
												  @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
		if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
				e -> encontrarProyecto(idProyecto, e))) {
			if (rRepository.existsByIdProyectoAndFormulario(idProyecto, formulario))
				return rRepository.findByIdProyectoAndFormulario(idProyecto, formulario);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Las respuestas del proyecto no existen");
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
	}

	@GetMapping("/respuestas/proyecto/{idProyecto}/{formulario}/exists")
	public Boolean respuestasProyectoExisten(@PathVariable("idProyecto") Integer idProyecto,
											 @PathVariable("formulario") Integer formulario) {
		return rServices.existeProyectoFormulario(idProyecto, formulario);
	}

	// Obtener Kano
	@GetMapping("/respuestas/obtener/todas")
	@ResponseStatus(code = HttpStatus.OK)
	public HashMap<String, HashMap<Integer, List<List<String>>>>  obtenerTodasRespuestas(){
		List<Respuestas> listaRespuestas = rRepository.findAll();
		HashMap<String, HashMap<Integer, List<List<String>>>> respuestasProyectos = new HashMap<>();
		listaRespuestas.forEach(lr -> {
			List<String> lImpacto = preguntasClient.obtenerImpactoPreguntas(lr.getIdProyecto(),lr.getNumeroPregunta());
			Collections.reverse(lImpacto);
			List<String> lOpcionesRespuesta = lr.getRespuestas();
			String nombre = prClient.obtenerNombre(lr.getIdProyecto());
			List<String> respuesta = new ArrayList<>();
			lOpcionesRespuesta.forEach(x -> {
				respuesta.add(lImpacto.get(Integer.parseInt(x)));
			});
			if(respuestasProyectos.containsKey(nombre)){
				HashMap<Integer, List<List<String>>> hM = respuestasProyectos.get(nombre);
				if(hM.containsKey(lr.getNumeroPregunta())){
					List<List<String>> rP = hM.get(lr.getNumeroPregunta());
					rP.add(respuesta);
					hM.put(lr.getNumeroPregunta(),rP);
					respuestasProyectos.put(nombre,hM);
				} else{
					List<List<String>> rP = new ArrayList<>();
					rP.add(respuesta);
					hM.put(lr.getNumeroPregunta(),rP);
					respuestasProyectos.put(nombre,hM);
				}
			} else {
				HashMap<Integer, List<List<String>>> hM = new HashMap<>();
				List<List<String>> rP = new ArrayList<>();
				rP.add(respuesta);
				hM.put(lr.getNumeroPregunta(),rP);
                respuestasProyectos.put(nombre,hM);
			}
		});
        return respuestasProyectos;
	}

	@GetMapping("/respuestas/ver-todas")
	@ResponseStatus(code = HttpStatus.OK)
	public List<Respuestas> verTodasRespuestas(){
		return rRepository.findAll();
	}

	// Funciones para arreglar problemas
	@DeleteMapping("/respuestas/eliminar-preguntas-problema")
	public Boolean eliminarPreguntasProblema(){
		rRepository.deleteByIdProyecto(2);
		rRepository.deleteByIdProyecto(6);
		rRepository.deleteByIdProyecto(17);
		return true;
	}

	@DeleteMapping("/respuestas/eliminar/proyecto/formulario/{idProyecto}")
	public Boolean eliminarRespuestasProyectoFormulario(@PathVariable("idProyecto") Integer idProyecto,
			@RequestParam(value = "formulario", defaultValue = "1") Integer formulario) throws IOException {
		try {
			if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
					e -> encontrarProyecto(idProyecto, e))) {
				rRepository.deleteByIdProyectoAndFormulario(idProyecto, formulario);
				fRepository.deleteByIdProyectoAndFormulario(idProyecto, formulario);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
		} catch (Exception e) {
			throw new IOException("error eliminar respuestas formulario, respuestas: " + e.getMessage());
		}
	}

	@DeleteMapping("/respuestas/eliminar/proyecto/pregunta/{idProyecto}")
	public Boolean eliminarRespuestasProyectoFormularioPregunta(@PathVariable("idProyecto") Integer idProyecto,
			@RequestParam(value = "formulario", defaultValue = "1") Integer formulario,
			@RequestParam("numeroPregunta") Integer numeroPregunta) throws IOException {
		try {
			if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
					e -> encontrarProyecto(idProyecto, e))) {
				rRepository.deleteByIdProyectoAndFormularioAndNumeroPregunta(idProyecto, formulario, numeroPregunta);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
		} catch (Exception e) {
			throw new IOException("error eliminar proyecto, respuestas: " + e.getMessage());
		}
	}

	@DeleteMapping("/respuestas/eliminar/proyecto/todo/{idProyecto}")
	public Boolean eliminarRespuestasProyecto(@PathVariable("idProyecto") Integer idProyecto) throws IOException {
		try {
			if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
					e -> encontrarProyecto(idProyecto, e))) {
				rRepository.deleteByIdProyecto(idProyecto);
				fRepository.deleteByIdProyecto(idProyecto);
				return true;
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
		} catch (Exception e) {
			throw new IOException("error eliminar proyecto, respuestas: " + e.getMessage());
		}
	}


//  ****************************	FUNCIONES	***********************************  //

	@PostMapping("/respuestas/arreglar/")
	@ResponseStatus(code = HttpStatus.OK)
	public void arreglar() {

	}

	private Boolean errorConexion(Throwable e) {
		log.info(e.getMessage());
		return false;
	}

	private Boolean encontrarProyecto(Integer codigoProyecto, Throwable e) {
		log.error(e.getMessage());
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio Proyectos no disponible");
	}

}
