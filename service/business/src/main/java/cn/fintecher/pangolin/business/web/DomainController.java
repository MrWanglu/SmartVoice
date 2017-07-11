package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.DomainRepository;
import cn.fintecher.pangolin.entity.Domain;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api")
public class DomainController {

    private static final String ENTITY_NAME = "domain";
    private final Logger log = LoggerFactory.getLogger(DomainController.class);
    private final DomainRepository domainRepository;

    public DomainController(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }


    @PostMapping("/domain")
    public ResponseEntity<Domain> createDomain(@RequestBody Domain domain) throws URISyntaxException {
        log.debug("REST request to save domain : {}", domain);
        if (domain.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        Domain result = domainRepository.save(domain);
        return ResponseEntity.created(new URI("/api/domain/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/domain")
    public ResponseEntity<Domain> updateDomain(@RequestBody Domain domain) throws URISyntaxException {
        log.debug("REST request to update Domain : {}", domain);
        if (domain.getId() == null) {
            return createDomain(domain);
        }
        Domain result = domainRepository.save(domain);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, domain.getId().toString()))
                .body(result);
    }

    @GetMapping("/domain")
    public List<Domain> getAllDomain() {
        log.debug("REST request to get all Domain");
        List<Domain> domainList = domainRepository.findAll();
        return domainList;
    }

    @GetMapping("/domain/{id}")
    public ResponseEntity<Domain> getDomain(@PathVariable String id) {
        log.debug("REST request to get domain : {}", id);
        Domain domain = domainRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(domain));
    }

    @DeleteMapping("/domain/{id}")
    public ResponseEntity<Void> deleteDomain(@PathVariable String id) {
        log.debug("REST request to delete domain : {}", id);
        domainRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

}
