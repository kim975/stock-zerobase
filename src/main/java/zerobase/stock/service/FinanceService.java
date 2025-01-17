package zerobase.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import zerobase.stock.exception.impl.NoCompanyException;
import zerobase.stock.model.Company;
import zerobase.stock.model.Dividend;
import zerobase.stock.model.ScrapeResult;
import zerobase.stock.model.constants.CacheKey;
import zerobase.stock.persist.CompanyRepository;
import zerobase.stock.persist.DividendRepository;
import zerobase.stock.persist.entity.CompanyEntity;
import zerobase.stock.persist.entity.DividendEntity;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapeResult getDividendByCompanyName(String companyName) {

        log.info("search company: {}", companyName);

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = companyRepository.findByName(companyName)
                .orElseThrow(NoCompanyException::new);

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntityList = dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반황
        return new ScrapeResult(
                Company.builder()
                        .ticker(company.getTicker())
                        .name(company.getName())
                        .build(),

                dividendEntityList.stream()
                        .map(e -> Dividend.builder()
                                .date(e.getDate())
                                .dividend(e.getDividend())
                                .build())
                        .collect(Collectors.toList())
        );
    }

}
