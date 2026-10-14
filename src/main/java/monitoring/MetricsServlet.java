package monitoring;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/metrics")
public class MetricsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/plain; version=0.0.4; charset=utf-8");
        String version = System.getenv().getOrDefault("APP_VERSION", "dev").replaceAll("[^a-zA-Z0-9_.-]", "_");
        var out = res.getWriter();
        out.println("# TYPE shoppingcart_requests_total counter");
        for (int code : new int[]{200,302,400,401,403,404,500,503}) {
            var count = RequestMetrics.COUNTS.get(code);
            out.printf("shoppingcart_requests_total{status=\"%d\",version=\"%s\"} %d%n", code, version, count == null ? 0 : count.sum());
        }
        for (var entry : RequestMetrics.COUNTS.entrySet()) {
            int code = entry.getKey();
            if (code != 200 && code != 302 && code != 400 && code != 401 && code != 403 && code != 404 && code != 500 && code != 503)
                out.printf("shoppingcart_requests_total{status=\"%d\",version=\"%s\"} %d%n", code, version, entry.getValue().sum());
        }
        out.println("# TYPE shoppingcart_request_duration_seconds_sum counter");
        out.printf(java.util.Locale.ROOT,"shoppingcart_request_duration_seconds_sum{version=\"%s\"} %.9f%n",version,RequestMetrics.DURATION.sum()/1e9);
    }
}
