package com.robotech.web.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotech.web.dao.IInscripcionesDao;
import com.robotech.web.models.Inscripciones;
import com.robotech.web.models.Torneo;

@Service
public class InscripcionesService {
	
	@Autowired
	private IInscripcionesDao inscripcionesDao;
	
	@Autowired
	private TorneoService torneoService;
	
	public List<Inscripciones> listarInscripciones(){
		return inscripcionesDao.findAll();
	}
	
	public Inscripciones listarID(Integer id) {
		return inscripcionesDao.findById(id).orElse(null);
	}
	
	public void guaradarInscripciones(Inscripciones inscripciones) {
		inscripcionesDao.save(inscripciones);
	}
	
	public void eliminarInscripciones(Integer id) {
		inscripcionesDao.deleteById(id);
	}
	
	public List<Inscripciones> obtenerPorTorneo(int torneoId, int estadoId) {
		 Torneo torneo = torneoService.listarID(torneoId); // Obtener el torneo por su ID
        return inscripcionesDao.findByTorneoIdAndEstadoInscripcionId_Id(torneo, estadoId);
    }
	
	public boolean existeInscripcion(Integer usuarioId, int torneoId) {
		return inscripcionesDao.existsByUsuarioIdIdAndTorneoIdId(usuarioId, torneoId);
	}
	
	public Inscripciones obtenerInscripcionPorUsuario(int usuarioId) {
		return inscripcionesDao.findByUsuarioIdId(usuarioId);
	}
	
	public Inscripciones obtenerOtraInscripcion(int usuarioId, int torneoActualId) {
	    List<Inscripciones> inscripcionesUsuario = inscripcionesDao.findByUsuarioId_Id(usuarioId);
	    return inscripcionesUsuario.stream()
	            .filter(inscripcion -> inscripcion.getTorneoId().getId() != torneoActualId)
	            .findFirst()
	            .orElse(null);
	}

	public boolean puedeInscribirse(Integer usuarioId) {
        // Obtener las inscripciones ordenadas por fecha para el usuario
        List<Inscripciones> inscripciones = inscripcionesDao.findByUsuarioIdOrderByFechaInscripcionDesc(usuarioId);

        if (inscripciones.isEmpty()) {
            // No tiene inscripciones previas, puede inscribirse
            return true;
        }

        // Obtener la última inscripción
        Inscripciones ultimaInscripcion = inscripciones.get(0);

        // Calcular la diferencia en días entre la última inscripción y la fecha actual
        Date ahora = new Date();
        long diferenciaMillis = ahora.getTime() - ultimaInscripcion.getFechaInscripcion().getTime();
        long diferenciaDias = diferenciaMillis / (1000 * 60 * 60 * 24);

        // Verificar si han pasado al menos 5 días
        return diferenciaDias >= 5;
    }
	
	// Verificar si un club puede inscribir más participantes en un torneo
    public boolean puedeInscribirMasParticipantes(Integer torneoId, Integer clubId) {
        long inscritos = inscripcionesDao.countByTorneoAndClub(torneoId, clubId);
        return inscritos < 3; // Permitir solo si hay menos de 3 inscritos
    }


}
