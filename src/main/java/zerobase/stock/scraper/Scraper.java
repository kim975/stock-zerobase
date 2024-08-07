package zerobase.stock.scraper;

import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapeResult;

public interface Scraper {

    ScrapeResult scrap(Company company);

    Company scrapCompanyByTicker(String ticker);

}
