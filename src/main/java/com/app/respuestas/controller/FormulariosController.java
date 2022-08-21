package com.app.respuestas.controller;

import com.app.respuestas.clients.ProyectosFeignClient;
import com.app.respuestas.models.Formularios;
import com.app.respuestas.repository.FormulariosRepository;
import com.app.respuestas.services.IFormulariosServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/formularios")
public class FormulariosController {

    @SuppressWarnings("rawtypes")
    @Autowired
    private CircuitBreakerFactory cbFactory;

    @Autowired
    ProyectosFeignClient prClient;

    @Autowired
    FormulariosRepository fRepository;

    @Autowired
    IFormulariosServices fServices;

    @PostMapping("/crear/")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Boolean crearFormulario(@RequestBody @Validated Formularios formulario) {
        if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(formulario.getIdProyecto()),
                e -> encontrarProyecto(formulario.getIdProyecto(), e))) {
            fServices.crearFormulario(formulario);
            return true;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
    }

    @PutMapping("/finalizar-usuario/")
    @ResponseStatus(code = HttpStatus.OK)
    public Boolean finalizarFormulario(@RequestBody @Validated Formularios formulario) {
        if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(formulario.getIdProyecto()),
                e -> encontrarProyecto(formulario.getIdProyecto(), e))) {
            fServices.finalizarFormulario(formulario.getIdProyecto(), formulario);
            return true;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
    }

    @GetMapping("/ver/proyecto/{idProyecto}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Formularios> verFormularios(@PathVariable("idProyecto") Integer idProyecto,
                                            @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
        if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(idProyecto),
                e -> encontrarProyecto(idProyecto, e))) {
            if (fRepository.existsByIdProyectoAndFormulario(idProyecto, formulario))
                return fRepository.findByIdProyectoAndFormulario(idProyecto, formulario);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no tiene respuestas");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
    }

    @GetMapping("/cantidad/{idProyecto}")
    @ResponseStatus(code = HttpStatus.OK)
    public Integer cantidadFormularios(@PathVariable("idProyecto") Integer idProyecto,
                                       @RequestParam(value = "formulario", defaultValue = "1") Integer formulario) {
        List<Formularios> f = fRepository.findByIdProyectoAndFormulario(idProyecto,formulario);
        return f.size();
    }

    @GetMapping("/ver/username/respondido/")
    @ResponseStatus(code = HttpStatus.OK)
    public Boolean verFormulariosUsername(@RequestBody @Validated Formularios formulario) {
        if (cbFactory.create("respuestas").run(() -> prClient.existCodigoProyecto(formulario.getIdProyecto()),
                e -> encontrarProyecto(formulario.getIdProyecto(), e))) {
            if (fRepository.existsByIdProyectoAndFormularioAndUsername(formulario.getIdProyecto(),
                    formulario.getFormulario(), formulario.getUsername()))
                return fRepository.findByIdProyectoAndFormularioAndUsername(formulario.getIdProyecto(),
                        formulario.getFormulario(), formulario.getUsername()).getRespondido();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Usuario no ha respondido");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El Proyecto no existe");
    }

    //  ****************************	FUNCIONES	***********************************  //

    private Boolean encontrarProyecto(Integer codigoProyecto, Throwable e) {
        log.error(e.getMessage());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Servicio Proyectos no disponible");
    }
}
