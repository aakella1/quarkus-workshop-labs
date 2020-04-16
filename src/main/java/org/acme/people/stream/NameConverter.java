package org.acme.people.stream;

import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import io.smallrye.reactive.messaging.annotations.Broadcast;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;


@ApplicationScoped
public class NameConverter {

    private static final String[] honorifics = {"Mr.", "Mrs.", "Sir", "Madam", "Lord", "Lady", "Dr.", "Professor", "Vice-Chancellor", "Regent", "Provost", "Prefect"};

    //Indicates that the method consumes the items from the `names`` topic
    @Incoming("names")       
    //Indicates that the objects returned by the method are sent to the `my-data-stream` stream        
    @Outgoing("my-data-stream")     
    //Indicates that the item are dispatched to all *subscribers* 
    @Broadcast    
    @Counted(name = "convertedNames", description = "How many names have been converted.") 
    @Timed(name = "converter", description = "A measure how long it takes to convert names.", unit = MetricUnits.MILLISECONDS)                    
    public String process(String name) {
        String honorific = honorifics[(int)Math.floor(Math.random() * honorifics.length)];
        return honorific + " " + name;
    }
}

