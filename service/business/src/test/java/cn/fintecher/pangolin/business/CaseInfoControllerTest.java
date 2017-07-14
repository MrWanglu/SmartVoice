package cn.fintecher.pangolin.business;


import cn.fintecher.pangolin.business.repository.AreaCodeRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.web.CaseInfoController;
import cn.fintecher.pangolin.entity.AreaCode;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.Personal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApp.class})
public class CaseInfoControllerTest {


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;


    private MockMvc restDataDictMockMvc;

    private CaseInfo caseInfo;
    @Autowired
    private CaseInfoRepository caseInfoRepository;
    @Autowired
    private AreaCodeRepository areaCodeRepository;
    @Autowired
    private PersonalRepository personalRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CaseInfoController caseInfoController = new CaseInfoController(caseInfoRepository);
        this.restDataDictMockMvc = MockMvcBuilders.standaloneSetup(caseInfoController)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        AreaCode areaCode = areaCodeRepository.findOne(1783);
        caseInfo = new CaseInfo();
        Personal personalInfo = personalRepository.findOne("8a80cb815d3faeef015d3faf0c6a0000");
        caseInfo.setPersonalInfo(personalInfo);
        caseInfo.setArea(areaCode);
    }


    @Test
    public void createCaseInfo() throws Exception {

        restDataDictMockMvc.perform(put("/api/caseInfo")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(caseInfo)))
                .andExpect(status().is2xxSuccessful());
    }

}
