package com.kmarine.fishing.payment;

import com.kmarine.fishing.common.ApiResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 검증 (프론트 결제 완료 후 호출)
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponseDto.Info>> verify(
    		@RequestParam("impUid")        String impUid,
            @RequestParam("reservationId") Long reservationId//,
            //@AuthenticationPrincipal Long userId,
            //@Valid @RequestBody PaymentRequestDto.Verify request
            ) throws IamportResponseException {
        paymentService.verify(impUid, reservationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
        //return ResponseEntity.ok(
        //        ApiResponse.ok(paymentService.verify(userId, request)));
    }

    // 환불
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PaymentRequestDto.Cancel request) {
        paymentService.cancel(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}