package com.oasis.onlinestore.controller;

import com.oasis.onlinestore.contract.SimpleResponse;
import com.oasis.onlinestore.domain.Item;
import com.oasis.onlinestore.service.ItemService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    ItemService service;

    @GetMapping
    ResponseEntity<?> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {

        Pageable paging = PageRequest.of(page, size);
        Page<Item> foundItems = service.findAll(paging);

        Map<String, Object> response = new HashMap<>();
        response.put("items", foundItems);
        response.put("currentPage", foundItems.getNumber());
        response.put("totalItems", foundItems.getTotalElements());
        response.put("totalPages", foundItems.getTotalPages());


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping(consumes = {"multipart/form-data"})
    ResponseEntity<?> saveItem(@RequestPart Item item,
                               @RequestPart("image") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            item.setImage(content);
            service.save(item);
        } catch (Exception e) {
            SimpleResponse res = new SimpleResponse(false, "Failed to create item");
            return new ResponseEntity<SimpleResponse>(res, HttpStatus.BAD_REQUEST);
        }
        SimpleResponse res = new SimpleResponse(true, "Successfully create item", item);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @RolesAllowed("ROLE_ADMIN")
    @PutMapping(path = "/{itemId}", consumes = {"multipart/form-data"})
    ResponseEntity<?> updateItem(
            @PathVariable String itemId,
            @RequestPart Item item,
            @RequestPart("image") MultipartFile file) {

        Item updateItem;
        try {
            UUID uuid = UUID.fromString(itemId);
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            item.setImage(content);

            updateItem = service.update(uuid, item);
        } catch (Exception e) {
            SimpleResponse res = new SimpleResponse(false, "Failed to update item");
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        SimpleResponse res = new SimpleResponse(true, "Successfully update item", updateItem);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

}
