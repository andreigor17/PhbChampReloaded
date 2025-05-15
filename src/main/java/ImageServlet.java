
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * The Image servlet for serving from absolute path.
 *
 * @author andre
 *
 */
@WebServlet("/image/*")
public class ImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo(); 
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Arquivo não especificado.");
            return;
        }

        File file = new File(pathInfo); 
        if (!file.exists() || file.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Arquivo não encontrado.");
            return;
        }

        response.setContentType(getServletContext().getMimeType(file.getName()));
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(file.toPath(), out);
        }
    }
}
