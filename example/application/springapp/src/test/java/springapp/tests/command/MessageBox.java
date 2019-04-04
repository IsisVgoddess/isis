package springapp.tests.command;

import java.util.Collections;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.collections._Multimaps;
import org.springframework.stereotype.Service;

import lombok.val;

@Service
public class MessageBox {

	private final _Multimaps.SetMultimap<String, String> store = _Multimaps.newSetMultimap();
	
	public void send(String key, String msg) {
		synchronized (store) {
			store.putElement(key, msg);	
		}
	}
	
	public Stream<String> streamMessages(String key) {
		val messages = store.getOrDefault(key, Collections.emptySet());
		return messages.stream();
	}
	
}
