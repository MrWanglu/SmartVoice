package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.AccFinanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface AccFinanceEntryRepository extends QueryDslPredicateExecutor<AccFinanceEntry>, JpaRepository<AccFinanceEntry, String> {
}
