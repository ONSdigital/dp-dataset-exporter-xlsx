package dp;

import com.github.onsdigital.logging.v2.DPLogger;
import com.github.onsdigital.logging.v2.Logger;
import com.github.onsdigital.logging.v2.LoggerImpl;
import com.github.onsdigital.logging.v2.LoggingException;
import com.github.onsdigital.logging.v2.config.Builder;
import com.github.onsdigital.logging.v2.config.ShutdownHook;
import com.github.onsdigital.logging.v2.nop.NopShutdownHook;
import com.github.onsdigital.logging.v2.serializer.JacksonLogSerialiser;
import com.github.onsdigital.logging.v2.serializer.LogSerialiser;
import com.github.onsdigital.logging.v2.storage.LogStore;
import com.github.onsdigital.logging.v2.storage.MDCLogStore;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static dp.logging.LogEvent.info;


@SpringBootApplication
public class Main {

    private static final String HUMAN_LOG = "HUMAN_LOG";

    public static void main(String[] args) {
        initLog();
        info().log("starting service");
        SpringApplication.run(Main.class, args);
        info().log("service started");
    }

    private static void initLog(){
        LogSerialiser serialiser = getLogSerialiser();
        LogStore store = new MDCLogStore(serialiser);
        Logger logger = new LoggerImpl("dp-dataset-exporter-xlsx");
        ShutdownHook shutdownHook = new NopShutdownHook();

        try {
            DPLogger.init(new Builder()
                    .serialiser(serialiser)
                    .logStore(store)
                    .logger(logger)
                    .shutdownHook(shutdownHook)
                    .dataNamespace("dp-dataset-exporter-xlsx.data")
                    .create());
        } catch (LoggingException ex) {
            System.err.println(ex);
            System.exit(1);
        }

    }

    private static LogSerialiser getLogSerialiser() {
        if(Integer.valueOf(System.getenv(HUMAN_LOG)) == 1){
            return new JacksonLogSerialiser(true);
        }
        return new JacksonLogSerialiser(false);
    }

}
