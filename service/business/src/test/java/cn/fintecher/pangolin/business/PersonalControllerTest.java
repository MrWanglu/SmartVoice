package cn.fintecher.pangolin.business;


import cn.fintecher.pangolin.business.repository.AreaCodeRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.entity.Personal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestApp.class})
public class PersonalControllerTest {


    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;


    private MockMvc restDataDictMockMvc;

    private Personal personal;
    @Autowired
    private PersonalRepository personalRepository;
    @Autowired
    private AreaCodeRepository areaCodeRepository;

//    @Before
//    public void setup() {
//        MockitoAnnotations.initMocks(this);
//        PersonalController personalController = new PersonalController(personalRepository);
//        this.restDataDictMockMvc = MockMvcBuilders.standaloneSetup(personalController)
//                .setCustomArgumentResolvers(pageableArgumentResolver)
//                .setMessageConverters(jacksonMessageConverter).build();
//    }

    @Before
    public void initTest() {
        personal = new Personal();
        personal.setAge(20);
        personal.setIdCard("650102198205060732");
        personal.setName("陈畅");
    }


    @Test
    public void createPersonalInfo() throws Exception {

        restDataDictMockMvc.perform(put("/api/personal")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(personal)))
                .andExpect(status().is2xxSuccessful());
    }

}
