package org.ecews.biometricapp.entities.dtos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabEncounterDTO  implements Serializable {
	private String visitId;
	private String labTestName;
	private String resultReported;
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime dateSampleCollected;
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime dateAssayed;
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime resultDate;
}
