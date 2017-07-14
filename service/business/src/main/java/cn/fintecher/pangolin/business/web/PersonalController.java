package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.types.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api")
public class PersonalController {

    private static final String ENTITY_NAME = "personal";
    private final Logger log = LoggerFactory.getLogger(PersonalController.class);
    private final PersonalRepository personalRepository;

    public PersonalController(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }


    @PostMapping("/personal")
    public ResponseEntity<Personal> createPersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to save personal : {}", personal);
        if (personal.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.created(new URI("/api/personal/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/personal")
    public ResponseEntity<Personal> updatePersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to update Personal : {}", personal);
        if (personal.getId() == null) {
            return createPersonal(personal);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, personal.getId().toString()))
                .body(result);
    }

    @GetMapping("/personal")
    public List<Personal> getAllPersonal() {
        log.debug("REST request to get all Personal");
        List<Personal> personalList = personalRepository.findAll();
        return personalList;
    }

    @GetMapping("/queryPersonal")
    public ResponseEntity<Page<Personal>> queryPersonal(@QuerydslPredicate(root = Personal.class) Predicate predicate, @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get all Personal");

        Page<Personal> page = personalRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryPersonal");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<Personal> getPersonal(@PathVariable String id) {
        log.debug("REST request to get personal : {}", id);
        Personal personal = personalRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personal));
    }

    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Void> deletePersonal(@PathVariable String id) {
        log.debug("REST request to delete personal : {}", id);
        personalRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

}
