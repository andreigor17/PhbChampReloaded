
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filename = request.getPathInfo().substring(1);

        File file = null;

        file = new File(request.getPathInfo());
        getFileCopy(response, filename, file);
    }

    private void getFileCopy(HttpServletResponse response, String filename, File file) throws IOException {
        response.setHeader("Content-Type", getServletContext().getMimeType(filename));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        if (file.exists() && !file.isDirectory()) {
            Files.copy(file.toPath(), response.getOutputStream());
        }
    }

}
