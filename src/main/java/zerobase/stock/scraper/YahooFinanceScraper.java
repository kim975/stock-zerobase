package zerobase.stock.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.stock.model.Company;
import zerobase.stock.model.Dividend;
import zerobase.stock.model.ScrapeResult;
import zerobase.stock.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {


    private static final String URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&filter=div&period1=%d&period2=%d";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapeResult scrap(Company company) {

        ScrapeResult scrapeResult = new ScrapeResult();
        scrapeResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(URL, company.getTicker(), START_TIME, now);

            Connection connect = Jsoup.connect(url);
            Document document = connect.get();

            Elements parsingDivs = document.getElementsByClass("table yf-ewueuo");
            Element tableEle = parsingDivs.get(0);
            Element tbody = tableEle.children().get(1);

            List<Dividend> dividendList = new ArrayList<>();
            for (Element tr : tbody.children()) {
                String txt = tr.text();

                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] split = txt.split(" ");
                int month = Month.strToNumber(split[0]);
                int day = Integer.parseInt(split[1].replace(",", ""));
                int year = Integer.parseInt(split[2]);
                String dividend = split[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected  Month enum value -> " + split[0]);
                }

                dividendList.add(Dividend.builder()
                        .date(LocalDateTime.of(year, month, day, 0, 0))
                        .dividend(dividend).build());

            }

            scrapeResult.setDividendEntities(dividendList);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return scrapeResult;

    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {

        String url = String.format(SUMMARY_URL, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByClass("yf-3a2v0c").get(0);


            String title = titleEle.text().trim();
            title = title.substring(0, title.lastIndexOf("(")).trim();

            System.out.println("titleEle.text(): " + title);

            return Company.builder()
                    .ticker(ticker)
                    .name(title)
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
