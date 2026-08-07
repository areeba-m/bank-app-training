package com.redmath.transfer.controller;

import com.redmath.transfer.dto.CreateTransferRequest;
import com.redmath.transfer.dto.TransferResponse;
import com.redmath.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping()
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @NonNull Authentication auth) {

        String email = auth.getName();

        return ResponseEntity.ok(
                transferService.createTransfer(request, email)
        );
    }

    @GetMapping()
    public ResponseEntity<Page<TransferResponse>> getTransfers(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                transferService.getTransfers(
                        authentication.getName(),
                        page,
                        size
                )
        );
    }
}
