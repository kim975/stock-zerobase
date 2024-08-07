package zerobase.stock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapeResult;
import zerobase.stock.model.constants.CacheKey;
import zerobase.stock.persist.CompanyRepository;
import zerobase.stock.persist.DividendRepository;
import zerobase.stock.persist.entity.CompanyEntity;
import zerobase.stock.persist.entity.DividendEntity;
import zerobase.stock.scraper.Scraper;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@RequiredArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;
    private final Scraper yahooFinanceScraper;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("Starting scheduler is starting");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companyEntityList = companyRepository.findAll();

        // 회사 맘다 배당금 정보를 새로 스크래핑
        for (CompanyEntity companyEntity : companyEntityList) {

            ScrapeResult scrapeResult = yahooFinanceScraper.scrap(
                    Company.builder()
                    .name(companyEntity.getName())
                    .ticker(companyEntity.getTicker())
                    .build()
            );

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapeResult.getDividendEntities().stream()
                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                    .forEach(e -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                            log.info("insert new dividend: " + e.toString());
                        }
                    });

            //연속적으로 스크래핑 대상 사이토 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


    }

}
