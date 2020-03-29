package com.http.httpfs;

import com.http.network.file.HttpReader;

import java.io.*;
import java.net.*;


public class httpfs {

    private static String MAINPATH = System.getProperty("user.dir") + "/src/com/http/httpfs";
    private static int PORT = 8080;
    private static boolean verbose = false; //TODO

    private ServerSocket client;
    private String filePath;
    private String headers = "";
    private int postContentLength = 0;

    private final String CODE_OK = "HTTP/1.0 200 OK\n";
    private final String CODE_NOT_FOUND = "HTTP/1.0 404 Not Found\n";
    private final String CODE_CREATED = "HTTP/1.0 201 Created\n";
    private final String CODE_BAD_REQUEST = "HTTP/1.0 400 Bad Request\n";
    private final String CODE_FORBIDDEN = "HTTP/1.0 403 Forbidden\n";

    public static void main(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                MAINPATH = args[i + 1];
            }

            if (args[i].equals("-p")) {
                PORT = Integer.parseInt(args[i + 1]);
            }

            if (args[i].equals("-v")) {
                verbose = true;
            }

        }


        httpfs server = new httpfs(PORT, MAINPATH);
        server.start();
    }

    private httpfs(int port, String directory) {
        try {
            client = new ServerSocket(port, 0, InetAddress.getLoopbackAddress());

            if (verbose) {
                System.out.println("\nServerSocket Created: " + client.toString());
                System.out.println("Directory: " + directory + "\n");
            }

        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    private void start() {
        System.out.println("Server started at port: " + PORT);

        while (true) {
            try {
                Socket socket = client.accept();
                System.out.println();

                InputStream is = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                OutputStream os = socket.getOutputStream();
                Writer writer = new BufferedWriter(new OutputStreamWriter(os));

                String lineReader;
                String content = "";
                int index = 0;
                headers = "";

                while ((lineReader = reader.readLine()).length() > 0) {

                    if (index == 0) {
                        content += lineReader;
                        System.out.println(lineReader);

                    } else {
                        if (verbose) {
                            System.out.println(lineReader);
                        }

                        if (lineReader.toLowerCase().contains("content-length:")) {
                            postContentLength = Integer.parseInt(lineReader.split(":")[1].trim());
                        } else {
                            headers += lineReader + "\n";
                        }
                    }

                    index++;
                }


                String[] line = content.split(" ");

                try {

                    filePath = new URL(line[1]).getPath();

                } catch (MalformedURLException e) {
                    // it wasn't a URL
                }

                if (verbose) {
                    System.out.println(lineReader);
                    System.out.println("File path: " + filePath);
                }

                if (line[0].toLowerCase().contains("get")) {
                    if ((filePath.contentEquals("")) || (filePath.contentEquals("/"))) {
                        /* PART 2: QUESTION 1*/
                        readAllFiles(writer);
                    } else {
                        /* PART 2: QUESTION 2*/
                        readFile(filePath, writer);
                    }
                } else if (line[0].toLowerCase().contains("post")) {
                    /* PART 2: QUESTION 3 */
                    postFile(filePath, writer, reader);
                }

                socket.close();
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }
    }

    private void readAllFiles(Writer writer) {
        try {
            File directory = new File(MAINPATH);
            File[] fileNames = directory.listFiles();

            String content = "";


            for (int i = 0; i < fileNames.length; i++) {
                content += fileNames[i].getName() + "\n";
            }

            writer.write(CODE_OK + headers + "\r\n" + content);
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile(String file, Writer writer) {
        String respone = "";

        try {
            if (file.contains("//")) {
                writer.write(CODE_BAD_REQUEST + headers + "\r\n");
                writer.flush();
            } else {
                try {
                    File fileName = new File(MAINPATH + file);

                    if (fileName.canRead()) {
                        FileReader fileReader = new FileReader(fileName);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        int length = HttpReader.readContentLength(fileName);
                        String contentType = HttpReader.readContentType(file);

                        char[] fileRead = new char[length];
                        bufferedReader.read(fileRead);

                        respone += CODE_OK + headers;
                        respone += "Content-Length: " + length + "\nContent-Type: " + contentType + "\n\r\n";


                        writer.write(respone);
                        writer.write(fileRead);
                        fileReader.close();
                        bufferedReader.close();

                    } else {
                        if (fileName.exists()) {
                            writer.write(CODE_FORBIDDEN + headers + "\r\n");
                        } else
                            throw new FileNotFoundException();
                    }

                    writer.flush();

                } catch (FileNotFoundException e) {
                    writer.write(CODE_NOT_FOUND + headers + "\r\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    private void postFile(String fileLocation, Writer writer, BufferedReader reader) {

        try {
            File file = new File(MAINPATH + fileLocation);

            if (!file.exists()) {
                String directory = file.getParent();
                File directoryX = new File(directory);
                directoryX.mkdirs();
                file = new File(MAINPATH + fileLocation);
            }

            FileWriter newFile = new FileWriter(MAINPATH + fileLocation);

            StringBuilder payload = new StringBuilder();
            while (reader.ready()) {
                payload.append((char) reader.read());
            }

            String data = payload.toString().replaceAll("\"", "").replaceAll("\\{", "").replaceAll("\\}", "");

            System.out.println("Data is: " + data);

            newFile.write(data);

            int length = HttpReader.readContentLength(file);
            String contentType = HttpReader.readContentType(fileLocation);

            writer.write(CODE_CREATED + "Content-Length: " + length + "\nContent-Type: " + contentType + "\n\r\n");


            writer.flush();
            reader.close();
            newFile.close();

        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}