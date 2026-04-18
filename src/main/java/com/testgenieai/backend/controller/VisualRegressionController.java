package com.testgenieai.backend.controller;

import com.testgenieai.backend.dto.VisualBaselineResponse;
import com.testgenieai.backend.dto.VisualBaselineUpsertRequest;
import com.testgenieai.backend.dto.VisualCompareRequest;
import com.testgenieai.backend.dto.VisualComparisonResponse;
import com.testgenieai.backend.service.VisualRegressionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visual-regression")
@RequiredArgsConstructor
public class VisualRegressionController {

    private final VisualRegressionService visualRegressionService;

    @PostMapping("/baseline")
    public VisualBaselineResponse upsertBaseline(@Valid @RequestBody VisualBaselineUpsertRequest request) {
        return visualRegressionService.upsertBaseline(request);
    }

    @PostMapping("/compare")
    public VisualComparisonResponse compare(@Valid @RequestBody VisualCompareRequest request) {
        return visualRegressionService.compare(request);
    }

    @GetMapping("/history")
    public List<VisualComparisonResponse> history(@RequestParam(required = false) String pageKey) {
        return visualRegressionService.history(pageKey);
    }
}
