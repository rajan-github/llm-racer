import com.rajan.llmracer.controller.GenerateController;
import com.rajan.llmracer.service.SimpleGenerateService;
import io.javalin.Javalin;


import java.util.concurrent.atomic.AtomicLong;

public class LlmRacer {
    private static final AtomicLong totalRequests = new AtomicLong();
    private static final AtomicLong successfulRequests = new AtomicLong();

    public static void main(String[] args) {
        System.out.println("Welcome to the LlmRacer!");
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.plugins.enableDevLogging();
        }).start(8080);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            app.stop();
            System.out.println("Server stopped and port freed");
        }));

        final var generateService = new SimpleGenerateService();
        final var generateController = new GenerateController(generateService);
        generateController.register(app);

    }
}
