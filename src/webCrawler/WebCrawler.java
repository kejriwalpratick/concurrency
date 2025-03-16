package webCrawler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawler {
    private final Set<String> crawled;

    public WebCrawler() {
        this.crawled = new HashSet<>();
    }

    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        asyncCrawl(startUrl, htmlParser);
        return new ArrayList<>(crawled);
    }

    public void asyncCrawl(String startUrl, HtmlParser htmlParser) {
        cache(startUrl);

        List<String> urls = htmlParser.getUrls(startUrl);
        List<Thread> threads = new ArrayList<>();

        for (String u : urls) {
            if (!isCached(u) && getHost(startUrl).equals(getHost(u))) {
                threads.add(new Thread(() -> asyncCrawl(u, htmlParser)));
            }
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t: threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isCached(String url) {
        synchronized (this) {
            return crawled.contains(url);
        }
    }

    private void cache(String url) {
        synchronized (this) {
            crawled.add(url);
        }
    }

    private String getHost(String startUrl) {
        String[] parts = startUrl.split("//");
        String[] parts2 = parts[1].split("/");
        return parts2[0];
    }

    public static class HtmlParser {
        public List<String> getUrls(String url) {
            // ...
            return null;
        }
    }
}
