package com.expensetracker.service;

import com.expensetracker.dto.PaymentMethodDTO;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.model.PaymentMethod;
import com.expensetracker.model.User;
import com.expensetracker.repository.PaymentMethodRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public List<PaymentMethodDTO> getAllForUser(Long userId) {
        return paymentMethodRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PaymentMethodDTO create(Long userId, PaymentMethodDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PaymentMethod pm = new PaymentMethod();
        pm.setName(dto.getName());
        pm.setIcon(dto.getIcon());
        pm.setUser(user);

        return toDTO(paymentMethodRepository.save(pm));
    }

    public PaymentMethodDTO update(Long userId, Long id, PaymentMethodDTO dto) {
        PaymentMethod pm = paymentMethodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        pm.setName(dto.getName());
        pm.setIcon(dto.getIcon());

        return toDTO(paymentMethodRepository.save(pm));
    }

    public void delete(Long userId, Long id) {
        PaymentMethod pm = paymentMethodRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        paymentMethodRepository.delete(pm);
    }

    private PaymentMethodDTO toDTO(PaymentMethod pm) {
        PaymentMethodDTO dto = new PaymentMethodDTO();
        dto.setId(pm.getId());
        dto.setName(pm.getName());
        dto.setIcon(pm.getIcon());
        return dto;
    }
}
