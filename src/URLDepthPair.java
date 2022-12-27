public class URLDepthPair {

    private final String url;
    private final int depth;

    public URLDepthPair(String url, int depth) {
        super();
        this.url = url;
        this.depth = depth;
    }


    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString(){
        return "URL: " + url + ", depth: " + depth;
    }
}