package edu.escuelaing.arep;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import edu.escuelaing.arep.annotations.GetMapping;
import edu.escuelaing.arep.annotations.RequestParam;
import edu.escuelaing.arep.annotations.RestController;

/**
 * The SimpleWebServer class represents a basic multithreaded web server
 * that listens on a specified port and serves static files or handles
 * custom RESTful services. The server can handle multiple clients
 * concurrently using a thread pool.
 */
public class SimpleWebServer {
    static final int PORT = 8080;
    public static final String WEB_ROOT = "src/main/java/edu/escuelaing/arep/resources/";
    private static boolean running = true;
    static Map<String, Method> getMappings = new HashMap<>();
    static Map<String, Method> postMappings = new HashMap<>();
    static Map<String, Object> controllers = new HashMap<>();

    /**
     * The main entry point of the SimpleWebServer. It sets up the server
     * to listen on the specified port, initializes REST services, and
     * handles incoming client connections.
     *
     * @param args command-line arguments (not used).
     * @throws IOException if an I/O error occurs while opening the server socket.
     */
    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Ready to receive on port " + PORT + "...");
        initializeControllers();
        while (running) {
            Socket clientSocket = serverSocket.accept();
            threadPool.submit(new ClientHandler(clientSocket));
        }
        serverSocket.close();
        threadPool.shutdown();
    }

    /**
     * Initializes the controllers by scanning the specified package for classes
     * annotated with @RestController. It registers each found controller class
     * to handle RESTful service requests.
     *
     * @throws ReflectiveOperationException if an error occurs while reflecting on the controller classes.
     * @throws IOException if an error occurs while scanning the package for classes.
     */
    private static void initializeControllers() throws ReflectiveOperationException, IOException {
        String packageName = "edu.escuelaing.arep";
        List<Class<?>> controllerClasses = findClassesWithAnnotation(packageName, RestController.class);
        
        for (Class<?> controllerClass : controllerClasses) {
            registerController(controllerClass);
        }
    }

    /**
     * Registers a controller class by creating an instance of it and mapping
     * its methods annotated with @GetMapping to their corresponding HTTP paths.
     *
     * @param controllerClass the class to be registered as a controller.
     * @throws ReflectiveOperationException if an error occurs while creating an instance of the controller class
     *                                      or accessing its methods.
     */
    private static void registerController(Class<?> controllerClass) throws ReflectiveOperationException {
        Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
        controllers.put(controllerClass.getName(), controllerInstance);

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                getMappings.put(getMapping.value(), method);
            }
        }
    }

    /**
     * Scans the specified package for classes annotated with the specified annotation.
     *
     * @param packageName the name of the package to scan.
     * @param annotation the annotation to look for in the classes.
     * @return a list of classes that are annotated with the specified annotation.
     * @throws ClassNotFoundException if a class cannot be found during the scan.
     * @throws IOException if an error occurs while reading from the file system.
     */
    private static List<Class<?>> findClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        URL resource = classLoader.getResource(path);
        File directory = new File(resource.getFile());
        List<Class<?>> classes = new ArrayList<>();
        if (directory.exists()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    String className = packageName + '.' + file.substring(0, file.length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotation)) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Stops the server by setting the running flag to false.
     * This method is called to gracefully shut down the server.
     */
    public static void stop() {
        running = false;
    }
}


/**
 * The ClientHandler class implements Runnable and is responsible for
 * handling individual client connections to the SimpleWebServer. It
 * processes HTTP requests, serves static files, and delegates requests
 * to registered RESTful services.
 */
class ClientHandler implements Runnable {
    private Socket clientSocket;

    /**
     * Constructs a new ClientHandler for the given client socket.
     *
     * @param socket the client socket to handle.
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * The run method is invoked when the ClientHandler is executed by a thread.
     * It processes the client's HTTP request, determines the type of request,
     * and calls the appropriate method to handle it.
     */
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null)
                return;
            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];

            printRequestLine(requestLine, in);
            if (fileRequested.startsWith("/app")) {
                handleAppRequest(method, fileRequested, out);
            } else {
                if (method.equals("GET")) {
                    handleGetRequest(fileRequested, out, dataOut);
                } else if (method.equals("POST")) {
                    handlePostRequest(fileRequested, out, dataOut);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prints the request line and headers from the client's HTTP request to the console.
     *
     * @param requestLine the initial request line (e.g., "GET /index.html HTTP/1.1").
     * @param in the BufferedReader for reading the client's request headers.
     */    
    private void printRequestLine(String requestLine, BufferedReader in) {
        System.out.println("Request line: " + requestLine);
        String inputLine;
        try {
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Header: " + inputLine);
                if (in.ready()) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a GET request by serving a static file from the server's root directory.
     * If the file is found, it is sent to the client along with appropriate HTTP headers.
     * If the file is not found, a 404 error message is returned.
     *
     * @param fileRequested the file requested by the client.
     * @param out the PrintWriter to send the HTTP headers to the client.
     * @param dataOut the BufferedOutputStream to send the file data to the client.
     * @throws IOException if an I/O error occurs while reading the file or sending the response.
     */    
    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        File file = new File(SimpleWebServer.WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (file.exists()) {
            byte[] fileData = readFileData(file, fileLength);
            out.println("HTTP/1.1 200 OK");
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println();
            out.flush();
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-type: text/html");
            out.println();
            out.flush();
            out.println("<html><body><h1>File Not Found</h1></body></html>");
            out.flush();
        }
    }

    /**
     * Handles a POST request by reading the request payload and returning a simple HTML
     * response that includes the received data.
     *
     * @param fileRequested the file requested by the client (not used in this method).
     * @param out the PrintWriter to send the HTTP headers and response to the client.
     * @param dataOut the BufferedOutputStream to send the response body to the client.
     * @throws IOException if an I/O error occurs while reading the input or sending the response.
     */
    private void handlePostRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                payload.append(line);
            }
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Content-type: text/html");
        out.println();
        out.println("<html><body><h1>POST data received:</h1>");
        out.println("<p>" + payload.toString() + "</p>");
        out.println("</body></html>");
        out.flush();
    }

    /**
     * Handles an application-specific HTTP request by determining the request method (GET),
     * extracting any query parameters, and invoking the appropriate controller method
     * that corresponds to the requested path.
     *
     * @param method the HTTP method of the request (e.g., GET).
     * @param path the request path (e.g., /app/hello?name=John).
     * @param out the PrintWriter used to send the HTTP response back to the client.
     */
    private void handleAppRequest(String method, String path, PrintWriter out) {
        if ("GET".equalsIgnoreCase(method)) {
            String[] pathParts = path.split("\\?");
            String basePath = pathParts[0];
            Map<String, String> queryParams = new HashMap<>();

            if (pathParts.length > 1) {
                String queryString = pathParts[1];
                String[] pairs = queryString.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    } else {
                        queryParams.put(keyValue[0], "");
                    }
                }
            }

            Method handlerMethod = SimpleWebServer.getMappings.get(basePath);
            if (handlerMethod != null) {
                try {
                    Object controller = SimpleWebServer.controllers.get(handlerMethod.getDeclaringClass().getName());
                    Object response = invokeControllerMethod(handlerMethod, controller, queryParams);
                    System.out.println("Se invoca el servicio: " +handlerMethod.getDeclaringClass().getName()+" y la respuesta es: "+response);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-type: text/plain");
                    out.println();
                    out.println(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    out.println("HTTP/1.1 500 Internal Server Error");
                }
            } else {
                out.println("HTTP/1.1 404 Not Found");
            }
        } else {
            out.println("HTTP/1.1 405 Method Not Allowed");
        }
        out.flush();
    }

    /**
     * Invokes the appropriate controller method that corresponds to the provided HTTP request.
     * It binds query parameters to method parameters based on the @RequestParam annotation
     * and converts parameter values to the required types.
     *
     * @param handlerMethod the method to be invoked, which corresponds to the requested path.
     * @param controller the instance of the controller class containing the method.
     * @param queryParams a map of query parameters extracted from the request URL.
     * @return the result of invoking the controller method, which is typically a response to be sent back to the client.
     * @throws IllegalAccessException if the controller method is inaccessible.
     * @throws InvocationTargetException if the controller method throws an exception.
     */
    private Object invokeControllerMethod(Method handlerMethod, Object controller, Map<String, String> queryParams) throws IllegalAccessException, InvocationTargetException {
        Parameter[] parameters = handlerMethod.getParameters();
        Object[] args = new Object[parameters.length];
    
        for (int i = 0; i < parameters.length; i++) {
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String paramName = requestParam.value();
                String defaultValue = requestParam.defaultValue();
                String paramValue = queryParams.get(paramName);
    
                // Si el parámetro no está en queryParams o está presente pero es vacío, usar el valor por defecto
                if (paramValue == null || paramValue.isEmpty()) {
                    paramValue = defaultValue;
                }
    
                Class<?> paramType = parameters[i].getType();
                if (paramType == int.class) {
                    args[i] = Integer.parseInt(paramValue);
                } else {
                    args[i] = paramValue; 
                }
            }
        }
    
        return handlerMethod.invoke(controller, args);
    }
      
    /**
     * Determines the MIME type of the requested file based on its extension.
     *
     * @param fileRequested the file requested by the client.
     * @return the MIME type of the file.
     */
    String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html"))
            return "text/html";
        else if (fileRequested.endsWith(".css"))
            return "text/css";
        else if (fileRequested.endsWith(".js"))
            return "application/javascript";
        else if (fileRequested.endsWith(".png"))
            return "image/png";
        else if (fileRequested.endsWith(".jpg"))
            return "image/jpeg";
        return "text/plain";
    }

    /**
     * Reads the contents of a file into a byte array.
     *
     * @param file the file to be read.
     * @param fileLength the length of the file in bytes.
     * @return a byte array containing the file's data.
     * @throws IOException if an I/O error occurs while reading the file.
     */
    byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }
        return fileData;
    }
}
