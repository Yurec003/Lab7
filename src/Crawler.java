import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {

    private static Pattern urlPattern = Pattern.compile("<a href=\"(http://.*?)\".*?>");
    private PageLoader pageLoader;

    public Crawler(PageLoader pageLoader){
        this.pageLoader = pageLoader;
    }

    public ArrayList<URLDepthPair> inspect(String startURL, int maxDepth){
        if (maxDepth == 0){
            return new ArrayList<>(){{
                add(new URLDepthPair(startURL, 0));
            }};
        }
        String page = pageLoader.loadPage(startURL);
        Matcher matcher = urlPattern.matcher(page);
        ArrayList<String> parsedURLs= new ArrayList<>();
        while (matcher.find()){
            parsedURLs.add(matcher.group(1));
        }
        ArrayList<URLDepthPair> results = new ArrayList<>();
        results.add(new URLDepthPair(startURL, 0));
        for (String URL : parsedURLs){
            ArrayList<URLDepthPair> childrenURL = inspect(URL, maxDepth-1);
            for (URLDepthPair childURL: childrenURL){
                results.add(new URLDepthPair(childURL.getUrl(), childURL.getDepth()+1));
            }
        }
        return results;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String url = scanner.nextLine();
        int depth = scanner.nextInt();
        Crawler crawler = new Crawler(new SocketPageLoader());
        for (URLDepthPair urlDepthPair: crawler.inspect(url, depth)){
            System.out.println(urlDepthPair.toString());
        }

    }

    interface PageLoader{
        String loadPage(String url);
    }

    static class SocketPageLoader implements PageLoader{

        @Override
        public String loadPage(String url) {
            try {
                URL parsedUrl = new URL(url);
                String host = parsedUrl.getHost();
                int port = parsedUrl.getPort();
                if (port == -1) {
                    port = 80;
                }
                String path = parsedUrl.getPath();
                if (path.isEmpty()) {
                    path = "/";
                }
                String query = parsedUrl.getQuery();
                if (query != null) {
                    path += "?" + query;
                }

                Socket socket = new Socket(host, port);

                PrintWriter requestWriter = new PrintWriter(socket.getOutputStream(), true);
                requestWriter.println("GET " + path + " HTTP/1.1");
                requestWriter.println("Host: " + host);
                requestWriter.println("Connection: close");
                requestWriter.println();
                requestWriter.flush();


                BufferedReader responseReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = responseReader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                socket.close();

                return responseBuilder.toString();
            } catch (IOException e) {
                return "";
            }
        }
    }
}