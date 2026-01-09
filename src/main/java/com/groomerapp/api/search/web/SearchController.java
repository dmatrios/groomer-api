package com.groomerapp.api.search.web;

import com.groomerapp.api.search.service.SearchService;
import com.groomerapp.api.search.web.dto.SearchResponse;
import com.groomerapp.api.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<SearchResponse> search(@RequestParam String q) {
        return ApiResponse.ok(searchService.search(q));
    }
}
