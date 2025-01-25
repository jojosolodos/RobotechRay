package com.robotech.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.robotech.web.dao.IVictoriasDao;
import com.robotech.web.models.Victorias;

@Service
public class VictoriasService {
	
	@Autowired
	private IVictoriasDao victoriasDao;
	
	public List<Victorias> listarVictorias(){
		return victoriasDao.findAll();
	}
	
	public Victorias listarID(Integer id) {
		return victoriasDao.findById(id).orElse(null);
	}
	
	public void guardarVictorias(Victorias victorias) {
		victoriasDao.save(victorias);
	}
	
	public void eliminarVictorias(Integer id) {
		victoriasDao.deleteById(id);
	}
	
	///////////////////////////////////////////////////////////
	
	public List<Victorias> listarVictoriasDesc(){
		return victoriasDao.findAllByOrderByCantidadDesc();
	}
	
	public Page<Victorias> listarVictoriasDescPageable(Pageable pageable){
		return victoriasDao.findAllByOrderByCantidadDescPageable(pageable);
	}
	
	public List<Victorias> listarTop3Victorias() {
        Pageable topThree = PageRequest.of(0, 3); // Página 0 con 3 resultados
        return victoriasDao.findAllByOrderByCantidadDesc(topThree);
    }	


}
