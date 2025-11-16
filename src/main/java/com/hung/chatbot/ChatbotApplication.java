package com.hung.chatbot;

import com.hung.chatbot.DTO.MaterialsInformationDTO;
import com.hung.chatbot.entity.MaterialsInformation;
import com.hung.chatbot.service.MaterialsInformationService;
import com.hung.chatbot.ultils.ExcelReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

@SpringBootApplication
public class ChatbotApplication {

	public static void main(String[] args) {
//		SpringApplication.run(ChatbotApplication.class, args);
		String filePath = "C:/Users/Admin/Downloads/Book1.xlsx";

		//"C:\Users\Admin\Downloads\Book1.xlsx"

		System.out.println("=== Đọc dữ liệu thô ===");
		List<MaterialsInformation> rawData = ExcelReader.readExcelToObjects(filePath, "Sheet1", MaterialsInformation.class, true);

		ConfigurableApplicationContext context = SpringApplication.run(ChatbotApplication.class, args);

		MaterialsInformationService service = context.getBean(MaterialsInformationService.class);
		service.insertFromFile(rawData);
	}

}
