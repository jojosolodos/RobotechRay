package com.robotech.web.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.robotech.web.models.Comunidad;

public interface IComunidadDao extends JpaRepository<Comunidad, Integer>{
	
	List<Comunidad> findByTema(String tema);
	
	Page<Comunidad> findByTemaContainingIgnoreCase(String tema, Pageable pageable);

}
