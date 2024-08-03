package zerobase.stock.scraper;

import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapeResult;

public interface Scraper {

    public ScrapeResult scrap(Company company);

    public Company scrapCompanyByTicker(String ticker);

}
