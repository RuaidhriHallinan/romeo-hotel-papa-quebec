package com.aspect;

import com.aspect.domain.WorkRequest;
import com.aspect.services.WorkOrderRequestService;
import com.aspect.util.DateTimeUtil;
import com.aspect.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * WorkRequestController RESTful controller that accepts GET, PUT, POST, DELETE requests
 *
 * Created by Ruaidhri on 03/07/2017.
 */
@RestController
public class WorkRequestController {

    private WorkOrderRequestService workOrderRequestService;

    @Autowired
    WorkRequestController(WorkOrderRequestService workOrderRequestService) {
        this.workOrderRequestService = workOrderRequestService;
    }

    /**
     * An endpoint for adding a ID to queue (enqueue). This endpoint should
     * accept two parameters, the ID to enqueue and the time at which the ID
     *
     * @param id
     * @param date
     * @return
     */
    @RequestMapping(value = "/put/{id}/{date}", method = RequestMethod.POST)
    public ResponseEntity<String> enqueueIdDate(@PathVariable("id") Long id, @PathVariable("date") String date) {

        if (id != null && id >= 0 && date != null) {

            try {
                WorkRequest wr = new WorkRequest(id, DateTimeUtil.composeDate(date), Util.getWorkRequestType(id));
                if (workOrderRequestService.enqueue(wr)) {
                    return new ResponseEntity(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Queue already contains Id", HttpStatus.BAD_REQUEST);
                }
            } catch (ParseException e) {
                return new ResponseEntity<>("Unknown error occurred", HttpStatus.BAD_REQUEST);
            }

        } else {
            return new ResponseEntity<>("Invalid id or date parameter", HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * An endpoint for removing a specific ID from the queue.
     * This endpoint should accept a single parameter, the ID to remove.
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> dequeueId(@PathVariable("id") Long id) {

        if (id != null && id > 0) {
            if (workOrderRequestService.dequeue(id)) {
                return new ResponseEntity(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Empty queue", HttpStatus.BAD_REQUEST);
            }

        } else {
            return new ResponseEntity<>("Invalid id parameter", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * An endpoint for getting the top ID from the queue and removing it (de-queue).
     * This endpoint should return the highest ranked ID and the time
     *
     * @return
     */
    @RequestMapping(value = "/remove/top", method = RequestMethod.DELETE)
    public ResponseEntity<String> dequeueTop() {

        if (workOrderRequestService != null) {
            try {

                long id = workOrderRequestService.dequeueTop();
                return new ResponseEntity("Removed " + id + " from the queue", HttpStatus.OK);

            } catch (Exception e) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity("Priority Queue empty", HttpStatus.NOT_FOUND);
        }

    }

    /**
     * An endpoint for getting the list of IDs in the queue.
     * This endpoint should return a list of IDs sorted from highest ranked to lowest.
     *
     * @return <List<Integer>
     */
    @RequestMapping(value = "/get/ids", method = RequestMethod.GET)
    public ResponseEntity<List<Integer>> listIds() {
        List<Integer> workOrders = workOrderRequestService.getWorkOrderIDs();
        return new ResponseEntity(workOrders, HttpStatus.OK);
    }

    /**
     * An endpoint to get the position of a specific ID in the queue.
     * This endpoint should accept one parameter, the ID to get the position of.
     * It should return the position of the ID in the queue indexed from 0.
     *
     * @param id
     * @return Integer
     */
    @RequestMapping(value = "/get/position/{id}", method = RequestMethod.GET)
    public ResponseEntity<Integer> position(@PathVariable("id") Long id) {

        if (id != null && id > 0) {
            int position = workOrderRequestService.getPosition(id);
            return new ResponseEntity(position, HttpStatus.OK);
        }

        return new ResponseEntity("Invalid id", HttpStatus.BAD_REQUEST);
    }

    /**
     * An endpoint to get the average wait time.
     * This endpoint should accept a single parameter, the current time,
     * and should return the average (mean) number of seconds that each ID has been waiting in the queue.
     *
     * @return
     */
    @RequestMapping(value = "/get/mean/{time}", method = RequestMethod.GET)
    public ResponseEntity<String> averageTime(@PathVariable("time") String currentDateRequest) {

        try {
            if (currentDateRequest != null && !currentDateRequest.isEmpty()) {
                Date currentDate = DateTimeUtil.composeDate(currentDateRequest);
                Date meanWait = workOrderRequestService.getWaitTime(currentDate);
                return new ResponseEntity(DateTimeUtil.parseDate(meanWait), HttpStatus.OK);
            }
        } catch (ParseException p) {
            return new ResponseEntity("Invalid time format", HttpStatus.BAD_REQUEST);
        }

        return null;
    }

}
