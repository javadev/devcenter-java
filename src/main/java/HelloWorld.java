import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import com.github.underscore.lodash.U;

public class HelloWorld extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setHeader("Content-Type", "text/javascript");
        resp.setHeader("Cache-Control", "no-cache, must-revalidate");
        resp.setHeader("Server", "Microsoft-IIS/10.0");
        resp.setCharacterEncoding("UTF-8");
        final String callback = req.getParameter("callback");
        final String url = req.getParameter("url");
        StringBuilder builder = new StringBuilder();
        builder.append(callback).append("(\n");
        final Map<String, Object> json = new LinkedHashMap<>();
        json.put("server", "xml-json.herokuapp.com");
        json.put("remote_host", req.getRemoteAddr());
        json.put("url", url);
        final List<String> html = new ArrayList<>();
        U.FetchResponse response = U.fetch(url);
        if (response.getStatus() == 301) {
            response = U.fetch(response.getHeaderFields().get("Location").get(0));
        }
        if (response.getStatus() == 200) {
            html.add(response.text());
        } else {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("message", "Error " + response.getStatus() + " while loading url: " + url);
            html.add(url.endsWith("xml") ? U.toXml(error) : U.toJson(error));
        }
        json.put("html", html);
        builder.append(U.toJson(json)).append("\n)");
        resp.setHeader("Content-Length", "" + builder.length());
        resp.getWriter().print(builder.toString());
    }

    public static void main(String[] args) throws Exception{
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new HelloWorld()),"/*");
        server.start();
        server.join();   
    }
}
