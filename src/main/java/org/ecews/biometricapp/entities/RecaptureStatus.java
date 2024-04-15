package org.ecews.biometricapp.entities;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity(name = "recapture_status")
@Data
public class RecaptureStatus {

    @Id
    @CsvBindByPosition(position = 0)
    private String patientId;

    @CsvBindByPosition(position = 1)
    private String status;

    @CsvBindByPosition(position = 2)
    @CsvDate("dd/MM/yyyy") // Define the date format
    @DateTimeFormat(pattern = "dd/MM/yyyy") // Optional Spring format, useful for MVC endpoints
    private LocalDate statusDate;
}
