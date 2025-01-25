package com.robotech.web.controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.robotech.web.models.Categoria;
import com.robotech.web.models.Club;
import com.robotech.web.models.Estado_torneo;
import com.robotech.web.models.Estado_usuario;
import com.robotech.web.models.Torneo;
import com.robotech.web.models.Usuario;
import com.robotech.web.services.CategoriaService;
import com.robotech.web.services.ClubService;
import com.robotech.web.services.Estado_torneoService;
import com.robotech.web.services.Estado_usuarioService;
import com.robotech.web.services.TorneoService;
import com.robotech.web.services.UploadFileService;
import com.robotech.web.services.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired private UsuarioService usuarioService;
	@Autowired private Estado_usuarioService estadoUsuService;
	@Autowired private ClubService clubService;
	@Autowired private UploadFileService uploadService;
	@Autowired private TorneoService torneoService;
	@Autowired private CategoriaService categoriaService;
	@Autowired private Estado_torneoService estadoTorneoService;
	
	// INICIO DEL PANEL DE CONTROL ADMINISTRADOR
	@GetMapping("")
	public String inicioPanel(Model model, HttpSession session) {
		return "admin/panel";
	}
	//muestra los usuarios Aprobados
		@GetMapping("/usuarios-aprobados")
		public String mostratUsuariosAprobados(@RequestParam(value = "page", defaultValue = "0") int page,
												@RequestParam(value = "size", defaultValue = "2") int size,
												Model model, HttpSession session) {
			
			Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
		    if (usuarioSesion == null) {
		        return "redirect:/login"; 
		    }
		    
		 // Validar que el índice de página no sea menor que 0
		    if (page < 0) {
		        page = 0;
		    }
		    
		    Page<Usuario> usuariosAprobados= usuarioService.listtarUsuariosAprobados(page, size);

		    if (usuariosAprobados.isEmpty()) {
		        model.addAttribute("usuarios", new ArrayList<>()); // Lista vacía para evitar errores en la vista
		        model.addAttribute("currentPage", 0);
		        model.addAttribute("totalPages", 0);
		        model.addAttribute("size", size);
		        model.addAttribute("noUsersMessage", "No hay usuarios desaprobados para mostrar.");
		    } else {
		        model.addAttribute("usuarios", usuariosAprobados.getContent());
		        model.addAttribute("currentPage", page);
		        model.addAttribute("totalPages", usuariosAprobados.getTotalPages());
		        model.addAttribute("size", size);
		    }

			
			return "admin/usuApproved";
		}
	
	//muestra los usuarios desaprobados
	@GetMapping("/usuarios-desaprobados")
	public String mostratUsuariosDesaprobados(@RequestParam(value = "page", defaultValue = "0") int page,
												@RequestParam(value = "size", defaultValue = "5") int size,
												Model model, HttpSession session) {
		
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
	    if (usuarioSesion == null) {
	        return "redirect:/login"; 
	    }
	    
	 // Validar que el índice de página no sea menor que 0
	    if (page < 0) {
	        page = 0;
	    }
	    
	    Page<Usuario> usuariosDesaprobados = usuarioService.listtarUsuariosDesaprobados(page, size);

	    if (usuariosDesaprobados.isEmpty()) {
	        model.addAttribute("usuarios", new ArrayList<>()); // Lista vacía para evitar errores en la vista
	        model.addAttribute("currentPage", 0);
	        model.addAttribute("totalPages", 0);
	        model.addAttribute("size", size);
	        model.addAttribute("noUsersMessage", "No hay usuarios desaprobados para mostrar.");
	    } else {
	        model.addAttribute("usuarios", usuariosDesaprobados.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", usuariosDesaprobados.getTotalPages());
	        model.addAttribute("size", size);
	    }

		return "admin/usuDisapproved";
	}
	
	
	//muestra a los Usuarios En espera
	@GetMapping("/usuarios-enespera")
	public String mostrarUsuariosEnEspera(@RequestParam(value = "page", defaultValue = "0") int page, 
											@RequestParam(value = "size", defaultValue = "5") int size, 
											Model model, HttpSession session) {
		
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
	    if (usuarioSesion == null) {
	        return "redirect:/login"; 
	    }
	    
	    if (page < 0) {
	        page = 0;
	    }
	    
	    Page<Usuario> usuarioEnEspera = usuarioService.listtarUsuariosEnEspera(page, size);
	    if (usuarioEnEspera.isEmpty()) {
	        model.addAttribute("usuarios", new ArrayList<>()); // Lista vacía para evitar errores en la vista
	        model.addAttribute("currentPage", 0);
	        model.addAttribute("totalPages", 0);
	        model.addAttribute("size", size);
	        model.addAttribute("noUsersMessage", "No hay usuarios desaprobados para mostrar.");
	    } else {
	        model.addAttribute("usuarios", usuarioEnEspera.getContent());
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", usuarioEnEspera.getTotalPages());
	        model.addAttribute("size", size);
	    }
		return "admin/usuariosAdmin";
	}
	
	@PostMapping("/usuarios/cambiarEstado")
	public String cambiarEstadoUsuarios(@RequestParam("usuarioId") List<Integer> usuarioIds,
	                                     @RequestParam("estadoId") List<Integer> estadoIds) {
	    // Iterar sobre los índices de las listas de IDs
	    for (int i = 0; i < usuarioIds.size(); i++) {
	        Integer usuarioId = usuarioIds.get(i);
	        Integer estadoId = estadoIds.get(i);

	        // Obtener el usuario
	        Usuario usuario = usuarioService.listarID(usuarioId);
	        if (usuario == null) {
	            throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId);
	        }

	        // Obtener el estado
	        Estado_usuario estado = estadoUsuService.listarID(estadoId);
	        if (estado == null) {
	            throw new IllegalArgumentException("Estado no encontrado con ID: " + estadoId);
	        }

	        // Actualizar el estado del usuario
	        usuario.setEstadoUsuario(estado);

	        // Guardar el usuario con el nuevo estado
	        usuarioService.guardarUsuario(usuario);
	    }

	    // Redirigir de nuevo a la página de usuarios
	    return "redirect:/admin/usuarios-enespera";
	}
	//CLUBS
	@GetMapping("/clubs")
	public String mostrarClubsAdmin(Model model, HttpSession session) {
		
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
	    if (usuarioSesion == null) {
	        return "redirect:/login"; 
	    }
	    
	    List<Club> allClubs = clubService.listarClub();
	    model.addAttribute("clubs", allClubs);
		
		return "admin/clubs/clubsAdmin";
	}
	
	@GetMapping("/clubs/crear")
	public String mostrarFormCLAdd(Model model) {
		
		List<Usuario> usu = usuarioService.listarUsuario();
		
		model.addAttribute("usuarios", usu);
		
		return "admin/clubs/clubsAdd";
	}
	
	//AGREGAR CLUB
	@PostMapping("/clubs/crear")
	public String crearClub(@RequestParam String nombre, 
	                        @RequestParam String descripcion, 
	                        @RequestParam MultipartFile img, 
	                        RedirectAttributes ra) {
	    try {
	        // Crear el club
	        Club nuevoClub = new Club();
	        nuevoClub.setNombre(nombre);
	        nuevoClub.setDescripcion(descripcion);
	        
	        Usuario ultimoUsuarioT3 = usuarioService.obtenerUltimoUsuario3(3);
	        
	        
	        if(ultimoUsuarioT3 != null) {
	        	nuevoClub.setUsuarioId(ultimoUsuarioT3);
	        }else {
	        	ra.addFlashAttribute("error", "no se encontro ningun usuario");
	        	return "redirect:/admin/clubs";
	        }
	        
	        
	        // Guardar la imagen del club (necesitarás un servicio para gestionar las imágenes)
	        String imgPath = uploadService.saveImage(img);
	        nuevoClub.setImg(imgPath);
	        
	        // Guardar el club en la base de datos
	        clubService.guardarClub(nuevoClub);
	        
	        ra.addFlashAttribute("success", "Club creado correctamente.");
	        return "redirect:/admin/clubs";  // Redirigir a la lista de clubes
	    } catch (Exception e) {
	        ra.addFlashAttribute("error", "Hubo un error al crear el club.");
	        return "redirect:/admin/clubs";  // Redirigir a la lista de clubes
	    }
	}

	
	@GetMapping("clubs/addusuarios")
	public String mostrarAgDuenoClub() {
		
		return "admin/clubs/addDuenoClub";
	}
	
	//AGREGAR DUEÑO DE CLUB
	@PostMapping("/clubs/addusuarios")
	public String agregarDuenoClub(@RequestParam String username,
									@RequestParam String password,
									@RequestParam String nombre,
									@RequestParam String email,
									@RequestParam Integer edad,
									@RequestParam MultipartFile img,
									@RequestParam Integer trayectoria,
									RedirectAttributes ra) {
		
		try {
			Usuario newDuenoClub = new Usuario();

	        String nombImg = uploadService.saveImage(img);

	        newDuenoClub.setUsername(username);
	        String passwordEncriptada = new BCryptPasswordEncoder().encode(password);
	        newDuenoClub.setPassword(passwordEncriptada);

	        newDuenoClub.setNombre(nombre);
	        newDuenoClub.setCorreo(email);
	        newDuenoClub.setEdad(edad);
	        newDuenoClub.setTrayectoria(trayectoria); // Asignar la trayectoria directamente

	        // Simplificar la lógica de asignación de categoría
	        if (trayectoria <= 24) {
	            newDuenoClub.setCategoriaId(usuarioService.AsignarCatAmateur());
	        } else if (trayectoria <= 48) {
	            newDuenoClub.setCategoriaId(usuarioService.AsignarCatSemiPro());
	        } else {
	            newDuenoClub.setCategoriaId(usuarioService.AsignarCatProfesional());
	        }

	        newDuenoClub.setFoto_perfil(nombImg);
	        newDuenoClub.setFoto_robot("default.png");

	        newDuenoClub.setTipoUsuario(usuarioService.AsignarTipoUsu3());
	        newDuenoClub.setEstadoUsuario(usuarioService.obtenerEstadoDuenoC());

	        usuarioService.guardarUsuario(newDuenoClub);
			
			System.out.println("se agrego el usuario correctamente");
			ra.addFlashAttribute("success", "Dueño de club creado correctamente");
			return "redirect:/admin/clubs/crear";
		}catch (Exception e){
			ra.addFlashAttribute("error", "Hubo un error al crear el Dueño del club");
			System.out.println("no wa");
			return "redirect:/admin/clubs/addusuarios";
		}
	}
	///////////////////////////////////////////////////////////////////////////////
	//GESTION DE TORNEOS//
	///////////////////////////////////////////////////////////////////////////////
	
	@GetMapping("/torneos")
	public String verTorneos(Model model, HttpSession session) {
		
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
	    if (usuarioSesion == null) {
	        return "redirect:/login"; 
	    }
	    
	    List<Torneo> torneos = torneoService.listarTorneo();
	    model.addAttribute("torneos", torneos);
	    
		
		return "admin/torneos/verTorneos";
	}
	
	@GetMapping("/torneos/crearTorneos")
	public String mostrarCrearTorneo(Model model) {
		
		model.addAttribute("categorias", categoriaService.listarCategoria());
		return "admin/torneos/crearTorneo";
	}
	
	@PostMapping("/torneos/crearTorneos")
	public String crearTorneos(@RequestParam String nombre,
								@RequestParam String descripcion,
								@RequestParam MultipartFile img,
								@RequestParam String centro,
								@RequestParam Date fecha,
								@RequestParam Integer jugReq,
								@RequestParam Integer categoriaId,
								RedirectAttributes ra) {
		
		try {
			Torneo newTorneo = new Torneo();
			
			String nomImg = uploadService.saveImage(img);
			newTorneo.setImg(nomImg);
			
			newTorneo.setNombre(nombre);
			newTorneo.setDescripcion(descripcion);
			newTorneo.setCentro(centro);
			newTorneo.setFecha(fecha);
			newTorneo.setJugReq(jugReq);
			
			Categoria categoria = categoriaService.listarID(categoriaId);
			newTorneo.setCategoriaId(categoria);
			
			Estado_torneo estadoTorneo = estadoTorneoService.listarID(1);
			newTorneo.setEstadoTorneoId(estadoTorneo);
			torneoService.guardarTorneo(newTorneo);
			
			System.out.println(newTorneo.getNombre() + " se ha creado correctamente.");
			ra.addFlashAttribute("success", "Torneo creado correctamente");
			return "redirect:/admin/torneos";
			
		} catch(Exception e){
			e.printStackTrace();
			ra.addFlashAttribute("error", "Hubo un error al crear el torneo");
		}
		
		return "redirect:/admin/torneos/crearTorneos";
	}
	
	
	
	
	
	

}
