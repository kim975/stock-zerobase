package zerobase.stock.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import zerobase.stock.exception.impl.NoCompanyException;
import zerobase.stock.model.Company;
import zerobase.stock.model.ScrapeResult;
import zerobase.stock.persist.CompanyRepository;
import zerobase.stock.persist.DividendRepository;
import zerobase.stock.persist.entity.CompanyEntity;
import zerobase.stock.persist.entity.DividendEntity;
import zerobase.stock.scraper.Scraper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {

        boolean exists = companyRepository.existsByTicker(ticker);
        if (exists) {
            throw new RuntimeException("Already exists ticker -> " + ticker);
        }

        return storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {
        // ticker를 기주능로 회사를 스크래핑
        Company company = yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("Failed to scrap ticker - > " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보를 스크래핑
        ScrapeResult scrapeResult = yahooFinanceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntityList = scrapeResult.getDividendEntities().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        dividendRepository.saveAll(dividendEntityList);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntityList = companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntityList.stream()
                .map(CompanyEntity::getName)
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        trie.put(keyword, null);
    }

    // 강의 내용으로 미사용이나 작성
    public List<String> autocomplete(String keyword) {
        return (List<String>) trie.prefixMap(keyword).keySet().stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    // 강의 내용으로 미사용이나 작성
    public void deleteAutocompleteKeyword(String keyword) {
        trie.remove(keyword);
    }

    @Transactional
    public String deleteCompany(String ticker) {
        CompanyEntity company = companyRepository.findByTicker(ticker)
                .orElseThrow(NoCompanyException::new);

        dividendRepository.deleteAllByCompanyId(company.getId());
        companyRepository.deleteById(company.getId());

        deleteAutocompleteKeyword(company.getName());

        return company.getName();

    }
}
