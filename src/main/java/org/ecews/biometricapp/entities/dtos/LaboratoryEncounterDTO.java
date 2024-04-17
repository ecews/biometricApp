package org.ecews.biometricapp.entities.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class LaboratoryEncounterDTO implements Serializable {
		private String visitId;
		private String visitDate;
		private String collectionDate;
		private String orderedTestDate;
		private String resultedTestDate;
		private String laboratoryTestTypeCode;
		private String laboratoryTestIdentifier;
		private String laboratoryResultedTestCode;
		private String laboratoryResultAnswerNumeric;
		private String laboratoryResultedTestCodeDescTxt;
}
