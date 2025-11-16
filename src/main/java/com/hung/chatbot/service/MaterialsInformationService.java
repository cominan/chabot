package com.hung.chatbot.service;

import com.hung.chatbot.entity.MaterialsInformation;
import com.hung.chatbot.repository.MaterialsInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @since 11/16/2025
 */

@Service
public class MaterialsInformationService {
    @Autowired
    private MaterialsInformationRepository repo;

    public void insertSample() {
        MaterialsInformation m = new MaterialsInformation();
        m.setTenVt("Sắt xây dựng");
        m.setSoLuong(50);
        m.setKhoPp("Kho A");
        m.setKhoNhan("Kho B");
        m.setNguoiTh("Nguyen Van A");
        m.setThoiGian(LocalDateTime.now());

        repo.save(m);  // -> AUTO INSERT
    }

    public void insertFromFile(List<MaterialsInformation> value) {
        if (value == null || value.isEmpty()) {
            System.out.println("Danh sách vật tư trống, không có gì để lưu");
            return;
        }

        // Lọc các bản ghi hợp lệ
        List<MaterialsInformation> validRecords = value.stream()
                .filter(m -> isValidMaterial(m))
                .collect(Collectors.toList());

        if (validRecords.isEmpty()) {
            System.out.println("Không có bản ghi hợp lệ để lưu");
            return;
        }

        try {
            repo.saveAll(validRecords);
            System.out.println("Đã lưu thành công " + validRecords.size() + " vật tư vào cơ sở dữ liệu");

            // Thông báo số bản ghi bị lỗi (nếu có)
            if (validRecords.size() < value.size()) {
                System.out.println((value.size() - validRecords.size()) + " bản ghi không hợp lệ đã bị bỏ qua");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu dữ liệu từ file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidMaterial(MaterialsInformation material) {
        return material != null &&
                material.getTenVt() != null && !material.getTenVt().trim().isEmpty() &&
                material.getSoLuong() >= 0;
    }
}
