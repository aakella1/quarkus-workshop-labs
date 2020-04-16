package org.acme.people.stream;

import io.reactivex.Flowable;
import javax.enterprise.context.ApplicationScoped;
import org.acme.people.utils.CuteNameGenerator;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class NameGenerator {

    //Instruct Reactive Messaging to dispatch the items from returned stream to generated-name
    @Outgoing("generated-name")      
    //The method returns a RX Java 2 stream (Flowable) emitting a random name every 5 seconds 
    public Flowable<String> generate() {  
        return Flowable.interval(5, TimeUnit.SECONDS)
                .map(tick -> CuteNameGenerator.generate());
    }

}