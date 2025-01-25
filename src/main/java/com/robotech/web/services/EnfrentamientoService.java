package com.robotech.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robotech.web.dao.IEnfrentamientoDao;
import com.robotech.web.models.Enfrentamiento;

@Service
public class EnfrentamientoService {

	@Autowired
	private IEnfrentamientoDao enfrentamientoDao;
	
	public List<Enfrentamiento> listarEnfrentamiento(){
		return enfrentamientoDao.findAll();
	}
	
	public Enfrentamiento listarID(Integer id) {
		return enfrentamientoDao.findById(id).orElse(null);
	}
	
	public void guardarEnfrentamiento(Enfrentamiento enfrentamiento) {
		enfrentamientoDao.save(enfrentamiento);
	}
	
	public void eliminarEnfrentamiento(Integer id) {
		enfrentamientoDao.deleteById(id);
	}
}
