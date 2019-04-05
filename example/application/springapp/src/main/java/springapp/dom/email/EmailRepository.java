package springapp.dom.email;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends CrudRepository<Email, Long> {
	
	List<Email> findByVerified(boolean verified);
	long countByVerified(boolean verified);
	
}
