package com.dbp.democarpultec.auth.listener;

import com.dbp.democarpultec.auth.event.VerificationCodeSubmittedEvent;
import com.dbp.democarpultec.email.EmailService;
import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VerificationCodeSubmittedListener {

	private final UserService userService;
	private final EmailService emailService;

	@EventListener
	@Transactional
	public void handleVerificationCodeSubmitted(VerificationCodeSubmittedEvent event) {
		User user = userService.findEntityByVerificationCode(event.verificationCode());
		userService.verifyUserByCode(event.verificationCode());
		emailService.sendWelcomeEmail(user);
	}
}