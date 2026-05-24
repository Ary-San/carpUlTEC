package com.dbp.democarpultec.email;

import com.dbp.democarpultec.ride.domain.Ride;
import com.dbp.democarpultec.ride.domain.RideDirection;
import com.dbp.democarpultec.ride.domain.RideStatus;
import com.dbp.democarpultec.user.domain.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${spring.application.name:CarpulTEC}")
	private String brandName;

	@Value("${app.backend.base-url:http://localhost:8080}")
	private String backendBaseUrl;

	@Value("${frontend.url:http://localhost:3000}")
	private String frontendUrl;

	@Value("${app.mail.default-trip-duration-minutes:45}")
	private long defaultTripDurationMinutes;

	@Async
	public void sendVerificationEmail(User user, String verificationCode) {
		sendTemplatedEmail(user.getEmail(), "Verifica tu cuenta en " + brandName, "Verify", verificationVariables(user, verificationCode));
	}

	@Async
	public void sendWelcomeEmail(User user) {
		sendTemplatedEmail(user.getEmail(), "Bienvenido a " + brandName, "Welcome", welcomeVariables(user));
	}

	@Async
	public void sendRideScheduleEmail(User passenger, Ride ride) {
		sendTemplatedEmail(passenger.getEmail(), "Tu viaje en " + brandName + " está confirmado", "RideSchedule", rideScheduleVariables(passenger, ride));
	}

	@Async
	public void sendRideStartedEmail(Ride ride, Collection<User> recipients) {
		for (User recipient : recipients) {
			sendTemplatedEmail(recipient.getEmail(), "Tu viaje ya inició", "RideStart", rideStatusVariables(recipient, ride, "El viaje ya está en curso", "El conductor ya inició el recorrido."));
		}
	}

	@Async
	public void sendRideCompletedEmail(Ride ride, Collection<User> recipients) {
		for (User recipient : recipients) {
			sendTemplatedEmail(recipient.getEmail(), "Tu viaje finalizó", "RideEnd", rideStatusVariables(recipient, ride, "El viaje terminó", "El recorrido ha finalizado correctamente."));
		}
	}

	private void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
		try {
			String senderEmail = Objects.requireNonNull(fromEmail, "fromEmail");
			String senderName = Objects.requireNonNull(brandName, "brandName");
			String recipientEmail = Objects.requireNonNull(to, "to");
			String mailSubject = Objects.requireNonNull(subject, "subject");
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
			Context context = new Context();
			context.setVariables(variables);
			helper.setFrom(senderEmail, senderName);
			helper.setTo(recipientEmail);
			helper.setSubject(mailSubject);
			helper.setText(Objects.requireNonNull(templateEngine.process(templateName, context), "template body"), true);
			mailSender.send(message);
		} catch (MailException ex) {
			log.warn("No se pudo enviar el correo a {}", to, ex);
		} catch (Exception ex) {
			log.warn("No se pudo preparar el correo a {}", to, ex);
		}
	}

	private Map<String, Object> welcomeVariables(User user) {
		Map<String, Object> variables = baseVariables(user, "Cuenta verificada");
		variables.put("message", "Tu cuenta fue verificada correctamente. Ya puedes iniciar sesión y usar los viajes.");
		variables.put("actionLabel", "Abrir la app");
		variables.put("actionUrl", frontendUrl);
		variables.put("secondaryActionLabel", null);
		variables.put("secondaryActionUrl", null);
		variables.put("details", orderedDetails(Map.of(
				"Correo", user.getEmail(),
				"Estado", "Verificada",
				"Roles", user.getRoles() == null ? "-" : user.getRoles().toString()
		)));
		return variables;
	}

	private Map<String, Object> verificationVariables(User user, String verificationCode) {
		Map<String, Object> variables = baseVariables(user, "Verifica tu cuenta");
		variables.put("message", "Usa este código para completar la verificación de tu correo.");
		variables.put("verificationCode", verificationCode);
		variables.put("actionLabel", "Verificar cuenta");
		variables.put("actionUrl", backendBaseUrl + "/api/auth/verify/" + verificationCode);
		variables.put("secondaryActionLabel", "Abrir la app");
		variables.put("secondaryActionUrl", frontendUrl);
		variables.put("details", orderedDetails(Map.of(
				"Correo", user.getEmail(),
				"Estado", "Pendiente de verificación"
		)));
		return variables;
	}

	private Map<String, Object> rideScheduleVariables(User passenger, Ride ride) {
		Map<String, Object> variables = baseVariables(passenger, "Tu viaje está confirmado");
		variables.put("message", "Te uniste correctamente al viaje. Revisa los detalles principales para llegar a tiempo.");
		variables.put("actionLabel", "Abrir la app");
		variables.put("actionUrl", frontendUrl);
		variables.put("details", orderedDetails(Map.of(
				"Origen", ride.getOrigin(),
				"Destino", ride.getDestination(),
				"Salida", formatDateTime(ride.getScheduledAt()),
				"Estado", statusLabel(ride.getStatus()),
				"Llegada estimada", formatDateTime(ride.getScheduledAt().plusMinutes(defaultTripDurationMinutes)),
				"Dirección", directionLabel(ride.getDirection())
		)));
		return variables;
	}

	private Map<String, Object> rideStatusVariables(User recipient, Ride ride, String title, String message) {
		Map<String, Object> variables = baseVariables(recipient, title);
		variables.put("message", message);
		variables.put("actionLabel", "Abrir la app");
		variables.put("actionUrl", frontendUrl);
		variables.put("details", orderedDetails(Map.of(
				"Origen", ride.getOrigin(),
				"Destino", ride.getDestination(),
				"Salida", formatDateTime(ride.getScheduledAt()),
				"Estado", statusLabel(ride.getStatus()),
				"Llegada estimada", formatDateTime(ride.getScheduledAt().plusMinutes(defaultTripDurationMinutes)),
				"Dirección", directionLabel(ride.getDirection())
		)));
		return variables;
	}

	private Map<String, Object> baseVariables(User user, String title) {
		Map<String, Object> variables = new LinkedHashMap<>();
		variables.put("appName", brandName);
		variables.put("title", title);
		variables.put("recipientName", fullName(user));
		variables.put("frontendUrl", frontendUrl);
		return variables;
	}

	private Map<String, String> orderedDetails(Map<String, String> details) {
		Map<String, String> ordered = new LinkedHashMap<>();
		ordered.putAll(details);
		return ordered;
	}

	private String fullName(User user) {
		return (user.getName() + " " + user.getLastName()).trim();
	}

	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime == null ? "-" : DATE_TIME_FORMATTER.format(dateTime);
	}

	private String statusLabel(RideStatus status) {
		return switch (status) {
			case PLANNED -> "Programado";
			case ACTIVE -> "En curso";
			case COMPLETED -> "Finalizado";
		};
	}

	private String directionLabel(RideDirection direction) {
		return switch (direction) {
			case TO_UNIVERSITY -> "Hacia la universidad";
			case FROM_UNIVERSITY -> "Salida desde la universidad";
		};
	}
}