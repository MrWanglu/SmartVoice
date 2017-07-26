package cn.fintecher.pangolin.service.reminder.web.rest;

import cn.fintecher.pangolin.service.reminder.model.ReminderCalendar;
import cn.fintecher.pangolin.service.reminder.repository.ReminderCalendarRepository;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Created by ChenChang on 2017/2/21.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/reminderCalendars")
public class ReminderCalendarResource {
    private final Logger log = LoggerFactory.getLogger(ReminderCalendarResource.class);
    @Autowired
    private ReminderCalendarRepository reminderCalendarRepository;

    @PostMapping
    public ResponseEntity<ReminderCalendar> createReminderCalendar(@RequestBody ReminderCalendar reminderCalendar) throws URISyntaxException {
        log.debug("REST request to save ReminderCalendar : {}", reminderCalendar);
        if (reminderCalendar.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ReminderCalendar", "idexists", "A new ReminderCalendar cannot already have an ID")).body(null);
        }
        ReminderCalendar result = reminderCalendarRepository.save(reminderCalendar);
        return ResponseEntity.created(new URI("/api/ReminderCalendars/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("ReminderCalendar", result.getId()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<ReminderCalendar> updateReminderCalendar(@RequestBody ReminderCalendar reminderCalendar) throws URISyntaxException {
        log.debug("REST request to update ReminderCalendar : {}", reminderCalendar);
        if (reminderCalendar.getId() == null) {
            return createReminderCalendar(reminderCalendar);
        }
        ReminderCalendar result = reminderCalendarRepository.save(reminderCalendar);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("ReminderCalendar", reminderCalendar.getId().toString()))
                .body(result);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReminderCalendar> getReminderCalendar(@PathVariable String id) {
        log.debug("REST request to get ReminderCalendar : {}", id);
        ReminderCalendar reminderCalendar = reminderCalendarRepository.findOne(id);
        return Optional.ofNullable(reminderCalendar)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReminderCalendar>> getAllReminderCalendar(Pageable pageable)
            throws URISyntaxException {
        log.debug("REST request to get a page of Customers");
        Page<ReminderCalendar> page = reminderCalendarRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reminderCalendars");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminderCalendar(@PathVariable String id) {
        log.debug("REST request to delete ReminderCalendar : {}", id);
        reminderCalendarRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("reminderCalendar", id.toString())).build();
    }
}
