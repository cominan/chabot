package com.hung.chatbot.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
/**
 * @author Admin
 * @since 11/16/2025
 */

@Entity
@Getter
@Setter
@Table(name = "MATERIALS_INFORMATION")
public class MaterialsInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STT")
    private Long stt;

    @Column(name = "TEN_VT", nullable = false)
    private String tenVt;

    @Column(name = "SO_LUONG", nullable = false)
    private Integer soLuong;

    @Column(name = "KHO_PP", nullable = false)
    private String khoPp;

    @Column(name = "KHO_NHAN", nullable = false)
    private String khoNhan;

    @Column(name = "NGUOI_TH", nullable = false)
    private String nguoiTh;

    @Column(name = "THOI_GIAN")
    private LocalDateTime thoiGian;

}
