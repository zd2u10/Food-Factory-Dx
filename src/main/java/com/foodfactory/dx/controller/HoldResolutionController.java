package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.HoldResolution;
import com.foodfactory.dx.dto.ResolveHoldRequest;
import com.foodfactory.dx.service.HoldResolutionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

/**
 * 保留対応のController。
 * 「交換」による対応は、新しい入荷明細の登録そのものなので、
 * MaterialArrivalLineController(あるいはProcurementService)側で扱う。
 */
@RestController
@RequestMapping("/api/holds")
public class HoldResolutionController {

    private final HoldResolutionService holdResolutionService;

    public HoldResolutionController(HoldResolutionService holdResolutionService) {
        this.holdResolutionService = holdResolutionService;
    }

    /** 対応待ち(ON_HOLD)の保留一覧を取得する。 */
    @GetMapping
    public List<HoldResolution> listOpenHolds() {
        return holdResolutionService.listOpenHolds();
    }

    /** 返品として対応する。 */
    @PostMapping("/{holdId}/resolve-returned")
    @ResponseStatus(HttpStatus.OK)
    public void resolveAsReturned(@PathVariable Long holdId, @RequestBody ResolveHoldRequest request) {
        holdResolutionService.resolveAsReturned(holdId, request.getComment());
    }

    /** 結局受け入れる、として対応する。 */
    @PostMapping("/{holdId}/resolve-accepted-late")
    @ResponseStatus(HttpStatus.OK)
    public void resolveAsAcceptedLate(@PathVariable Long holdId, @RequestBody ResolveHoldRequest request) {
        holdResolutionService.resolveAsAcceptedLate(holdId, request.getComment());
    }
}
