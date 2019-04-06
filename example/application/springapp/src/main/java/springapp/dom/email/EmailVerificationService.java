package springapp.dom.email;

import java.util.concurrent.CompletableFuture;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class EmailVerificationService {
	
	//TODO #boilerplate
    @Setter(onMethod = @__({@PersistenceContext})) private EntityManager em;

    // -- BUSINESS LOGIC
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void startVerificationProcess(Email email) {
		
		log.info("start process for {}", toInfo(email));
		
		// we probably want 'email' passed over by the caller to be 'attached', but its 'detached'
		email = em.merge(email); //TODO #boilerplate
		
		email.setVerified(true);
		
		// why is the 'commit' not handled by the current transaction?
		em.persist(email); 	//TODO #boilerplate
		
		log.info("end process for {}", toInfo(email));
	}
	

	@Async @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public CompletableFuture<Void> startVerificationProcessAsync(Email email) {
		
		log.info("start process for {}", toInfo(email));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// we probably want 'email' passed over by the caller to be 'attached', but its 'detached'
		email = em.merge(email); //TODO #boilerplate
		
		email.setVerified(true);
		
		// why is the 'commit' not handled by the current transaction?
		em.persist(email); 	//TODO #boilerplate
		
		log.info("end process for {}", toInfo(email));
		
		return new AsyncResult<Void>(null).completable();
	}

	// -- HELPER
	
	private String toInfo(Email email) {
		return new StringBuilder()
		.append("email.verified=")
		.append(email.isVerified())
		.toString();
	}
	
}
