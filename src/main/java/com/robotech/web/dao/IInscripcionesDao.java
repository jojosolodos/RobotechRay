package com.robotech.web.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.robotech.web.models.Inscripciones;
import com.robotech.web.models.Torneo;

public interface IInscripcionesDao extends JpaRepository<Inscripciones, Integer>{
	
    List<Inscripciones> findByTorneoIdAndEstadoInscripcionId_Id(Torneo torneo, Integer estadoInscripcionId);
    
    boolean existsByUsuarioIdIdAndTorneoIdId(int usuarioId, int torneoId);
    
    Inscripciones findByUsuarioIdId(Integer usuarioId);

	List<Inscripciones> findByUsuarioId_Id(int usuarioId);
	
	@Query("SELECT i FROM Inscripciones i WHERE i.usuarioId.id = :usuarioId ORDER BY i.fechaInscripcion DESC")
    List<Inscripciones> findByUsuarioIdOrderByFechaInscripcionDesc(@Param("usuarioId") Integer usuarioId);
	
	@Query("SELECT COUNT(i) FROM Inscripciones i WHERE i.torneoId.id = :torneoId AND i.usuarioId.id IN " +
	           "(SELECT m.usuarioId.id FROM Membresia_club m WHERE m.clubId.id = :clubId)")
	    long countByTorneoAndClub(@Param("torneoId") Integer torneoId, @Param("clubId") Integer clubId);
    
}
