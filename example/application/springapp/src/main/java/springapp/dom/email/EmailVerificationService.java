package springapp.dom.email;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class EmailVerificationService {

    // -- BUSINESS LOGIC
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void startVerificationProcess(Email email) {
		
		log.info("start process for {}", toInfo(email));
		
		email.setVerified(true);
		
		log.info("end process for {}", toInfo(email));
	}
	

	@Async @Transactional(propagation = Propagation.REQUIRES_NEW)
	public CompletableFuture<Void> startVerificationProcessAsync(Email email) {
		
		log.info("start process for {}", toInfo(email));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		email.setVerified(true);
		
		log.info("end process for {}", toInfo(email));
		
		return new AsyncResult<Void>(null).completable();
	}

	// -- HELPER
	
	private String toInfo(Email email) {

		val sb = new StringBuilder();
		
		//TODO what transaction are we currently in, if any?
		
		sb
		.append(", email.verified=")
		.append(email.isVerified());
		
		return sb.toString();
	}
	
}
