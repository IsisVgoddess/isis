package springapp.tests.command;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.services.background.BackgroundExecutionService;
import org.springframework.stereotype.Service;

import springapp.dom.customer.CustomerRepository;
import springapp.dom.email.Email;
import springapp.dom.email.EmailRepository;

@Service
public class CommandDemoBean {
	
	@Inject EmailRepository emailRepository;
	@Inject CustomerRepository customerRepository;
	@Inject BackgroundExecutionService backgroundService;
	
	@Action
	public void verifyCustomerEmails() {
		
		for(Email email: emailRepository.findByVerified(false)) {
			backgroundService.execute(email).startVerificationProcess();
		}
	    
	}

}
