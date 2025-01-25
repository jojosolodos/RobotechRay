package com.robotech.web.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.robotech.web.models.Club;
import com.robotech.web.models.Estado_membresia;
import com.robotech.web.models.Membresia_club;
import com.robotech.web.models.Usuario;
import com.robotech.web.services.Estado_membresiaService;
import com.robotech.web.services.Membresia_ClubService;
import com.robotech.web.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/club")
public class ClubController {

	@Autowired private Membresia_ClubService membresiaClubService;
	@Autowired private UsuarioService usuarioService;
	@Autowired private Estado_membresiaService estadoMembresiaService;

	// INICIO DEL PANEL ADMINISTRATIVO DEL DUEÑO DEL CLUB
	@GetMapping("")
	public String inicioClub(Model model, HttpSession session) {
		return "club/panelClub";
	}

	// VER MIEMBROS ACTIVOS DEL CLUB
	@GetMapping("/miembrosClub")
	public String listarJugadores(HttpSession session, Model model) {

		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

		if (usuarioSesion == null || usuarioSesion.getId() == null) {
			return "redirect:/login";
		}

		Club club = usuarioService.obtenerClubPorUsuario(usuarioSesion);
		if (club == null) {
			model.addAttribute("error", "No tienes un club asignado.");
			return "club/panelClub";
		}

		List<Membresia_club> miembrosActivos = membresiaClubService.obtenerMiembrosPorEstado(club.getId(), 1);
		model.addAttribute("miembros", miembrosActivos);
		return "club/miembros";
	}
	
	// VER SOLICITUDES DE MIEMBROS AL CLUB
	@GetMapping("/solicitudes")
    public String verMiembrosPendientes(HttpSession session, Model model) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null || usuarioSesion.getId() == null) {
            return "redirect:/login";
        }

        Club club = usuarioService.obtenerClubPorUsuario(usuarioSesion);
        if (club == null) {
            model.addAttribute("error", "No tienes un club asignado.");
            return "club/panelClub";
        }

        List<Membresia_club> miembrosPendientes = membresiaClubService.obtenerMiembrosPorEstado(club.getId(), 3);
        model.addAttribute("miembros", miembrosPendientes);
        
        if (miembrosPendientes.isEmpty()) {
            model.addAttribute("mensajeNoSolicitudes", "Ya no hay solicitudes pendientes.");
        }
        
        return "club/solicitudes";
    }
	
	@PostMapping("/membresias/cambiarEstado")
	public String cambiarEstadoMembresias(@RequestParam("id") List<Integer> membresiaIds,
	                                      @RequestParam("estadoMembresiaId") List<Integer> estadoIds) {
	    System.out.println("Membresía IDs recibidos: " + membresiaIds);  // Ver los IDs recibidos

	    for (int i = 0; i < membresiaIds.size(); i++) {
	        Integer membresiaId = membresiaIds.get(i);
	        Integer estadoId = estadoIds.get(i);
	        System.out.println("Procesando Membresía ID: " + membresiaId + " con Estado ID: " + estadoId);

	        Membresia_club membresia = membresiaClubService.listarID(membresiaId);
	        if (membresia == null) {
	            System.out.println("No se encontró la membresía con ID: " + membresiaId);
	            continue;
	        }

	        Estado_membresia estado = estadoMembresiaService.listarID(estadoId);
	        if (estado == null) {
	            System.out.println("No se encontró el estado con ID: " + estadoId);
	            continue;
	        }

	        membresia.setEstadoMembresiaId(estado);
	        membresiaClubService.guardarMembresiaClub(membresia);
	    }

	    return "redirect:/club/solicitudes";
	}



}
