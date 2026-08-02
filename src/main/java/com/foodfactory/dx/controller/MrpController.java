package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.domain.MrpRun;
import com.foodfactory.dx.service.MrpService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MRP(自動生産計画)のController。
 * 本来は1日1回のスケジューラ実行(AUTO)を組む想定だが、
 * 今回はそのスケジューラ自体は未実装のため、このAPIを手動で叩くことでAUTO相当の動きを代用する。
 * CANCELLED/REJECTED発生時のEVENT実行は、ManufacturingService側から自動的に呼ばれるため、
 * このControllerを経由しない。
 */
@RestController
public class MrpController {

    private final MrpService mrpService;

    public MrpController(MrpService mrpService) {
        this.mrpService = mrpService;
    }

    /** 全商品についてMRPを手動実行する。 */
    @PostMapping("/api/mrp/run")
    public ResponseEntity<List<ManufacturingBatch>> run() {
        List<ManufacturingBatch> created = mrpService.runForAllItems(MrpRun.TriggeredBy.MANUAL);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
