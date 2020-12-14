package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@RestController
public class Application {
    // FC HTTP function:
    // This mapping route the requests with the following FC HTTP trigger path to this fcHome handler
    // CustomContainerDemo: FC service name
    // java-springboot-http: FC function name
    @RequestMapping("/2016-08-15/proxy/CustomContainerDemo/java-springboot-http/")
    public String fcHome(@RequestHeader Map<String, String> headers) {
        // FC context parameters are passed in as HTTP headers with prefix x-fc-*
        // A temporary STS credential to access Alibaba Cloud services can be obtained from context headers with the following header keys:
        //   x-fc-access-key-id
        //   x-fc-access-key-secret
        //   x-fc-security-token can be used
        //
        // See https://www.alibabacloud.com/help/doc-detail/132044.html#common-headers for the complete headers list
        String fcRequestID = headers.get("x-fc-request-id");
        return "Hello Spring Boot, from FC HTTP function!\nPowered by FunctionCompute custom-container runtime\n"
                + "RequestID: " + fcRequestID + "\n";
    }

    @RequestMapping("/stress")
    public String stress(@RequestHeader Map<String, String> headers) {
        long start = System.currentTimeMillis();
        fibonacci(40);
        long end = System.currentTimeMillis();
        long elapse = end - start;
        return String.format("Time elapse: %d ms", elapse);
    }

    private int fibonacci(int i) {
        if (i == 1 || i == 2) {
            return 1;
        } else {
            return fibonacci(i - 1) + fibonacci(i - 2);
        }
    }

    // FC Event function:
    // Invoke handler: /invoke will be called when the FC function responds to an event (e.g. API call or OSS PutObject)
    @PostMapping("/invoke")
    public String fcEventInvoke(@RequestHeader Map<String, String> headers, @RequestBody String event) {
        String fcRequestID = headers.get("x-fc-request-id");
        System.out.println("Invoke finished, request ID: " + fcRequestID);
        return "Hello Spring Boot, from FC Event Function!\nPowered by FunctionCompute custom-container runtime\n"
                + "RequestID: " + fcRequestID + "\nEcho event: " + event + "\n";
    }

    // FC Event function:
    // FC Initializer: /initialize will be called when the FC functions are initialized by
    // API calls, Function updates or FC internal system upgrades
    @PostMapping("/initialize")
    public String fcEventInitialize(@RequestHeader Map<String, String> headers) {
        String fcRequestID = headers.get("x-fc-request-id");
        System.out.println("Initialized, request ID: " + fcRequestID);
        return "Hello Spring Boot, from FC Event function initializer!\nPowered by FunctionCompute custom-container runtime\n"
                + "RequestID: " + fcRequestID + "\n";
    }

    @RequestMapping("/")
    public String home() {
        return "Hello Docker World";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
