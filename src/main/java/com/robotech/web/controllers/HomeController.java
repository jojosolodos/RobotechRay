package com.robotech.web.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.robotech.web.models.Club;
import com.robotech.web.models.Comunidad;
import com.robotech.web.models.Estado_membresia;
import com.robotech.web.models.Inscripciones;
import com.robotech.web.models.Membresia_club;
import com.robotech.web.models.Torneo;
import com.robotech.web.models.Usuario;
import com.robotech.web.models.Victorias;
import com.robotech.web.services.ClubService;
import com.robotech.web.services.ComunidadService;
import com.robotech.web.services.Estado_inscripcionService;
import com.robotech.web.services.Estado_membresiaService;
import com.robotech.web.services.InscripcionesService;
import com.robotech.web.services.Membresia_ClubService;
import com.robotech.web.services.TorneoService;
import com.robotech.web.services.UploadFileService;
import com.robotech.web.services.UsuarioService;
import com.robotech.web.services.VictoriasService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class HomeController {

	@Autowired
	private ComunidadService comunidadService;
	@Autowired
	private VictoriasService victoriasService;
	@Autowired
	private TorneoService torneoService;
	@Autowired
	private ClubService clubService;
	@Autowired
	private UsuarioService usuarioService;
	@Autowired
	private Membresia_ClubService membresiaClubService;
	@Autowired
	private InscripcionesService inscripcionesService;
	@Autowired
	private UploadFileService uploadService;
	@Autowired
	private Estado_membresiaService estadoMembresiaService;
	@Autowired
	private Estado_inscripcionService estadoInscripcionesService;

	// INICIO
	@GetMapping("")
	public String inicio(Model model, HttpSession session) {
		List<Victorias> victorias = victoriasService.listarTop3Victorias();
		model.addAttribute("victorias", victorias);
		return "home/home";
	}

	///////////////////////////////////////////////////////////////////////
	/////////// TORNEOS //////////////
	//////////////////////////////////////////////////////////////////////

	// VER TORNEOS
	@GetMapping("/torneos")
	public String listarTorneos(@RequestParam(value = "page", defaultValue = "0") int page,
								@RequestParam(value = "size", defaultValue = "6") int size,
								Model model) {
		
		
		Page<Torneo> torneo = torneoService.listarTorneoPaginado(PageRequest.of(page, size));
		
		
		model.addAttribute("torneo", torneo.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPages", torneo.getTotalPages());
		
		
		return "home/torneo";
	}

	// VER DETALLES DEL TORNEO
	@GetMapping("/torneos/{id}")
	public String listarTorneosId(Model model, HttpSession session, @PathVariable("id") int id,
			@RequestParam(value = "inscripcionExitosa", required = false) String inscripcionExitosa) {
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
		Torneo torneoDet = torneoService.listarID(id);
		
		List<Inscripciones> inscripciones = inscripcionesService.obtenerPorTorneo(id, 2);
		model.addAttribute("inscripciones", inscripciones);

		if (usuarioSesion == null) {
			model.addAttribute("mensajeCuenta", "Debe tener una cuenta registrada con nosotros.");
		} else {
			Membresia_club membresia = membresiaClubService.obtenerPorUsuario(usuarioSesion.getId());
			if (membresia == null) {
				model.addAttribute("mensajeMembresia", "Usted no pertenece a ningún club.");
			} else if (membresia.getEstadoMembresiaId().getId() != 1) {
				model.addAttribute("mensajeMembresia", "Aún no puede participar, su estado de membresía se encuentra "
						+ membresia.getEstadoMembresiaId().getNombre() + ".");
			} else {
				int categoriaUsuario = usuarioSesion.getCategoriaId().getId();
				int categoriaTorneo = torneoDet.getCategoriaId().getId();

				if (categoriaUsuario != categoriaTorneo) {
					model.addAttribute("mensajeCategoria", "Este torneo no pertenece a tu categoría.");

				} else {
					// Validar si el usuario ya está inscrito en este torneo
					boolean yaInscrito = inscripcionesService.existeInscripcion(usuarioSesion.getId(), id);
					model.addAttribute("yaInscrito", yaInscrito);

					if (!yaInscrito) {
						// Verificar si está inscrito en otro torneo
						Inscripciones otraInscripcion = inscripcionesService
								.obtenerOtraInscripcion(usuarioSesion.getId(), id);
						if (otraInscripcion != null) {
							model.addAttribute("mensajeOtroTorneo",
									"Ya estás inscrito en otro torneo: " + otraInscripcion.getTorneoId().getNombre());
						} else {
							// Validar si el club ya tiene 3 participantes inscritos
							Integer clubId = membresia.getClubId().getId();
							boolean puedeInscribirMas = inscripcionesService.puedeInscribirMasParticipantes(id, clubId);
							if (!puedeInscribirMas) {
								model.addAttribute("mensajeClub",
										"Tu club ya tiene 3 participantes inscritos en este torneo.");
							} else {
								if(inscripciones.size() >= torneoDet.getJugReq()){
									model.addAttribute("mensajeClub","Este torneo ya alcanzo el limite de participantes.");
								}else {
									model.addAttribute("mostrarBotonInscripcion", true);
								}
							}
						}
					}
				}
			}
		}

		if (inscripcionExitosa != null) {
			model.addAttribute("inscripcionExitosa", true);
		}

		

		model.addAttribute("torneoDet", torneoDet);
		return "home/torneoDetalles";
	}

	@PostMapping("/torneos/{id}/inscribirse")
	public String incribirseTorneo(@PathVariable("id") int id, HttpSession session, RedirectAttributes ra) {
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

		if (usuarioSesion == null) {
			ra.addFlashAttribute("error", "Debe iniciar sesión para inscribirse en un torneo.");
			return "redirect:/login";
		}
		
		// Obtener el torneo
	    Torneo torneo = torneoService.listarID(id);
	    if (torneo == null) {
	        ra.addFlashAttribute("error", "El torneo no existe.");
	        return "redirect:/home/torneos";
	    }

		// Verificar si el usuario tiene una membresía activa
		Membresia_club membresia = membresiaClubService.obtenerPorUsuario(usuarioSesion.getId());
		if (membresia == null || membresia.getEstadoMembresiaId().getId() != 1) {
			ra.addFlashAttribute("error", "Tu membresía no está activa o no perteneces a ningún club.");
			return "redirect:/home/torneos/" + id;
		}

		Integer clubId = membresia.getClubId().getId();
		// Verificar si el club ya alcanzó el límite de 3 inscritos en este torneo
		boolean puedeInscribirMas = inscripcionesService.puedeInscribirMasParticipantes(id, clubId);
		if (!puedeInscribirMas) {
			ra.addFlashAttribute("error", "Tu club ya tiene 3 participantes inscritos en este torneo.");
			return "redirect:/home/torneos/" + id;
		}

		// Verificar si el usuario puede inscribirse (no ha participado en los últimos 5
		// días)
		boolean puedeInscribirse = inscripcionesService.puedeInscribirse(usuarioSesion.getId());
		if (!puedeInscribirse) {
			ra.addFlashAttribute("error",
					"No puedes inscribirte en este torneo. Debes esperar al menos 5 días desde tu última inscripción.");
			return "redirect:/home/torneos/" + id;
		}

		boolean yaInscrito = inscripcionesService.existeInscripcion(usuarioSesion.getId(), id);
		if (yaInscrito) {
			ra.addFlashAttribute("error", "Ya estas inscrito en este torneo.");
			return "redirect:/home/torneos" + id;
		}
		
		// Verificar si el número de inscripciones ya alcanzó el límite del torneo
		int inscritos = inscripcionesService.contarInscripcionesPorTorneo(clubId);
		if(inscritos >= torneo.getJugReq()) {
			ra.addFlashAttribute("error", "El torneo ya alcanzo el limite de jugadores permitidos.");
			return "redirect:home/torneos/" + id;
		}

		try {

			// Obtener la categoría del torneo
			int categoriaUsuario = usuarioSesion.getCategoriaId().getId();
			int categoriaTorneo = torneo.getCategoriaId().getId();

			// Verificar si la categoría del usuario coincide con la del torneo
			if (categoriaUsuario != categoriaTorneo) {
				ra.addFlashAttribute("error",
						"No puedes inscribirte a este torneo, ya que no pertenece a tu categoría.");
				return "redirect:/home/torneos/" + id;
			}

			Inscripciones newIncription = new Inscripciones();
			newIncription.setUsuarioId(usuarioSesion);
			newIncription.setTorneoId(torneo);
			newIncription.setEstadoInscripcionId(estadoInscripcionesService.listarID(2));

			inscripcionesService.guaradarInscripciones(newIncription);

			System.out.println("Se guardo correctamente la inscripcion con ID: " + newIncription.getId());

			ra.addFlashAttribute("inscripcionExitosa", true);
		} catch (Exception e) {
			e.printStackTrace();
			ra.addFlashAttribute("error", "Hubo un error al inscribir en el torneo");
		}

		return "redirect:/home/torneos/" + id;
	}

///////////////////////////////////////////////////////////////////////
///////////					CLUBS						//////////////
//////////////////////////////////////////////////////////////////////

	// VER CLUBES
	@GetMapping("/clubes")
	public String listarClubes(Model model) {
		List<Club> club = clubService.listarClub();
		model.addAttribute("club", club);
		return "home/club";
	}

	// VER INFORMACIÓN DEL CLUB
	@GetMapping("/clubes/{id}")
	public String listarClubId(Model model, HttpSession session, 
								@PathVariable("id") int id,
								@RequestParam(value = "page" ,defaultValue = "0") int page,
								@RequestParam(value = "size" ,defaultValue = "2") int size) {

		// Verifica si el usuario está logueado
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
		if (usuarioSesion == null) {
			// Si no está logueado, mostrar mensaje de cuenta no registrada
			model.addAttribute("mensajeCuenta", "Debe tener una cuenta registrada con nosotros.");
		} else {
			// Actualiza la sesión con la información más reciente del usuario
			Usuario usuarioActualizado = usuarioService.listarID(usuarioSesion.getId());
			session.setAttribute("usuario", usuarioActualizado);
			model.addAttribute("usuario", usuarioActualizado);

			// Verifica la membresía del usuario en el club
			Membresia_club membresia = membresiaClubService.obtenerPorUsuario(usuarioSesion.getId());

			if (membresia != null) {
				if (membresia.getClubId().getId() == id) {
					// El usuario ya está en el club
					if (membresia.getEstadoMembresiaId().getId() == 3) {
						// Si la membresía está en revisión
						model.addAttribute("mensajeSolicitudRevision", "Tu solicitud se encuentra en revisión.");
					} else {
						// Si ya pertenece al club
						model.addAttribute("mensajeMembresia", "Ya perteneces a este club.");
					}
				} else {
					// El usuario pertenece a otro club
					model.addAttribute("mensajeMembresia", "Ya perteneces a otro club. Actualmente estás inscrito en "
							+ membresia.getClubId().getNombre());
				}
			} else {
				// Verifica si el usuario tiene una solicitud pendiente en el club
				Membresia_club solicitudEnEspera = membresiaClubService.obtenerPorUsuarioYClub(usuarioSesion.getId(),
						id);
				if (solicitudEnEspera != null && solicitudEnEspera.getEstadoMembresiaId().getId() == 3) {
					// Solicitud en revisión
					model.addAttribute("mensajeSolicitudEnEspera",
							"Ya enviaste una solicitud a este club y está en revisión.");
				} else {
					// Si no tiene solicitud pendiente, verifica el estado de su cuenta
					if (usuarioActualizado.getEstadoUsuario().getId() == 1) {
						// Si la cuenta del usuario está aprobada
						model.addAttribute("mostrarBotonInscripcion", true);
					} else {
						// Si la cuenta del usuario no está aprobada
						model.addAttribute("mensajeEstadoUsuario",
								"Su cuenta no está aprobada para realizar esta acción.");
					}
				}
			}
		}

		 // Obtener la información del club
	    Club clubDet = clubService.listarID(id);
	    model.addAttribute("clubDet", clubDet);

	    // Obtener los jugadores activos paginados
	    Page<Membresia_club> jugadoresPage = membresiaClubService.listarJugadoresPorClubYEstadoPages(id, page, size);
	    model.addAttribute("jugadoresActivos", jugadoresPage.getContent());
	    model.addAttribute("currentPage", jugadoresPage.getNumber());
	    model.addAttribute("totalPages", jugadoresPage.getTotalPages());
	    model.addAttribute("size", jugadoresPage.getSize());

	    // Cantidad de usuarios en el club
	    int cantidadUsuarios = membresiaClubService.contarUsuariosEnClub(id);
	    model.addAttribute("cantidadUsuarios", cantidadUsuarios);

	    // Cantidad de victorias del club
	    Integer cantidadVictorias = membresiaClubService.contarVictoriasEnClub(id);
	    model.addAttribute("cantidadVictorias", cantidadVictorias);

	    return "home/clubDetalles";
	}

	// ENVIAR SOLICITUD A UN CLUB
	@PostMapping("/clubes/{id}/inscribirse")
	public String inscribirseClub(@PathVariable("id") int clubId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

		if (usuarioSesion == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "Debe iniciar sesión para inscribirse en un club.");
			return "redirect:/clubes/" + clubId;
		}

		// Verificar si el usuario ya pertenece a un club
		Membresia_club membresiaExistente = membresiaClubService.obtenerPorUsuario(usuarioSesion.getId());
		if (membresiaExistente != null) {
			redirectAttributes.addFlashAttribute("mensajeError",
					"Ya pertenece a un club, no puede inscribirse a otro.");
			return "redirect:/clubes/" + clubId;
		}

		// Crear una nueva membresía con estado "pendiente" (id = 3)
		Membresia_club nuevaMembresia = new Membresia_club();
		Club club = clubService.listarID(clubId);

		if (club == null) {
			redirectAttributes.addFlashAttribute("mensajeError", "El club seleccionado no existe.");
			return "redirect:/clubes";
		}

		nuevaMembresia.setUsuarioId(usuarioSesion);
		nuevaMembresia.setClubId(club);

		// Obtener el estado "pendiente" (id = 3)
		Estado_membresia estadoPendiente = estadoMembresiaService.listarID(3);
		nuevaMembresia.setEstadoMembresiaId(estadoPendiente);

		// Guardar la nueva membresía
		membresiaClubService.guardarMembresiaClub(nuevaMembresia);

		redirectAttributes.addFlashAttribute("mensajeExito", "Solicitud de inscripción enviada correctamente.");
		return "redirect:/home/clubes/" + clubId;
	}
///////////////////////////////////////////////////////////////////////
///////////				COMUNIDAD						//////////////
//////////////////////////////////////////////////////////////////////

	// VER COMUNIDAD
	@GetMapping("/comunidad")
	public String listarComunidad(@RequestParam(value = "tema", required = false) String tema,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size, Model model) {

		Page<Comunidad> comunidad;

		if (tema != null && !tema.isEmpty()) {
			comunidad = comunidadService.buscarPorTemaPaginado(tema, PageRequest.of(page, size));
		} else {
			comunidad = comunidadService.listarComunidadPaginado(PageRequest.of(page, size));
		}

		model.addAttribute("comunidad", comunidad);
		model.addAttribute("tema", tema);

		return "home/comunidad";
	}

	@GetMapping("/comunidad/addcomentario")
	public String verAddComentario(Model model, HttpSession session) {
		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
		if (usuarioSesion == null) {
			return "redirect:/login";
		}
		// Solo pasa la lista de temas al modelo, no es necesario crear un objeto
		// Comunidad
		model.addAttribute("temas",
				new String[] { "Robots de pelea", "Estrategias", "Nuevas tecnologías", "Competencias", "Otros" });

		return "home/addComentario";
	}

	@PostMapping("/comunidad/addcomentario")
	public String guardarComentario(@RequestParam("tema") String tema, @RequestParam("comentario") String comentario,
			@RequestParam("img") MultipartFile img, RedirectAttributes ra, HttpSession session) {

		Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
		if (usuarioSesion == null) {
			return "redirect:/login";
		}

		try {
			Comunidad com = new Comunidad();
			com.setUsuarioId(usuarioSesion);
			com.setTema(tema);
			com.setComentario(comentario);

			// Guardar la imagen en el sistema de archivos
			String imgPath = uploadService.saveImage(img);
			com.setImg(imgPath);

			// Guardar el comentario en la base de datos
			comunidadService.guardarComunidad(com);

			ra.addFlashAttribute("success", "El comentario se agregó correctamente.");
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Hubo un problema al agregar el comentario.");
		}

		return "redirect:/home/comunidad";
	}

///////////////////////////////////////////////////////////////////////
///////////				CLASIFICACION					//////////////
//////////////////////////////////////////////////////////////////////

	// VER TABLA DE MÉRITO
	@GetMapping("/clasificacion")
	public String listarTablaMerito(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, Model model) {

		PageRequest pageable = PageRequest.of(page, size);

		Page<Victorias> victoriasPage = victoriasService.listarVictoriasDescPageable(pageable);

		model.addAttribute("victorias", victoriasPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPages", victoriasPage.getTotalPages());
		model.addAttribute("totalElements", victoriasPage.getTotalElements());

		return "home/clasificacion";
	}

	// VER TOP3
	@GetMapping("clasificacion/top3")
	public String listarTablaMeritoTop3(Model model) {

		List<Victorias> victorias = victoriasService.listarTop3Victorias();
		model.addAttribute("victorias", victorias);

		return "home/top3";
	}

///////////////////////////////////////////////////////////////////////
///////////					PERFIL						//////////////
//////////////////////////////////////////////////////////////////////

	// VER PERFIL
	@GetMapping("/perfil")
	public String perfilUsuario(Model model, HttpSession session) {

		System.out.println("Accessing /perfil");

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/login"; // Redirigir al login si no hay usuario en sesión
		}
		model.addAttribute("usuario", usuario); // Pasar la información del usuario al modelo
		return "home/verPerfil";

	}

	// EDITAR USUARIO
	@GetMapping("/perfil/editar")
	public String ActualizarPerfilUsuario(Model model, HttpSession session) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/login"; // Redirigir al login si no hay usuario en sesión
		}

		model.addAttribute("usuario", usuario);

		return "home/perfilUpdate";
	}

	// Actualización del perfil
	@PostMapping("/perfil/editar")
	public String actualizarPerfil(@RequestParam("username") String username,
			@RequestParam(value = "foto_robot", required = false) MultipartFile fotoRobot, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Usuario usuario = (Usuario) session.getAttribute("usuario");
		if (usuario == null) {
			return "redirect:/login";
		}

		// Actualizar el username
		usuario.setUsername(username);

		// Actualizar la foto del robot si se proporcionó una nueva
		if (fotoRobot != null && !fotoRobot.isEmpty()) {
			try {
				String nombreImagen = uploadService.saveImage(fotoRobot);
				usuario.setFoto_perfil(nombreImagen);
			} catch (IOException e) {
				redirectAttributes.addFlashAttribute("error", "Error al subir la imagen");
				return "redirect:/perfil/editar";
			}
		}

		// Guardar cambios en la base de datos
		usuarioService.guardarUsuario(usuario);

		// Actualizar la sesión
		session.setAttribute("usuario", usuario);

		redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
		return "redirect:/home/perfil";
	}

	// VER INFORMACIÓN DEL PERFIL DE UN USUARIO X
	@GetMapping("/TopUsuario/{id}")
	public String listarTopUsuario(@PathVariable("id") Integer id, Model model) {

		Usuario usuario = usuarioService.listarID(id);
		if (usuario != null) {
			model.addAttribute("usuario", usuario);
			return "home/detalleUsuario";
		}
		return "redirect:/home";
	}

}
