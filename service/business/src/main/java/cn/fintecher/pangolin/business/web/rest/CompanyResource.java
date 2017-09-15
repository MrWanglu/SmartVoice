package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.QCompany;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/9/14.
 */
@RestController
@RequestMapping("/api/companyResource")
@Api(description = "公司Resource")
public class CompanyResource {

    @Inject
    CompanyRepository companyRepository;

    @GetMapping("/getCompanyByCode")
    @ApiOperation(value = "查询公司信息", notes = "查询公司信息")
    public ResponseEntity<Company> getCompanyByCode(@RequestParam(value = "companyCode") String companyCode) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCompany.company.code.eq(companyCode));
        Company one = companyRepository.findOne(builder);
        return new ResponseEntity<>(one, HttpStatus.OK);
    }
}
