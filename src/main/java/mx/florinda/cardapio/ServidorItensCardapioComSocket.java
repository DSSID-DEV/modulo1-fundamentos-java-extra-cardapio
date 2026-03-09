package mx.florinda.cardapio;

import com.dssid.dev.persistence.RepositoryFactory;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static mx.florinda.cardapio.utils.Utils.extractVariables;

public class ServidorItensCardapioComSocket {

//    private static final repository repository = new SQLrepository();
    private static final ItemCardapioRepository repository = RepositoryFactory.createRepository(ItemCardapioRepository.class);


    public static void main(String[] args) throws Exception {

        Executor executor = Executors.newFixedThreadPool(50);

        try(ServerSocket serverSocket = new ServerSocket(8000)) {
            System.out.println("Subiu servidor!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> trataRequisicao(clientSocket));
            }

        }
    }

    private static void trataRequisicao(Socket clientSocket) {
        String regexPathMapping = "^/([^/]+)(?:/\\d+)?/?$";
        try (clientSocket) {
            InputStream clientIS = clientSocket.getInputStream();

            StringBuilder requestBuilder = new StringBuilder();
            int data;
            do {
                data = clientIS.read();
                requestBuilder.append((char) data);
            } while (clientIS.available() > 0);

            String request = requestBuilder.toString();
            System.out.println(request);
            System.out.println("\n\nChegou um novo request");

            String[] requestChunks = request.split("\r\n\r\n");
            String requestLineAndHeaders = requestChunks[0];
            String[] requestLineAndHeadersChunks = requestLineAndHeaders.split("\r\n");
            String requestLine = requestLineAndHeadersChunks[0];
            String[] requestLineChunks = requestLine.split(" ");
            String method = requestLineChunks[0];
            String requestURI = requestLineChunks[1].replaceAll(regexPathMapping, "/$1");
            String httpVersion = requestLineChunks[2];;
            var params = extractVariables(requestLineChunks[1]);
            System.out.println("Method: " + method);
            System.out.println("Request URI: " + requestURI);
            System.out.println("HTTP Version: " + httpVersion);

            Thread.sleep(250);

            OutputStream clientOS = clientSocket.getOutputStream();
            PrintStream clientOut = new PrintStream(clientOS);

            if ("/itensCardapio.json".equals(requestURI)) {
                System.out.println("Chamou arquivo itensCardapio.json");

                Path path = Path.of("itensCardapio.json");
                String json = Files.readString(path);

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } if ("GET".equals(method)
                    && requestURI.matches(regexPathMapping)
                    && !params.isEmpty()
            ) {
                System.out.println("Chamou listagem de itens de cardápio pelo id");
                var id = params.values().stream().findFirst().orElse(null);
                ItemCardapio itemCardapio = repository.findById(Long.parseLong(id.toString()));

                Gson gson = new Gson();
                String json = gson.toJson(itemCardapio);
                if(itemCardapio == null) {
                    clientOut.println("HTTP/1.1 204 Not Found");
                    return;
                }
                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } else if ("GET".equals(method) && "/itens-cardapio".equals(requestURI)) {
                System.out.println("Chamou listagem de itens de cardápio");
                List<ItemCardapio> listaItensCardapio = repository.findAll();

                Gson gson = new Gson();
                String json = gson.toJson(listaItensCardapio);

                if (listaItensCardapio.isEmpty()) {
                    clientOut.println("HTTP/1.1 204 No Content");
                    return;
                }

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } else if ("GET".equals(method) && "/itens-cardapio/total".equals(requestURI)) {
                System.out.println("Chamou total de itens de cardápio");
                int totalItens = repository.totalItensCardapio();

                clientOut.println("HTTP/1.1 200 OK");
                clientOut.println();
                clientOut.println(totalItens);
            } else if ("POST".equals(method) && "/itens-cardapio".equals(requestURI)) {
                System.out.println("Chamou adição de itens de cardápio");

                if (requestChunks.length == 1) {
                    clientOut.println("HTTP/1.1 400 Bad Request");
                }
                String body = requestChunks[1];

                Gson gson = new Gson();
                ItemCardapio item = gson.fromJson(body, ItemCardapio.class);

                var created = repository.insert(item);
                var json = gson.toJson(created);
                clientOut.println("HTTP/1.1 201 Created");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } else if ("PUT".equals(method) && "/itens-cardapio".equals(requestURI)) {
                System.out.println("Chamou update de itens de cardápio");

                if (requestChunks.length == 1 || params.isEmpty()) {
                    clientOut.println("HTTP/1.1 400 Bad Request");
                }

                String body = requestChunks[1];
                var id = params.values().stream().findFirst().orElse(null);
                Gson gson = new Gson();
                ItemCardapio item = gson.fromJson(body, ItemCardapio.class);

                var created = repository.update(item, Long.valueOf(id.toString()));
                var json = gson.toJson(created);
                clientOut.println("HTTP/1.1 200 Ok");
                clientOut.println("Content-type: application/json; charset=UTF-8");
                clientOut.println();
                clientOut.println(json);

            } else if ("DELETE".equals(method)
                    && requestURI.matches(regexPathMapping)
                    && !params.isEmpty()) {
                System.out.println("Chamou deleção de itens de cardápio");

                if (params.isEmpty()) {
                    clientOut.println("HTTP/1.1 400 Bad Request");
                }
                var id = params.values().stream().findFirst().orElse(null).toString();
                if(!repository.existsById(Long.parseLong(id))) {
                    clientOut.println("HTTP/1.1 404 Not Found");
                }

                repository.deleteById(Long.parseLong(id));

                clientOut.println("HTTP/1.1 200 Ok");
            }
            else {
                System.out.println("URI não encontrada: " + requestURI);
                clientOut.println("HTTP/1.1 404 Not Found");
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
