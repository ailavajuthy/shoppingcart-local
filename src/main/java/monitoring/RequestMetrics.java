package monitoring;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
public class RequestMetrics implements Filter {
    static final ConcurrentHashMap<Integer, LongAdder> COUNTS = new ConcurrentHashMap<>();
    static final LongAdder DURATION = new LongAdder();
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        res.setHeader("X-App-Version", System.getenv().getOrDefault("APP_VERSION", "dev"));
        if (path.startsWith("/health/") || path.equals("/metrics") || path.equals("/version")) { chain.doFilter(req, res); return; }
        long start = System.nanoTime();
        boolean failed = false;
        try {
            if ("true".equalsIgnoreCase(System.getenv("APP_FAULT"))) {
                res.setStatus(503); res.setContentType("application/json");
                res.getWriter().print("{\"error\":\"deliberately faulty release\"}");
            } else { chain.doFilter(req, res); }
        } catch (IOException | ServletException | RuntimeException e) { failed = true; throw e; }
        finally {
            int status = failed ? 500 : res.getStatus();
            COUNTS.computeIfAbsent(status, k -> new LongAdder()).increment();
            DURATION.add(System.nanoTime() - start);
        }
    }
}
